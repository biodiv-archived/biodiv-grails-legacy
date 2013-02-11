package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.params.TermsParams

import species.CommonNames
import species.Habitat;
import species.NamesParser
import species.Synonyms
import species.TaxonomyDefinition
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.participation.Checklist;
import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote.ConfidenceType;

class ChecklistSearchService {

	static transactional = false

	def grailsApplication

	SolrServer solrServer;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	static int BATCH_SIZE = 50;

	/**
	 * 
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to checklist search index"

		//TODO: change limit
		int limit = Checklist.count()+1, offset = 0;

		def checklists;
		def startTime = System.currentTimeMillis()
		while(true) {
			checklists = Checklist.list(max:limit, offset:offset);
			if(!checklists) break;
			publishSearchIndex(checklists, true);
			checklists.clear();
			offset += limit;
		}

		log.info "Time taken to publish checklists search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	def publishSearchIndex(Checklist chk, boolean commit) {
		return publishSearchIndex([chk], commit);
	}
	/**
	 * 
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<Checklist> chks, boolean commit) {
		if(!chks) return;
		log.info "Initializing publishing to checklists search index : "+chks.size();

		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]

		chks.each { chk ->
			log.debug "Reading Checklist : "+chk.id;
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField(searchFieldsConfig.ID, chk.id.toString());

			doc.addField(searchFieldsConfig.TITLE, chk.title);

			doc.addField(searchFieldsConfig.CONTRIBUTOR, chk.author.name);
			doc.addField(searchFieldsConfig.ATTRIBUTION, chk.attribution);
			doc.addField(searchFieldsConfig.LOCATION, chk.placeName);
			doc.addField(searchFieldsConfig.LATLONG, chk.latitude+","+chk.longitude);


			doc.addField(searchFieldsConfig.UPLOADED_ON, chk.publicationDate);
			doc.addField(searchFieldsConfig.UPDATED_ON, chk.lastUpdated);
			doc.addField(searchFieldsConfig.FROM_DATE, chk.fromDate);
			doc.addField(searchFieldsConfig.TO_DATE, chk.toDate);

			//doc.addField(searchFieldsConfig.SGROUP, chk.group.id);
			//doc.addField(searchFieldsConfig.HABITAT, chk.habitat.id);
			doc.addField(searchFieldsConfig.REFERENCE, chk.refText);

			chk.speciesGroups.each { sGroup ->
				doc.addField(searchFieldsConfig.SGROUP, sGroup.id);
			}

			chk.userGroups.each { userGroup ->
				doc.addField(searchFieldsConfig.USER_GROUP, userGroup.id);
				doc.addField(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
			}

			chk.row.each { row ->
				if(row.reco) {
					doc.addField(searchFieldsConfig.NAME, row.reco.name);
				}
				doc.addField(searchFieldsConfig.MESSAGE, row.value);
			}
			doc.addField(searchFieldsConfig.MESSAGE, chk.description);
			chk.state.each { s->
				doc.addField(searchFieldsConfig.LOCATION, chk.placeName);
			}
			chk.district.each { s->
				doc.addField(searchFieldsConfig.LOCATION, chk.placeName);
			}
			chk.taluka.each { s->
				doc.addField(searchFieldsConfig.LOCATION, chk.placeName);
			}

			docs.add(doc);
		}

		//log.debug docs;

		try {
			solrServer.add(docs);
			if(commit) {
				//commit ...server is configured to do an autocommit after 10000 docs or 1hr
				solrServer.blockUntilFinished();
				solrServer.commit();
				log.info "Finished committing to checklists solr core"
			}
		} catch(SolrServerException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def search(query) {
		def params = SolrParams.toSolrParams(query);
		log.info "Running checklist search query : "+params
		return solrServer.query( params );
	}

	/**
	 * delete requires an immediate commit
	 * @return
	 */
	def delete(long id) {
		log.info "Deleting checklist from search index"
		solrServer.deleteByQuery("id:${id}");
		solrServer.commit();
	}

	/**
	 * 
	 * @return
	 */
	def deleteIndex() {
		log.info "Deleting checklist search index"
		solrServer.deleteByQuery("*:*")
		solrServer.commit();
	}

	/**
	 * 
	 * @return
	 */
	def optimize() {
		log.info "Optimizing checklist search index"
		solrServer.optimize();
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	def terms(query, field, limit) {
		field = field ?: "autocomplete";
		SolrParams q = new SolrQuery().setQueryType("/terms")
				.set(TermsParams.TERMS, true).set(TermsParams.TERMS_FIELD, field)
				.set(TermsParams.TERMS_LOWER, query)
				.set(TermsParams.TERMS_LOWER_INCLUSIVE, true)
				.set(TermsParams.TERMS_REGEXP_STR, query+".*")
				.set(TermsParams.TERMS_REGEXP_FLAG, "case_insensitive")
				.set(TermsParams.TERMS_LIMIT, limit)
				.set(TermsParams.TERMS_RAW, true);
		log.info "Running checklist search query : "+q
		return solrServer.query( q );
	}
}
