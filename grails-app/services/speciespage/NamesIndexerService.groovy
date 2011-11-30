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
import species.search.Lookup.LookupResult


class NamesIndexerService {

	static transactional = false
	def grailsApplication;

	private static final log = LogFactory.getLog(this);
	private Lookup lookup = new Lookup<Record>();
	private boolean dirty = false;
	
	/**
	 * Rebuilds whole index and persists it. 
	 * Existing lookup is present 
	 */
	void rebuild() {
		log.debug "Publishing names autocomplete index";
		Lookup<Record> lookup1 = new Lookup<Record>();
		
		setDirty(false);
		
		def analyzer = new ShingleAnalyzerWrapper(Version.LUCENE_CURRENT, 2, 15)
		analyzer.setOutputUnigrams(true);
		
		//TODO fetch in batches
		def startTime = System.currentTimeMillis()
		def recos = Recommendation.list();
		recos.each { reco ->
			add(reco, analyzer, lookup1);
		}
		
		synchronized(lookup) {
			lookup = lookup1;
		}
		
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
		
		log.debug "Adding recommendation : "+reco.name;
		
		boolean success = false;
		
		def icon = getIcon(reco.taxonConcept);
		log.debug "Generating ngrams"
		def tokenStream = analyzer.tokenStream("name", new StringReader(reco.name));
		OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		
		while (tokenStream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			String term = charTermAttribute.toString();
			log.debug "Adding name term : "+term
			synchronized(lookup) {
				success |= lookup.add(term.replaceAll("\u00A0|\u2007|\u202F", " "), new Record(name:term, originalName:reco.name, canonicalForm:reco.taxonConcept.canonicalForm, icon:icon, wt:0));
			}
		}
		return success;
	}
	
	private String getIcon(TaxonomyDefinition taxonConcept) {
		if(!taxonConcept) return "";

		log.debug "Getting icon"
		def species = Species.findByTaxonConcept(taxonConcept);
		def icon = species?.mainImage()?.fileName;
		if(icon) {
			icon = grailsApplication.config.speciesPortal.images.serverURL + "/images/" + icon;
			icon = icon.replaceFirst(/\.[a-zA-Z]{3,4}$/, '_gall_th.jpg');
		}
		return icon;
	}
	
	/**
	 * 
	 * @param reco
	 * @return
	 */
	boolean update(Recommendation oldReco, Recommendation reco) {
		log.debug "Updating recommendation : "+reco;
		delete(oldReco);
		add(reco);
	}

	/**
	 * 	
	 * @param reco
	 * @return
	 */
	boolean delete(Recommendation reco) {
		log.debug "Deleting recommendation from names index : "+reco;
		
		boolean success = false;
		
		log.debug "Generating nGrams from the name"
		def tokenStream = analyzer.tokenStream("name", new StringReader(reco.name));
		OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		
		while (tokenStream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			String term = charTermAttribute.toString();
			log.debug "Removing name to autoComplete index : "+term
			synchronized(lookup) {
				success &= lookup.remove(new Record(name:term, originalName:reco.name, canonicalForm:reco.taxonConcept.canonicalForm));
			}
		}
		return success;
	}
		
	/**
	*
	* @param query
	* @return
	*/
   def suggest(query) {
	   log.debug "Running term query : "+query
	   def fromElement = new Record(name:query.term);
	   def toElement = new Record(name:query.term.next());
	   //def result = lookup.subSet(fromElement, toElement);
	   List<LookupResult> result = lookup.lookup(query.term, true, 10);
	   log.debug "terms result : "+result
	   //return new ArrayList(result);
	   return result.collect {it.value};
   }
   
	/**
	 *
	 */
	boolean load(String storeDir) {
		File f = new File(storeDir);
		if(!f.exists()) {
			rebuild();
		} else {
			log.debug "Loading autocomplete index from : "+f.getAbsolutePath();
			lookup.load(new File(storeDir));
		}
	}
	
	/**
	 *
	 */
	boolean store(String storeDir) {
		File f = new File(storeDir);
		if(!f.exists()) {
			if(!f.mkdir()) {
				log.error "Could not create directory : "+storeDir;
			}
		}
		lookup.store(new File(storeDir));
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
