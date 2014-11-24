package speciespage.search

import static groovyx.net.http.ContentType.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.common.SolrException
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.params.TermsParams
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer
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
        /*if(!resources) return;
        log.info "Initializing publishing to resource search index : "+resources.size();

        //def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields

        Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        Map docsMap = [:]

        resources.each { r ->
            log.debug "Reading Resource : "+r.id;
            List ds = getSolrDocument(r);
            docs.addAll(ds);
        }

        return commitDocs(docs, commit);
        */
    }

    /**
     */
    public SolrInputDocument getSolrDocument(Resource r) {
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField(searchFieldsConfig.ID, r.class.simpleName +"_"+r.id.toString());
        doc.addField(searchFieldsConfig.OBJECT_TYPE, r.class.simpleName);

//        doc.addField(searchFieldsConfig.TITLE, r.fileName?:r.url);
        doc.addField(searchFieldsConfig.RESOURCETYPE, r.type.value());
        doc.addField(searchFieldsConfig.CONTEXT, r.context.value());

//        doc.addField(searchFieldsConfig.AUTHOR, r.uploader.name);
//        doc.addField(searchFieldsConfig.AUTHOR+"_id", r.uploader.id);
        r.contributors.each { contributor ->
            if(contributor.user) {
                doc.addField(searchFieldsConfig.CONTRIBUTOR, contributor.user.name +" ### "+contributor.user.email +" "+contributor.user.username+" "+contributor.user.id.toString());
            } else {
                doc.addField(searchFieldsConfig.CONTRIBUTOR, contributor.name);
            }
        }

        r.attributors.each { attributor ->
            doc.addField(searchFieldsConfig.ATTRIBUTION, attributor.name);
        }

        doc.addField(searchFieldsConfig.UPLOADED_ON, r.uploadTime);
        r.licenses.each { license ->
            doc.addField(searchFieldsConfig.LICENSE, license.name.name());
        }

        if(r.description) {
            doc.addField(searchFieldsConfig.MESSAGE, r.description);
        }

        String memberInfo = ""
        List allMembers = utilsServiceBean.getParticipants(r)
        allMembers.each { mem ->
            memberInfo = mem.name + " ### " + mem.email +" "+ mem.username +" "+mem.id.toString()
            doc.addField(searchFieldsConfig.MEMBERS, memberInfo);
        }


        return doc

    }
}
