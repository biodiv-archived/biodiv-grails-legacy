package speciespage.search

//import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map
import java.util.HashMap

import species.auth.SUser;
import groovyx.net.http.HTTPBuilder

import groovyx.net.http.ContentType
import static groovyx.net.http.Method.POST

class SUserSearchService extends AbstractSearchService {

    static transactional = false

	/**
	 *
	 */
	def publishSearchIndex() {
		log.info "Initializing publishing to ufiles search index"

		//TODO: change limit
		int limit = BATCH_SIZE, offset = 0, noIndexed = 0;

        def susers;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:SUser.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            SUser.withNewTransaction([readOnly:true]) { status ->
                susers = SUser.findAll("from SUser as u where u.accountLocked =:ae and u.accountExpired =:al and u.enabled=:en", [ae:false, al:false, en:true], [max:limit, offset:offset, sort: "id"]);
                noIndexed += susers.size();
                if(!susers) return;
                if(susers)  {
                    publishSearchIndex(susers, true);
                    //susers.clear();
                }
                offset += limit;
                cleanUpGorm();
            }
            if(!susers) break;
            susers.clear();
        }

		log.info "Time taken to publish users search index is ${System.currentTimeMillis()-startTime}(msec)";
	 }

	/**
	 *
	 * @param ufiles
	 * @param commit
	 * @return
	 */

  def publishSearchIndex(List<SUser> susers, boolean commit) {
		if(!susers) return;
		log.info "Initializing publishing to Users search index : "+susers.size();

		def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields

    List<Map<String,Object>> eDocs=new ArrayList<Map<String,Object>>();
		Map names = [:];
		Map docsMap = [:]
		susers.each { suser ->
            suser = SUser.read(suser.id);
            log.debug "Reading User : "+suser.id;
            Map<String,Object> doc=new HashMap<String,Object>();
            //doc.setDocumentBoost(3);
            String className = org.hibernate.Hibernate.getClass(suser).getSimpleName()
            doc.put(searchFieldsConfig.ID, suser.id.toString());
            doc.put(searchFieldsConfig.OBJECT_TYPE, className);
            doc.put(searchFieldsConfig.NAME, suser.name);
            doc.put(searchFieldsConfig.USERNAME, suser.username);
            doc.put(searchFieldsConfig.EMAIL, suser.email);
            doc.put(searchFieldsConfig.ABOUT_ME, suser.aboutMe);
            doc.put(searchFieldsConfig.LAST_LOGIN, suser.lastLoginDate);
            doc.put("placename", suser.location);

            String userInfo = suser.name + " ### " + suser.email +" "+ suser.username +" "+suser.id.toString()
            doc.put(searchFieldsConfig.USER, userInfo);

            doc.put(searchFieldsConfig.UPLOADED_ON, suser.dateCreated);
            doc.put(searchFieldsConfig.UPDATED_ON, suser.lastLoginDate);

            suser.groups.each { userGroup ->
                doc.put(searchFieldsConfig.USER_GROUP, userGroup.id);
                doc.put(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
            }



            eDocs.add(doc);
		}
        postToElastic(eDocs,"suser");
        //return commitDocs(eDocs, commit);
	}


    def delete(long id) {
        String className = org.hibernate.Hibernate.getClass(suser).getSimpleName()
        super.delete("suser",id.toString());
    }
}
