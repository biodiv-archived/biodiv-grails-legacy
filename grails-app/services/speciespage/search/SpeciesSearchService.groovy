package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.List
import java.util.Map

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.common.SolrException
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.params.TermsParams
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.transaction.annotation.Transactional

import species.CommonNames
import species.NamesParser
import species.Species
import species.SpeciesField
import species.Synonyms
import species.TaxonomyDefinition
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer
import species.auth.SUser;

class SpeciesSearchService extends AbstractSearchService {

    def resourceSearchService;
    static transactional = false

	/**
	 *
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to search index"

		//TODO: change limit
		int limit=5, offset = 0, noIndexed = 0;

		def species;
		def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:Species.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
		while(noIndexed < INDEX_DOCS) {
            Species.withNewTransaction([readOnly:true]) { status ->
                println "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
                println status.isNewTransaction();
			species = listSpecies(0, [max:limit, offset:offset,sort:'id',order:'asc']);
            noIndexed += species.size();
			if(!species) return;
			publishSearchIndex(species);
			offset += limit;
			cleanUpGorm();
            }
			if(!species) break;;
			species = null;
		}
		log.info "Time taken to publish search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	def listSpecies(id, params) {
			//return Species.findAllByIdGreaterThanAndPercentOfInfoGreaterThan(id,0, params);
			return Species.findAllByIdGreaterThan(id, params);
	}

	/**
	 *
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<Species> species, boolean commit=true) {
		if(!species) return;
		log.info "Initializing publishing to search index for species : "+species.size();

		def fieldsConfig = grails.util.Holders.config.speciesPortal.fields
		def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		List<Map<String,Object>> eDocs=new ArrayList<Map<String,Object>>();
		Map names = [:];
		Map docsMap = [:]

		species.each { s ->
			log.debug "Reading Species : "+s.id;
			SolrInputDocument docsa = new SolrInputDocument();
			Map<String,Object> doc=new HashMap<String,Object>();
          //  doc.setDocumentBoost(2);
			doc.put(searchFieldsConfig.ID, s.id.toString());
			doc.put(searchFieldsConfig.OBJECT_TYPE, s.class.simpleName);
			doc.put(searchFieldsConfig.GUID, s.guid);
			docsa.addField(searchFieldsConfig.ID, s.class.simpleName +"_"+s.id.toString());
			docsa.addField(searchFieldsConfig.OBJECT_TYPE, s.class.simpleName);
			docsa.addField(searchFieldsConfig.GUID, s.guid);
			addNameToDoc(docsa, s.taxonConcept);

			def syns = s.taxonConcept.fetchSynonyms()
			syns.each { syn ->
				doc.put(searchFieldsConfig.NAME, syn.name);
			}

			def commonNames = CommonNames.findAllByTaxonConceptAndIsDeleted(s.taxonConcept, false);
			commonNames.each { commonName ->
				doc.put(searchFieldsConfig.NAME, commonName.name);
			}

			s.globalDistributionEntities.each {
				doc.put(searchFieldsConfig.LOCATION, it.country.countryName);
			}
			s.globalEndemicityEntities.each {
				doc.put(searchFieldsConfig.LOCATION, it.country.countryName);
			}
			s.indianDistributionEntities.each {
				doc.put(searchFieldsConfig.LOCATION, it.country.countryName);
			}
			s.indianEndemicityEntities.each {
				doc.put(searchFieldsConfig.LOCATION, it.country.countryName);
			}
			s.fetchTaxonomyRegistry().each { classification, taxonDefinitionsList ->
				taxonDefinitionsList.each { taxonDefinition ->
					doc.put(searchFieldsConfig.TAXON, taxonDefinition.canonicalForm);
				}
			}

			String message = "";
			s.fields.each { field ->
				boolean copyDesc = true;
				String concept = field.field.concept;
				String category = field.field.category;
				String subcategory = field.field.subCategory;

                field.licenses.each { l ->
    				doc.put(searchFieldsConfig.LICENSE, l.name.name());
                }

				field.contributors.each { contributor ->
                    String userInfo = ""
                    if(contributor.id) {
                        userInfo = " ### "+contributor.email+" "+contributor.username+" "+contributor.id.toString()
                    }
					if(contributor.name)
						doc.put(searchFieldsConfig.CONTRIBUTOR, contributor.name + userInfo);
				}
				field.attributors.each { attribution ->
					if(attribution.name)
						doc.put(searchFieldsConfig.ATTRIBUTION, attribution.name);
				}

				field.references.each { reference ->
					if(reference.title)
						doc.put(searchFieldsConfig.REFERENCE, reference.title)
				}
				if(field.description && copyDesc) {
					message += field.description+" ";
				}

                switch(concept) {
                    case ["Overview","Généralités"] :
						doc.put(searchFieldsConfig.SP_OVERVIEW, field.description);
                    break

                    case ["Nomenclature and Classification","Nomenclature et Classification"] :
						doc.put(searchFieldsConfig.SP_NC, field.description);
                    break

                    case ["Natural History","Histoire Naturelle"] :
						doc.put(searchFieldsConfig.SP_NH, field.description);
                    break

                    case ["Habitat and Distribution","Habitat et Distribution"] :
						doc.put(searchFieldsConfig.SP_HD, field.description);
                    break

                    case ["Demography and Conservation","Démographie et Conservation"] :
						doc.put(searchFieldsConfig.SP_DC, field.description);
                    break

                    case ["Uses and Management","Usages et Gestion"] :
						doc.put(searchFieldsConfig.SP_UM, field.description);
                    break

                    case ["Information Listing","Liste d'Information"] :
						doc.put(searchFieldsConfig.SP_IL, field.description);
                    break

                    default:
                    log.info "Not indexing this concept ${concept} separately"

                }

              //  List resourceDocs = getResourcesDocs(field);
			    //eDocs.addAll(resourceDocs);
			}

			s.resources.each { resource ->

				doc.put(resource.type.value().toLowerCase(), resource.description);

                resource.contributors.each  { contributor ->
                    String userInfo = ""
                    if(contributor.user) {
                        userInfo = " ### "+contributor.user.email+" "+contributor.user.username+" "+contributor.user.id.toString()
                    }
					if(contributor.name)
						doc.put(searchFieldsConfig.CONTRIBUTOR, contributor.name + userInfo);
				}
				resource.attributors.each {  attributor ->
					if(attributor.name)
						doc.put(searchFieldsConfig.ATTRIBUTION, attributor.name);
				}
			}

			doc.put(searchFieldsConfig.PERCENT_OF_INFO, s.percentOfInfo);
			doc.put(searchFieldsConfig.MESSAGE, message);

			doc.put(searchFieldsConfig.UPLOADED_ON, s.dateCreated);
			doc.put(searchFieldsConfig.UPDATED_ON, s.lastUpdated);
			doc.put(searchFieldsConfig.SGROUP, s.fetchSpeciesGroup().id.longValue());
			//doc.put(searchFieldsConfig.HABITAT, s.);

            String memberInfo = ""
            List allMembers = utilsServiceBean.getParticipants(s)
            allMembers.each { mem ->
				if(mem){
					memberInfo = mem.name + " ### " + mem.email +" "+ mem.username +" "+mem.id.toString()
					doc.put(searchFieldsConfig.MEMBERS, memberInfo);
				}
            }

            s.userGroups.each { userGroup ->
                doc.put(searchFieldsConfig.USER_GROUP, userGroup.id);
                doc.put(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
            }

            //List resourceDocs = getResourcesDocs(s);

          
			eDocs.add(doc);
			//eDocs.addAll(resourceDocs);
		}

		//log.debug docs;
		postToElastic(eDocs,"species")
        //return commitDocs(docs, commit);
	}

	/**
	 *
	 * @param doc
	 * @param name
	 */
	private void addNameToDoc(SolrInputDocument doc, TaxonomyDefinition name) {
		def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields

		if(name.name) doc.addField(searchFieldsConfig.TITLE, name.name);
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

    List getResourcesDocs(Species species) {
        def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields
        List resourcesDocs = [];
        def resourceDoc;
        species.resources.each { resource ->
            resourceDoc =  resourceSearchService.getSolrDocument(resource);
            if(resourceDoc) {
                resourceDoc.addField(searchFieldsConfig.NAME, species.fetchSpeciesCall());
                resourceDoc.addField(searchFieldsConfig.CONTAINER, species.class.simpleName +"_"+species.id.toString());
                resourcesDocs << resourceDoc
            }
        }
        return resourcesDocs;
    }

    List getResourcesDocs(SpeciesField speciesField) {
        def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields
        List resourcesDocs = [];
        def resourceDoc;
        speciesField.resources.each { resource ->
            resourceDoc =  resourceSearchService.getSolrDocument(resource);
            if(resourceDoc) {
                resourceDoc.addField(searchFieldsConfig.NAME, speciesField.species.fetchSpeciesCall());
                resourceDoc.addField(searchFieldsConfig.CONTAINER, speciesField.species.class.simpleName +"_"+speciesField.species.id.toString());
                resourcesDocs << resourceDoc
            }
        }
        return resourcesDocs;
    }

    def delete(long id) {
        super.delete(Species.simpleName +"_"+id.toString());
    }
}
