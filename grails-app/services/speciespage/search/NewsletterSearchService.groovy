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
import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote.ConfidenceType;
import utils.Newsletter;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer

class NewsletterSearchService extends AbstractSearchService {

	/**
	 * 
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to newsletters search index"
		
		//TODO: change limit
		int limit = BATCH_SIZE, offset = 0;
        int noIndexed = 0;
		
		def newsletters;
		def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:Newsletter.count()+1;
		while(noIndexed < INDEX_DOCS) {
			newsletters = Newsletter.list(max:limit, offset:offset);
            noIndexed += newsletters;
			if(!newsletters) break;
			publishSearchIndex(newsletters, true);
			newsletters.clear();
			offset += limit;
		}
		
		log.info "Time taken to publish newsletter search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<Newsletter> obvs, boolean commit) {
		if(!obvs) return;
		log.info "Initializing publishing to newsletters search index : "+obvs.size();

		def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields

		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		Map names = [:];
		Map docsMap = [:]
		obvs.each { obv ->
			log.debug "Reading Newsletter : "+obv.id;

				SolrInputDocument doc = new SolrInputDocument();
                println "=====ID======== " + obv.class.simpleName +"_"+obv.id.toString()
				doc.addField(searchFieldsConfig.ID, obv.class.simpleName +"_"+ obv.id.toString());
			    doc.addField(searchFieldsConfig.OBJECT_TYPE, obv.class.simpleName);
				doc.addField(searchFieldsConfig.NAME, obv.title);
				doc.addField(searchFieldsConfig.UPLOADED_ON, obv.date);
				doc.addField(searchFieldsConfig.UPDATED_ON, obv.date);
				if(obv.userGroup) {
					doc.addField(searchFieldsConfig.USER_GROUP, obv.userGroup.id);
					doc.addField(searchFieldsConfig.USER_GROUP_WEBADDRESS, obv.userGroup.webaddress);
				}
				if(obv.newsitem) {
					
					doc.addField(searchFieldsConfig.MESSAGE, obv.newsitem);
				}
				
//				obv.tags.each { tag ->
//					doc.addField(searchFieldsConfig.TAG, tag);
//				}
					
				docs.add(doc);
			
		}

        return commitDocs(docs, commit);
	}

}
