package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.io.WKTWriter;

import species.groups.UserGroup;

class UserGroupSearchService extends AbstractSearchService {

    static transactional = false

	/**
	 *
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to usergroup search index"

		//TODO: change limit
		int limit = BATCH_SIZE//UserGroup.count()+1,
        int offset = 0, noIndexed = 0;

		def userGroups;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:UserGroup.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            UserGroup.withNewTransaction([readOnly:true]) { status ->
                userGroups = UserGroup.list(max:limit, offset:offset);
                noIndexed += userGroups.size();
                if(!userGroups) return;
                publishSearchIndex(userGroups, true);
                //userGroups.clear();
                offset += limit;
            }
            if(!userGroups) break;
            userGroups.clear();
        }

		log.info "Time taken to publish usergroup search index is ${System.currentTimeMillis()-startTime}(msec)";
	}

	/**
	 *
	 * @param species
	 * @return
	 */
	def publishSearchIndex(List<UserGroup> ugs, boolean commit) {
		if(!ugs) return;
		log.info "Initializing publishing to usergroups search index : "+ugs.size();

		def fieldsConfig = grails.util.Holders.config.speciesPortal.fields
		def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields


    List<Map<String,Object>> eDocs=new ArrayList<Map<String,Object>>();

		Map names = [:];
		Map docsMap = [:]
		ugs.each { ug ->
			log.debug "Reading usergroup : "+ug.id;

        Map<String,Object> doc=new HashMap<String,Object>();

              //  doc.setDocumentBoost(3);
				doc.put(searchFieldsConfig.ID, ug.id.toString());
			    doc.put(searchFieldsConfig.OBJECT_TYPE, ug.class.simpleName);
				doc.put(searchFieldsConfig.TITLE, ug.name);
				//Location
                doc.put(searchFieldsConfig.UPLOADED_ON, ug.foundedOn);
				//Pages
                def allPages = ""
                ug.newsletters.each {
                    allPages += it.title + " "
                }
                doc.put(searchFieldsConfig.PAGES, allPages);

                String memberInfo = ""
                List allMembers = utilsServiceBean.getParticipants(ug)
                allMembers.each { mem ->
                    memberInfo = mem.name + " ### " + mem.email +" "+ mem.username +" "+mem.id.toString()
                    doc.put(searchFieldsConfig.MEMBERS, memberInfo);
                }
              
                eDocs.add(doc);

		}
    postToElastic(eDocs,"usergroup")
        //return commitDocs(docs, commit);
	}

    def delete(long id) {
        super.delete(UserGroup.simpleName +"_"+id.toString());
    }
}
