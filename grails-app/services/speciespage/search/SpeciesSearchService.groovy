package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.List
import java.util.Map

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.params.TermsParams

import species.CommonNames
import species.NamesParser
import species.Species
import species.Synonyms
import species.TaxonomyDefinition

class SpeciesSearchService {

	static transactional = false

	def grailsApplication
	
	SolrServer solrServer;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	static int BATCH_SIZE = 50;

	/**
	 * 
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to search index"
		
		//TODO: change limit
		int limit=Species.count()+1, offset = 0;
		
		def species;
		def startTime = System.currentTimeMillis()
		while(true) {
			species = Species.list(max:limit, offset:offset);
			if(!species) break;
			publishSearchIndex(species);
			species.clear();
			offset += limit;
		}
		log.info "Time taken to publish search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<Species> species) {
		if(!species) return;
		log.info "Initializing publishing to search index for species : "+species.size();

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
			s.fetchTaxonomyRegistry().each { classification, taxonDefinitionsList ->
				taxonDefinitionsList.each { taxonDefinition ->
					doc.addField(searchFieldsConfig.TAXON, taxonDefinition.canonicalForm);
				}
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
			solrServer.add(docs);
			//commit ...server is configured to do an autocommit after 10000 docs or 1hr
			solrServer.blockUntilFinished();
			solrServer.commit();
			log.info "Finished committing to solr species core"
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
		return parsedNamesMap;
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def search(query) {
		def params = SolrParams.toSolrParams(query);
		log.info "Running search query : "+params
		return solrServer.query( params );
	}

	/**
	 * 
	 * @return
	 */
	def deleteIndex() {
		log.info "Deleting search index"
		solrServer.deleteByQuery("*:*")
		solrServer.commit();
	}

	/**
	 * 
	 * @return
	 */
	def optimize() {
		log.info "Optimizing search index"
		solrServer.optimize();
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
		log.info "Running search query : "+q
		return solrServer.query( q );
	}
}
