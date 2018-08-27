package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import content.Project

class ProjectSearchService extends AbstractSearchService {

    static transactional = false
	/**
	 *
     */
    def publishSearchIndex() {
        log.info "Initializing publishing to projects search index"

        //TODO: change limit
        int limit = BATCH_SIZE, offset = 0;
        int noIndexed = 0;

        def projects;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:Project.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            Project.withNewTransaction([readOnly:true]) { status ->
                projects = Project.list(max:limit, offset:offset);
                noIndexed += projects.size()
                if(!projects) return;
                publishSearchIndex(projects, true);
                //projects.clear();
                offset += limit;
            }
            if(!projects) return;
            projects.clear();

        }

        log.info "Time taken to publish projects search index is ${System.currentTimeMillis()-startTime}(msec)";
    }

	/**
	 *
	 * @param projects
	 * @param commit
	 * @return
	 */
	def publishSearchIndex(List<Project> projects, boolean commit) {
		if(!projects) return;
		log.info "Initializing publishing to projects search index : "+projects.size();

		def fieldsConfig = grails.util.Holders.config.speciesPortal.fields
		def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields

    List<Map<String,Object>> eDocs=new ArrayList<Map<String,Object>>();

		Map names = [:];
		Map docsMap = [:]
		projects.each { proj ->
			log.debug "Reading Project : "+proj.id;

        Map<String,Object> doc=new HashMap<String,Object>();

				doc.put(searchFieldsConfig.ID, proj.id.toString());
				doc.put(searchFieldsConfig.TITLE, proj.title);
				doc.put(searchFieldsConfig.GRANTEE_ORGANIZATION, proj.granteeOrganization);

                doc.put(searchFieldsConfig.UPLOADED_ON, proj.dateCreated);
                doc.put(searchFieldsConfig.UPDATED_ON, proj.lastUpdated);


				proj.locations.each { location ->
					doc.put(searchFieldsConfig.SITENAME, location.siteName);
					doc.put(searchFieldsConfig.CORRIDOR, location.corridor);
				}


				proj.tags.each { tag ->
					doc.put(searchFieldsConfig.TAG, tag);
				}


				proj.userGroups.each { userGroup ->
					doc.put(searchFieldsConfig.USER_GROUP, userGroup.id);
					doc.put(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
				}
        String values = "";
        doc.each { k,v ->
          values += v.toString() +" ";
        }

        doc.put("all",values)
				eDocs.add(doc);

		}

        postToElastic(eDocs,"project")
        //return commitDocs(docs, commit);
	}

    def delete(long id) {
        super.delete("project",id.toString());
    }
}
