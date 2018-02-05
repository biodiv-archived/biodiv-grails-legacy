package speciespage.search


import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map
import grails.converters.JSON;

import java.text.SimpleDateFormat;

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.common.SolrException
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.params.TermsParams
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer
import groovy.sql.Sql;
import species.CommonNames
import species.Habitat;
import species.NamesParser
import species.Synonyms
import species.TaxonomyDefinition
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.participation.Observation;
import species.participation.Checklists;
import species.participation.Recommendation;
import species.participation.RecommendationVote.ConfidenceType;
import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.io.WKTWriter;
import species.UtilsService
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import groovyx.net.http.ContentType
class ObservationsSearchService extends AbstractSearchService {

    static transactional = false
    def resourceSearchService;
    def dataSource

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
            List edoc=getJson(obv);
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
            doc.addField(searchFieldsConfig.LICENSE, obv.license.name.name());
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
            doc.addField(searchFieldsConfig.EXTERNAL_ID, obv.externalId);
            doc.addField(searchFieldsConfig.EXTERNAL_URL, obv.externalUrl);
            doc.addField(searchFieldsConfig.VIA_ID, obv.viaId);
            doc.addField(searchFieldsConfig.VIA_CODE, obv.viaCode);
            doc.addField(searchFieldsConfig.DATASET, obv.dataset?.id);
            doc.addField(searchFieldsConfig.ORIGINAL_AUTHOR, obv.originalAuthor);

            doc.addField(searchFieldsConfig.BASIS_OF_RECORD, obv.basisOfRecord);
            doc.addField(searchFieldsConfig.PROTOCOL, obv.protocol);

            String memberInfo = ""
            List allMembers = utilsServiceBean.getParticipants(obv)
            allMembers.each { mem ->
                if(mem == null) return;
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

            def checklistObvs;
            checklistObvs = addChecklistData(obv, doc)

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
                //doc.addField(searchFieldsConfig.CONTRIBUTOR, vote.author.name +" ### "+vote.author.email +" "+vote.author.username+" "+vote.author.id.toString());
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
        doc.addField(searchFieldsConfig.LICENSE, chk.license.name.name());
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

    def delete(long id) {
        super.delete(Observation.simpleName +"_"+id.toString());
    }


        /**
        * get JSON for Elastic
        */
        public List getJson(Observation obv) {
            def sql =  Sql.newInstance(dataSource);

            String query = """
            SELECT obs.id,
            obs.version,
            obs.author_id as authorId,
            su.name as authorName,
            su.profile_pic as authorProfilePic,
            obs.created_on as createdOn,
            obs.group_id as speciesGroupId,
            sg.name as speciesGroupName,
            obs.latitude,
            obs.longitude,
            obs.notes,
            obs.from_date as fromDate,
            obs.place_name as placeName,
            obs.rating,
            obs.reverse_geocoded_name as reverseGeocodedName,
            obs.flag_count as flagCount,
            obs.geo_privacy as geoPrivacy,
            obs.habitat_id as habitatId,
            h.name as habitatName,
            obs.is_deleted as isDeleted,
            obs.last_revised as lastRevised,
            obs.location_accuracy as locationAccuracy,
            obs.visit_count as visitCount,
            obs.search_text as searchText,
            obs.max_voted_reco_id as maxVotedrecoId,
            obs.agree_terms as agreeTerms,
            obs.is_checklist as isCheckList,
            obs.is_showable as isShowable,
            obs.source_id as sourceId,
            obs.to_date as toDate,
            obs.topology,
            obs.checklist_annotations as checklistAnnotations,
            obs.feature_count as featureCount,
            obs.is_locked as isLocked,
            obs.license_id as licenseId,
            ll.name as licenseName,
            obs.language_id as languageId,
            l.name as languageName,
            obs.location_scale as locationScale,
            obs.access_rights as accessRights,
            obs.catalog_number as catalogNumber,
            obs.dataset_id as datasetId,
            obs.external_dataset_key as externalDatasetKey,
            obs.external_id as externalId,
            obs.external_url as externalUrl,
            obs.information_withheld as informationWithHeld,
            obs.last_crawled as lastCrawled,
            obs.last_interpreted as lastInterpreted,
            obs.original_author as originalAuthor,
            obs.publishing_country as publishingCountry,
            obs.repr_image_id as reprImageId,
            obs.via_code viaCode,
            obs.via_id ViaId,
            obs.protocol,
    	      obs.traits,
            obs.basis_of_record as basisOfRecord,
            obs.no_of_images as noOfImages,
            obs.no_of_videos as noOfVideos,
            obs.no_of_audio as noOfAudio,
            obs.no_of_identifications as noOfIdentifications,
            r.name as name,
    	      r.taxon_concept_id as taxonConceptId,
    	      r.accepted_name_id as acceptednameId,
            t.canonical_form as taxonomyCanonicalForm,
    	      t.status,
    	      t.position,
    	      t.rank,
    	      tres.path as path,
    	      tres.classification_id as classificationId,
    	      resp.file_name as thumbnail,
            array_remove(array_agg(DISTINCT ug.id) ,null) as userGroupId,
            array_remove(array_agg(DISTINCT ug.name) ,null) as userGroupName,
    	      array_remove(array_agg(DISTINCT res.file_name),null ) as imageResource
         	  FROM observation obs
            LEFT JOIN language l ON obs.language_id = l.id
            LEFT JOIN suser su ON obs.author_id = su.id
            LEFT JOIN habitat h ON obs.habitat_id = h.id
            LEFT JOIN species_group sg ON obs.group_id = sg.id
            LEFT JOIN license ll ON obs.license_id = ll.id
            LEFT JOIN recommendation r ON obs.max_voted_reco_id = r.id
            LEFT JOIN taxonomy_definition t ON r.accepted_name_id = t.id
            LEFT JOIN user_group_observations ugo ON obs.id = ugo.observation_id
            LEFT JOIN user_group ug ON ug.id = ugo.user_group_id
    	      LEFT JOIN resource as resp ON obs.repr_image_id=resp.id
          	LEFT JOIN observation_resource obvres ON obs.id= obvres.observation_id
          	LEFT JOIN resource as res ON obvres.resource_id= res.id
          	LEFT JOIN taxonomy_registry as tres ON tres.taxon_definition_id = t.id
    	      where  (classification_id=265799 or classification_id is null) and obs.is_deleted =false and obs.id="""+obv.id+"""
            GROUP BY obs.id, su.name, su.profile_pic, sg.name, h.name, ll.name, l.name,
            r.name, t.canonical_form,res.file_name, r.taxon_concept_id,r.accepted_name_id,tres.path,tres.classification_id,t.status,t.position,t.rank,resp.file_name
            """;




            println "Running sql for getting observation json";

            def obvRows = sql.rows(query);
            obvRows.each { obvRow ->
              Map<String,Object> eData=new HashMap<String,Object>();
              obvRow.each { k, v ->
                if(k=="usergroupid"){

                  eData.put(k, v.getArray());
                }
                else if(k=="imageresource"){
                  eData.put(k, v.getArray());                }
                else if(k=="usergroupname"){

                  eData.put(k, v.getArray());
                }
                else if(k=="todate"){
                  print v.class
                  eData.put(k,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                }
                else if(k=="fromdate"){
                  print v.class
                  eData.put(k,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                }
                else if(k=="lastrevised"){
                  print v.class
                  eData.put(k,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                }
                else if(k=="createdon"){
                  print v.class
                  eData.put(k,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                }
                else{
                    eData.put(k, v.toString());
                }


              }

              def fromdate=obvRow.get("fromdate");
              def location="["+obvRow.get("longitude")+","+ obvRow.get("latitude")+"]";




              eData.put("location",location);
              eData.put("frommonth",new SimpleDateFormat("M").format(fromdate));


              def traits = obvRow.get("traits");
              if(traits) {
                  traits.each {traitDetails ->
                      println traitDetails;
                      traitDetails.getArray().each { t1 ->
                          String traitKey = "trait_"+t1[0];
                          if(!eData[traitKey]) eData[traitKey] = [];
                          t1.eachWithIndex { t2, index ->
                              println t2;
                              if(index > 0) {
                                  eData[traitKey] << t2
                              }
                          }
                      }
                  }
              }
              eData.remove('traits');
              postToElastic(eData)
            }

        }


    void postToElastic(Map doc) {
      if(!doc) return;
      println doc
        def searchConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.search
        def http = new HTTPBuilder('http://localhost:8081')
        http.request(POST, groovyx.net.http.ContentType.JSON) {
            uri.path = "/biodiv-api/naksha/observation/observations/${doc.id}";
            body = doc

            response.success = { resp, reader ->
                log.debug "Successfully posted observation ${doc.id} to elastic"
            }
            response.'404' = {
              println 'Not found';
              log.debug "Error in posting observation to elastic : Not found";
            }
        }
    }

}
