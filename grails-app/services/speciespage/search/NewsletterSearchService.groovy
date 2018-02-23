package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

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

class NewsletterSearchService extends AbstractSearchService {

    static transactional = false

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
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            Newsletter.withNewTransaction([readOnly:true]) { status ->
                newsletters = Newsletter.list(max:limit, offset:offset);
                noIndexed += newsletters.size();
                if(!newsletters) return;
                publishSearchIndex(newsletters, true);
                //newsletters.clear();
                offset += limit;
            }

            if(!newsletters) break;
            newsletters.clear();
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

		def fieldsConfig = grails.util.Holders.config.speciesPortal.fields
		def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields

    List<Map<String,Object>> eDocs=new ArrayList<Map<String,Object>>();

		Map names = [:];
		Map docsMap = [:]
		obvs.each { obv ->
			log.debug "Reading Newsletter : "+obv.id;

        Map<String,Object> doc=new HashMap<String,Object>();
                //doc.setDocumentBoost(1.5);
                println "=====ID======== " + obv.class.simpleName +"_"+obv.id.toString()
				doc.put(searchFieldsConfig.ID,  obv.id.toString());
			    doc.put(searchFieldsConfig.OBJECT_TYPE, obv.class.simpleName);
				doc.put(searchFieldsConfig.NAME, obv.title);
				doc.put(searchFieldsConfig.UPLOADED_ON, obv.date);
				doc.put(searchFieldsConfig.UPDATED_ON, obv.date);
				if(obv.userGroup) {
					doc.put(searchFieldsConfig.USER_GROUP, obv.userGroup.id);
					doc.put(searchFieldsConfig.USER_GROUP_WEBADDRESS, obv.userGroup.webaddress);
				}
				if(obv.newsitem) {

					doc.put(searchFieldsConfig.MESSAGE, obv.newsitem);
				}

//				obv.tags.each { tag ->
//					doc.put(searchFieldsConfig.TAG, tag);
//				}
  
				eDocs.add(doc);

		}

    postToElastic(eDocs,"newsletter")
        //return commitDocs(docs, commit);

	}

    def delete(long id) {
        super.delete(Newsletter.simpleName +"_"+id.toString());
    }

}
