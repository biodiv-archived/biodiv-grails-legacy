package species.search

import java.text.SimpleDateFormat;

import org.apache.commons.logging.LogFactory
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.MultiMapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.TermsParams;
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.SolrServerException;

import species.CommonNames;
import species.NamesParser;
import species.Species;
import species.Synonyms;
import species.TaxonomyDefinition;
import species.TaxonomyDefinition.TaxonomyRank;

import groovyx.net.http.HTTPBuilder;
import static groovyx.net.http.ContentType.JSON;

/**
 * 
 * @author sravanthi
 *
 */
class SearchIndexManager {

	private static SolrServer _server;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final log = LogFactory.getLog(this);

	/**
	 * 
	 */
	public SearchIndexManager() {
		initializeSearchServerConnection();
	}

	/**
	 * 
	 */
	private void initializeSearchServerConnection() {

		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.search

		//Uses an internal MultiThreadedHttpConnectionManager to manage http connections
		if(!_server) {
			_server = new StreamingUpdateSolrServer(config.serverURL, config.queueSize, config.threadCount );
			_server.setSoTimeout(config.soTimeout);
			_server.setConnectionTimeout(config.connectionTimeout);
			_server.setDefaultMaxConnectionsPerHost(config.defaultMaxConnectionsPerHost);
			_server.setMaxTotalConnections(config.maxTotalConnections);
			_server.setFollowRedirects(config.followRedirects);
			_server.setAllowCompression(config.allowCompression);
			_server.setMaxRetries(config.maxRetries);
			//_server.setParser(new XMLResponseParser()); // binary parser is used by default
			log.debug "Initialized search server to "+config.serverURL
		}
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<Species> species) {
		log.debug "Initializing publishing to search index"

		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]

		species.each { s ->
			log.debug "Reading Species : "+s.id;
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField(searchFieldsConfig.ID, s.id.toString());
			doc.addField(searchFieldsConfig.GUID, s.guid);
			addNameToDoc(doc, s.taxonConcept);
				
			def syns = Synonyms.findAllByTaxonConcept(s.taxonConcept)
			syns.each { syn ->
				doc.addField(searchFieldsConfig.NAME, syn.name);
			}
			
			def commonNames = CommonNames.findAllByTaxonConcept(s.taxonConcept);
			commonNames.each { commonName ->
				doc.addField(searchFieldsConfig.NAME, commonName.name);
			}

			s.globalDistributionEntities.each {
				doc.addField(searchFieldsConfig.LOCATION, it.country.countryName);
			} 
			s.globalEndemicityEntities.each {
				doc.addField(searchFieldsConfig.LOCATION, it.country.countryName);
			}
			s.indianDistributionEntities.each {
				doc.addField(searchFieldsConfig.LOCATION, it.country.countryName);
			}
			s.indianEndemicityEntities.each {
				doc.addField(searchFieldsConfig.LOCATION, it.country.countryName);
			}
			s.taxonomyRegistry.each { 
				doc.addField(searchFieldsConfig.TAXON, it.taxonDefinition.name);
			}
				
			String message = "";
			s.fields.each { field ->
				boolean copyDesc = true;
				String concept = field.field.concept;
				String category = field.field.category;
				String subcategory = field.field.subCategory;

				field.contributors.each { contributor ->
					if(contributor.name)
						doc.addField(searchFieldsConfig.CONTRIBUTOR, contributor.name);
				}
				field.attributors.each { attribution ->
					if(attribution.name)
						doc.addField(searchFieldsConfig.ATTRIBUTION, attribution.name);
				}
				field.resources.each { resource ->

					doc.addField(resource.type.value().toLowerCase(), resource.description);

					resource.contributors.each { contributor ->
						if(contributor.name)
							doc.addField(searchFieldsConfig.CONTRIBUTOR, contributor.name);
					}
				}
				field.references.each { reference ->
					if(reference.title)
						doc.addField(searchFieldsConfig.REFERENCE, reference.title)
				}
				if(field.description && copyDesc) {
					message += field.description+" ";
				}
			}
			doc.addField(searchFieldsConfig.MESSAGE, message);
			docs.add(doc);

		}

		//log.debug docs;

		try {
			_server.add(docs);
			//commit ...server is configured to do an autocommit after 10000 docs or 1hr
			_server.blockUntilFinished();
			_server.commit();
			log.debug "Finished committing to solr species core"
		} catch(SolrServerException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param doc
	 * @param name
	 */
	private void addNameToDoc(SolrInputDocument doc, TaxonomyDefinition name) {
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		if(name.canonicalForm) doc.addField(searchFieldsConfig.CANONICAL_NAME, name.canonicalForm);
		//if(name.normalizedForm) doc.addField(searchFieldsConfig.SCIENTIFIC_NAME, name.normalizedForm);
		if(name.normalizedForm) doc.addField(searchFieldsConfig.NAME, name.normalizedForm);
		//if(name.uninomial) doc.addField(searchFieldsConfig.UNINOMIAL, name.uninomial);
		//if(name.genus) doc.addField(searchFieldsConfig.GENUS, name.genus);
		//if(name.species) doc.addField(searchFieldsConfig.SPECIES, name.species);
		//if(name.infraGenus) doc.addField(searchFieldsConfig.INFRAGENUS, name.infraGenus);
		//if(name.infraSpecies) doc.addField(searchFieldsConfig.INFRASPECIES, name.infraSpecies);
		for( author in name.author) {
			doc.addField(searchFieldsConfig.AUTHOR, author);
		}
		
		for( year in name.year) {
			doc.addField(searchFieldsConfig.YEAR, Integer.parseInt(year));
		}
	}

	/**
	 * 
	 * @param names
	 * @return
	 */
	private Map fetchParsedNames(names) {
		def nameParser = new NamesParser();
		def parsedNamesMap = [:];
		names.each { docName ->
			parsedNamesMap[docName.key] = nameParser.parse(docName.value);
		}
		println '-----------------------';
		println parsedNamesMap;
		return parsedNamesMap;
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def search(query) {
		def params = SolrParams.toSolrParams(query);
		log.debug "Running search query : "+params
		return _server.query( params );
	}

	/**
	 * 
	 * @return
	 */
	def deleteIndex() {
		log.debug "Deleting search index"
		_server.deleteByQuery("*:*")
		_server.commit();
	}

	/**
	 * 
	 * @return
	 */
	def optimize() {
		log.debug "Optimizing search index"
		_server.optimize();
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def terms(query) {
		def field = query?.field?:"autocomplete";
		SolrParams q = new SolrQuery().setQueryType("/terms")
				.set(TermsParams.TERMS, true).set(TermsParams.TERMS_FIELD, field)
				.set(TermsParams.TERMS_LOWER, query.term)
				.set(TermsParams.TERMS_LOWER_INCLUSIVE, false)
				.set(TermsParams.TERMS_REGEXP_STR, query.term+".*")
				.set(TermsParams.TERMS_REGEXP_FLAG, "case_insensitive")
				.set(TermsParams.TERMS_RAW, true);
		log.debug "Running search query : "+q
		return _server.query( q );
	}
}
