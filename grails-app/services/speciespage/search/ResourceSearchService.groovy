package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import org.springframework.transaction.annotation.Transactional

import species.CommonNames
import species.Habitat;
import species.NamesParser
import species.Synonyms;
import species.Resource;
import species.TaxonomyDefinition
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote.ConfidenceType;
import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.io.WKTWriter;

class ResourceSearchService extends AbstractSearchService {

    static transactional = false

    /**
     *
     */
    def publishSearchIndex() {
        log.info "Initializing publishing to resources search index"

        //TODO: change limit
        int limit = BATCH_SIZE//Resource.count()+1,
        int offset = 0;
        int noIndexed = 0;

        def resources;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:Resource.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            Resource.withNewTransaction([readOnly:true]) { status ->
                resources = Resource.list(max:limit, offset:offset, sort:'id');
                noIndexed += resources.size()
                if(!resources) return;
                publishSearchIndex(resources, true);
                //resources.clear();
                offset += limit;
                cleanUpGorm()
            }
            if(!resources) break;

            resources.clear();

        }
        log.info "Time taken to publish resources search index is ${System.currentTimeMillis()-startTime}(msec)";
    }

    /**
     * @param species
     * @return
     */
    def publishSearchIndex(List<Resource> resources, boolean commit) {
        if(!resources) return;
        log.info "Initializing publishing to resource search index : "+resources.size();

        //def fieldsConfig = grails.util.Holders.config.speciesPortal.fields

        List<Map<String,Object>> eDocs=new ArrayList<Map<String,Object>>();

        Map docsMap = [:]

        resources.each { r ->
            log.debug "Reading Resource : "+r.id;
            Map<String,Object> doc = getDocument(r);

            eDocs.add(doc);
        }
        postToElastic(eDocs,"resource")
      //  return commitDocs(docs, commit);

    }

    /**
     */
    public Map<String,Object> getDocument(Resource r) {
        def searchFieldsConfig = grails.util.Holders.config.speciesPortal.searchFields
        Map<String,Object> doc=new HashMap<String,Object>();
        doc.put(searchFieldsConfig.ID, r.id.toString());
        doc.put(searchFieldsConfig.OBJECT_TYPE, r.class.simpleName);

//        doc.put(searchFieldsConfig.TITLE, r.fileName?:r.url);
        doc.put(searchFieldsConfig.RESOURCETYPE, r.type.value());
        doc.put(searchFieldsConfig.CONTEXT, r.context.value());

//        doc.put(searchFieldsConfig.AUTHOR, r.uploader.name);
//        doc.put(searchFieldsConfig.AUTHOR+"_id", r.uploader.id);
        r.contributors.each { contributor ->
            if(contributor.user) {
                doc.put(searchFieldsConfig.CONTRIBUTOR, contributor.user.name +" ### "+contributor.user.email +" "+contributor.user.username+" "+contributor.user.id.toString());
            } else {
                doc.put(searchFieldsConfig.CONTRIBUTOR, contributor.name);
            }
        }

        r.attributors.each { attributor ->
            doc.put(searchFieldsConfig.ATTRIBUTION, attributor.name);
        }

        doc.put(searchFieldsConfig.UPLOADED_ON, r.uploadTime);
//        r.licenses.each { license ->
            doc.put(searchFieldsConfig.LICENSE, r.license.name.name());
//        }

        if(r.description) {
            doc.put(searchFieldsConfig.MESSAGE, r.description);
        }

        String memberInfo = ""
        List allMembers = utilsServiceBean.getParticipants(r)
        allMembers.each { mem ->
			if(mem){
				memberInfo = mem.name + " ### " + mem.email +" "+ mem.username +" "+mem.id.toString()
				doc.put(searchFieldsConfig.MEMBERS, memberInfo);
			}
        }


        return doc

    }

    def delete(long id) {
        super.delete("resource",id.toString());
    }
}
