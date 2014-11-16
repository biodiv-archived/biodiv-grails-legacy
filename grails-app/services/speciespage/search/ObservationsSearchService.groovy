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
import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.io.WKTWriter;
import species.UtilsService

class ObservationsSearchService extends AbstractSearchService {

    static transactional = false
    def resourceSearchService;

    /**
     * 
     */
    def publishSearchIndex() {
        log.info "Initializing publishing to observations search index"

        //TODO: change limit
        int limit = BATCH_SIZE//Observation.count()+1, 
        int offset = 0;
        int noIndexed = 0;

        def observations;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:Observation.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            Observation.withNewTransaction([readOnly:true]) { status ->
                observations = Observation.findAllByIsShowableAndIsDeleted(true, false, [max:limit, offset:offset, sort:'id',readOnly:true]);
                noIndexed += observations.size()
                if(!observations) return;
                publishSearchIndex(observations, true);
                //observations.clear();
                offset += limit;
                cleanUpGorm()
            }

            if(!observations) break;
            observations.clear();
        }

        log.info "Time taken to publish observations search index is ${System.currentTimeMillis()-startTime}(msec)";
    }

    /**
     * @param species
     * @return
     */
    def publishSearchIndex(List<Observation> obvs, boolean commit) {
        if(!obvs) return;
        log.info "Initializing publishing to observations search index : "+obvs.size();

        //def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields

        Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        Map names = [:];
        Map docsMap = [:]

        obvs.each { obv ->
            log.debug "Reading Observation : "+obv.id;
            List ds = getSolrDocument(obv);
            docs.addAll(ds);
        }

        return commitDocs(docs, commit);
    }

    /**
     */
    public List<SolrInputDocument> getSolrDocument(Observation obv) {
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        List docs = [];
        if(!obv.isDeleted) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.setDocumentBoost(1.5);
            doc.addField(searchFieldsConfig.ID, obv.class.simpleName +"_"+obv.id.toString());
            doc.addField(searchFieldsConfig.OBJECT_TYPE, obv.class.simpleName);
            addNameToDoc(obv, doc);

            //doc.addField(searchFieldsConfig.TITLE, obv.fetchSpeciesCall());
            //doc.addField(searchFieldsConfig.AUTHOR, obv.author.name);
            //doc.addField(searchFieldsConfig.AUTHOR+"_id", obv.author.id);
            doc.addField(searchFieldsConfig.CONTRIBUTOR, obv.author.name +" ### "+obv.author.email +" "+obv.author.username+" "+obv.author.id.toString());

            doc.addField(searchFieldsConfig.FROM_DATE, obv.fromDate);
            doc.addField(searchFieldsConfig.TO_DATE, obv.toDate);

            doc.addField(searchFieldsConfig.OBSERVED_ON, obv.fromDate);
            doc.addField(searchFieldsConfig.UPLOADED_ON, obv.createdOn);
            doc.addField(searchFieldsConfig.UPDATED_ON, obv.lastRevised);
            doc.addField(searchFieldsConfig.LICENSE, obv.license.name.value());
            if(obv.notes) {
                doc.addField(searchFieldsConfig.MESSAGE, obv.notes);
            }

            doc.addField(searchFieldsConfig.SGROUP, obv.group.id);			
            doc.addField(searchFieldsConfig.HABITAT, obv.habitat.id);
            doc.addField(searchFieldsConfig.LOCATION, obv.placeName);
            doc.addField(searchFieldsConfig.LOCATION, obv.reverseGeocodedName);
            doc.addField(searchFieldsConfig.ISFLAGGED, (obv.flagCount > 0));

            def topology = obv.topology;
            doc.addField(searchFieldsConfig.LATLONG, obv.latitude+","+obv.longitude);

            WKTWriter wkt = new WKTWriter();
            try {
                String geomStr = wkt.write(obv.topology);
                doc.addField(searchFieldsConfig.TOPOLOGY, geomStr);
            } catch(Exception e) {
                log.error "Error writing polygon wkt : ${observationInstance}"
            }

            doc.addField(searchFieldsConfig.IS_CHECKLIST, obv.isChecklist);
            doc.addField(searchFieldsConfig.IS_SHOWABLE, obv.isShowable);
            doc.addField(searchFieldsConfig.SOURCE_ID, obv.sourceId);

            String memberInfo = ""
            List allMembers = utilsServiceBean.getParticipants(obv)
            allMembers.each { mem ->
                memberInfo = mem.name + " ### " + mem.email +" "+ mem.username +" "+mem.id.toString()
                doc.addField(searchFieldsConfig.MEMBERS, memberInfo);
            }

            //boolean geoPrivacy = false;
            //String locationAccuracy;
            obv.tags.each { tag ->
                doc.addField(searchFieldsConfig.TAG, tag);
            }

            obv.userGroups.each { userGroup ->
                doc.addField(searchFieldsConfig.USER_GROUP, userGroup.id);
                doc.addField(searchFieldsConfig.USER_GROUP_WEBADDRESS, userGroup.webaddress);
            }

            def checklistObvs = addChecklistData(obv, doc)
            
            List resourceDocs = getResourcesDocs(obv);

            docs.add(doc);
            docs.addAll(checklistObvs);
            docs.addAll(resourceDocs);
        }
        return docs

    }

    /**
     * @param doc
     * @param name
     */
    private void addNameToDoc(Observation obv, SolrInputDocument doc) {
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        doc.addField(searchFieldsConfig.MAX_VOTED_SPECIES_NAME, obv.fetchSpeciesCall());

        def recoVotes = obv.recommendationVote
        def distRecoVotes;
        if(recoVotes) {
            //distRecoVotes = recoVotes.unique { it.recommendation };
                distRecoVotes = obv.recommendationVote?.unique { it.recommendation };
            //distRecoVotes = obv.maxVotedReco;
            distRecoVotes.each { vote ->
                doc.addField(searchFieldsConfig.NAME, vote.recommendation.name);
                doc.addField(searchFieldsConfig.CONTRIBUTOR, vote.author.name +" ### "+vote.author.email +" "+vote.author.username+" "+vote.author.id.toString());
                if(vote.recommendation.taxonConcept)
                    doc.addField(searchFieldsConfig.CANONICAL_NAME, vote.recommendation.taxonConcept.canonicalForm);
            }
        }
        
    }

    /**
     */	
    private List<SolrInputDocument> addChecklistData(Observation obv, SolrInputDocument doc){
        if(!obv.isChecklist) return []

            def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
            def chk = obv 

            doc.addField(searchFieldsConfig.TITLE, chk.title);
        doc.addField(searchFieldsConfig.LICENSE, chk.license.name.value());
        doc.removeField(searchFieldsConfig.UPLOADED_ON);
        doc.addField(searchFieldsConfig.UPLOADED_ON, chk.publicationDate?:chk.createdOn);
        doc.addField(searchFieldsConfig.REFERENCE, chk.refText);
        doc.addField(searchFieldsConfig.SOURCE_TEXT, chk.sourceText);

        chk.contributors.each { s ->
            String userInfo = ""
            if(s.user) {
                userInfo = " ### "+s.user.email+" "+s.user.username+" "+s.user.id.toString()
            }
            doc.addField(searchFieldsConfig.CONTRIBUTOR, s.name + userInfo);
        }
        chk.attributions.each { s ->
            doc.addField(searchFieldsConfig.ATTRIBUTION, s.name);
        }

        chk.states.each { s->
            doc.addField(searchFieldsConfig.LOCATION, s);
        }
        chk.districts.each { s->
            doc.addField(searchFieldsConfig.LOCATION, s);
        }
        chk.talukas.each { s->
            doc.addField(searchFieldsConfig.LOCATION, s);
        }

        def docs = [];
        chk.observations.each { row ->
            //addNameToDoc(row, doc)
            Observation chk_obv = Observation.read(row.id);
            def d = getSolrDocument(chk_obv);
            if(d) {
                d[0].addField(searchFieldsConfig.TITLE, chk.title);
                docs.addAll(d);
            }

            def r = getResourcesDocs(chk_obv);
            if(r) {
                r.each {
                    it.addField(searchFieldsConfig.CONTAINER, chk_obv.class.simpleName +"_"+chk_obv.id.toString());
                }
                docs.addAll(r);
            }
        }
        return docs;
    }

    List getResourcesDocs(Observation obv) {
        def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        List resourcesDocs = [];
        SolrInputDocument resourceDoc;
        obv.resource.each { resource ->
            resourceDoc =  resourceSearchService.getSolrDocument(resource);
            if(resourceDoc) {
                resourceDoc.addField(searchFieldsConfig.NAME, obv.fetchSpeciesCall());
                resourceDoc.addField(searchFieldsConfig.CONTAINER, obv.class.simpleName +"_"+obv.id.toString());
                resourcesDocs << resourceDoc
            }
        }
        return resourcesDocs;
    }
}
