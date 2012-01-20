package speciespage

import static groovyx.net.http.ContentType.JSON

import java.util.List

import org.apache.commons.logging.LogFactory
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.util.Version

import species.Species
import species.TaxonomyDefinition
import species.participation.Recommendation
import species.search.Lookup
import species.search.Record
import species.search.TSTLookup
import species.search.Lookup.LookupResult


class NamesIndexerService {

	static transactional = false
	def grailsApplication;

	private static final log = LogFactory.getLog(this);
	private Lookup lookup = new TSTLookup();
	private boolean dirty = false;

	/**
	 * Rebuilds whole index and persists it. 
	 * Existing lookup is present 
	 */
	void rebuild() {
		log.debug "Publishing names to autocomplete index";
		Lookup lookup1 = new TSTLookup();

		setDirty(false);

		def analyzer = new ShingleAnalyzerWrapper(Version.LUCENE_CURRENT, 2, 15)
		analyzer.setOutputUnigrams(true);

		//TODO fetch in batches
		def startTime = System.currentTimeMillis()
		def recos = Recommendation.list();
		int noOfRecosAdded = 0;
		recos.each { reco ->
			if(add(reco, analyzer, lookup1)) {
				noOfRecosAdded++;
			}
		}

		synchronized(kk) {
			lookup = lookup1;
		}
		
		log.debug "Added recos : ${noOfRecosAdded}";
		log.debug "Time taken to rebuild index : "+((System.currentTimeMillis() - startTime)/1000) + "(sec)"

		def indexStoreDir = grailsApplication.config.speciesPortal.nameSearch.indexStore;
		store(indexStoreDir);
	}

	/**
	 * 
	 * @param reco
	 * @return
	 */
	boolean add(Recommendation reco) {
		def analyzer = new ShingleAnalyzerWrapper(Version.LUCENE_CURRENT, 2, 15)
		analyzer.setOutputUnigrams(true);
		return add(reco, analyzer, lookup);
	}

	/**
	 * 
	 * @param reco
	 * @param analyzer
	 * @param lookup
	 * @return
	 */
	private boolean add(Recommendation reco, Analyzer analyzer, lookup) {
		if(isDirty()) {
			log.debug "Rebuilding index as its dirty"
			rebuild();
			return;
		}

		log.debug "Adding recommendation : "+reco.name + " with taxonConcept : "+reco.taxonConcept;

		boolean success = false;

		def species = getSpecies(reco.taxonConcept);
		def icon = species?.mainImage()?.fileName;
		log.debug "Generating ngrams"
		def tokenStream = analyzer.tokenStream("name", new StringReader(reco.name));
		OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);

		def wt = 0;
		while (tokenStream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			String term = charTermAttribute.toString()?.replaceAll("\u00A0|\u2007|\u202F", " ");
			log.debug "Adding name term : "+term
			synchronized(lookup) {
				success |= lookup.add(term, new Record(originalName:reco.name, canonicalForm:reco.taxonConcept?.canonicalForm, icon:icon, wt:wt, speciesId:species?.id));
			}
		}
		return success;
	}

	private Species getSpecies(TaxonomyDefinition taxonConcept) {
		if(!taxonConcept) return null;		
		return Species.findByTaxonConcept(taxonConcept);
	}

	/**
	 * 
	 */
	boolean incrementByWt(String taxonConcept, int wt) {
		def record = lookup.getByName(taxonConcept);
		if(record) {
			synchronized(record) {
				record.wt += wt;
			}
			return true;
		} else {
			log.error "Couldnt find the record for term : "+taxonConcept
		}
		return false;
	}

	/**
	 *
	 * @param query
	 * @return
	 */
	def suggest(params) {
		log.debug "Suggest name using params : "+params
		List<LookupResult> lookupResults = lookup.lookup(params.term.toLowerCase(), true, 10);		
		
		def result = new ArrayList();
		lookupResults.each { lookupResult ->
			def term = lookupResult.key;
			def record = lookupResult.value;
			int index = term.toLowerCase().indexOf(params.term.toLowerCase());
			
			String name = term.replaceFirst(/(?i)${params.term}/, "<b>"+params.term+"</b>");
			String highlightedName = record.originalName.replaceFirst(/(?i)${term}/, name);
			String icon = record.icon;
			if(icon) {
				icon = grailsApplication.config.speciesPortal.resources.serverURL + "/" + icon;
				icon = icon.replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix);
			}
			result.add([value:record.canonicalForm, label:highlightedName, desc:record.canonicalForm, icon:icon, speciesId:record.speciesId, "category":"Names"]);
		}
		//Thread.sleep(10000);
		log.debug "suggestion : "+result;
		return result;
	}

	public static final String FILENAME = "tstLookup.dat";

	/**
	 *
	 */
	synchronized boolean load(String storeDir) {
		File f = new File(storeDir, FILENAME);
		if(!f.exists() || !f.canRead()) {
			rebuild();
		} else {
			log.debug "Loading autocomplete index from : "+f.getAbsolutePath();
			def startTime = System.currentTimeMillis()
			f.withObjectInputStream(lookup.getClass().classLoader){ ois ->
				lookup = ois.readObject( )
			}
			log.debug "Loading autocomplete index done";
			log.debug "Time taken to load names index : "+((System.currentTimeMillis() - startTime)/1000) + "(sec)"
			return true;
		}
	}

	/**
	 *
	 */
	synchronized boolean store(String storeDir) {
		File f = new File(storeDir);
		if(!f.exists()) {
			if(!f.mkdir()) {
				log.error "Could not create directory : "+storeDir;
			}
		}
		if (!f.exists() || !f.isDirectory() || !f.canWrite()) {
			return false;
		}
		
		def startTime = System.currentTimeMillis();
		File data = new File(f, FILENAME);
		data.withObjectOutputStream { oos ->
			oos.writeObject(lookup);
		}
		log.debug "Time taken to store index : "+((System.currentTimeMillis() - startTime)/1000) + "(sec)"
		return true;
	}

	/**
	 * 	
	 */
	synchronized void setDirty(boolean flag) {
		dirty = flag;
	}

	/**
	 * 
	 */
	boolean isDirty() {
		return dirty;
	}
}
