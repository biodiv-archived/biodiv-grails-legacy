package speciespage.search
import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map
import grails.converters.JSON;

import java.text.SimpleDateFormat;

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
import groovyx.net.http.Method
// import static groovyx.net.http.ContentType.TEXT
//import groovyx.net.http.ContentType.JSON
import groovyx.net.http.ContentType
class ObservationsSearchService extends AbstractSearchService {

    static transactional = false
    def dataSource

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

    def publishSearchIndex(List<Observation> obvs, boolean commit) {
        if(!obvs) return;
        log.info "Initializing publishing to observations search index : "+obvs.size();

        //def fieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.fields

        List docs = new ArrayList();
        Map names = [:];
        Map docsMap = [:]

        obvs.each { obv ->
            log.debug "Reading Observation : "+obv.id;
            Map edoc=getJson(obv);
            postToElastic(edoc)
//            docs.addAll(edoc);
        }

//        return commitDocs(docs, commit);
    }

    def delete(long id) {
        super.delete(Observation.simpleName +"_"+id.toString());
    }

    List getJson(Observation obv) {
        def sql =  Sql.newInstance(dataSource);

        String query = """
        SELECT obs.id,
      obs.version,
      obs.author_id AS authorid,
      su.name AS authorname,
      case when su.icon is null then su.profile_pic else su.icon end  as authorprofilepic,
      obs.created_on AS createdon,
      obs.group_id AS speciesgroupid,
      sg.name AS speciesgroupname,
      obs.latitude,
      obs.longitude,
      obs.notes,
      obs.from_date AS fromdate,
      obs.place_name AS placename,
      obs.rating,
      obs.reverse_geocoded_name AS reversegeocodedname,
      obs.flag_count AS flagcount,
      obs.geo_privacy AS geoprivacy,
      obs.habitat_id AS habitatid,
      h.name AS habitatname,
      obs.is_deleted AS isdeleted,
      obs.last_revised AS lastrevised,
      obs.location_accuracy AS locationaccuracy,
      obs.visit_count AS visitcount,
      obs.search_text AS searchtext,
      obs.max_voted_reco_id AS maxvotedrecoid,
      obs.agree_terms AS agreeterms,
      obs.is_checklist AS ischecklist,
      obs.is_showable AS isshowable,
      obs.source_id AS sourceid,
      obs.to_date AS todate,
      obs.topology,
      obs.checklist_annotations AS checklistannotations,
      obs.feature_count AS featurecount,
      obs.is_locked AS islocked,
      obs.license_id AS licenseid,
      ll.name AS licensename,
      obs.language_id AS languageid,
      l.name AS languagename,
      obs.location_scale AS locationscale,
      obs.access_rights AS accessrights,
      obs.catalog_number AS catalognumber,
      obs.dataset_id AS datasetid,
      obs.external_dataset_key AS externaldatasetkey,
      obs.external_id AS externalid,
      obs.external_url AS externalurl,
      obs.information_withheld AS informationwithheld,
      obs.last_crawled AS lastcrawled,
      obs.last_interpreted AS lastinterpreted,
      obs.original_author AS originalauthor,
      obs.publishing_country AS publishingcountry,
      obs.repr_image_id AS reprimageid,
      obs.via_code AS viacode,
      obs.via_id AS viaid,
      obs.protocol,
      obs.traits,
      obs.basis_of_record AS basisofrecord,
      obs.no_of_images AS noofimages,
      obs.no_of_videos AS noofvideos,
      obs.no_of_audio AS noofaudio,
      obs.no_of_identifications AS noofidentifications,
      r.name,
      r.taxon_concept_id AS taxonconceptid,
      r.accepted_name_id AS acceptednameid,
      t.canonical_form AS taxonomycanonicalform,
      t.status,
      t."position",
      t.rank,
      tres.path,
      tres.classification_id AS classificationid,
      resp.file_name AS thumbnail,
      array_remove(array_agg(DISTINCT ug.id), NULL::bigint) AS usergroupid,
      array_remove(array_agg(DISTINCT ug.name), NULL::character varying) AS usergroupname,
      array_remove(array_agg(DISTINCT res.file_name), NULL::character varying) AS imageresource,
      array_remove(array_agg(DISTINCT res.url), NULL::character varying) AS urlresource
     FROM observation obs
     LEFT JOIN observation_resource obvres ON obs.id = obvres.observation_id
     LEFT JOIN resource resp ON obs.repr_image_id = resp.id
     LEFT JOIN resource res ON obvres.resource_id = res.id
     LEFT JOIN language l ON obs.language_id = l.id
     LEFT JOIN suser su ON obs.author_id = su.id
     LEFT JOIN habitat h ON obs.habitat_id = h.id
     LEFT JOIN species_group sg ON obs.group_id = sg.id
     LEFT JOIN license ll ON obs.license_id = ll.id
     LEFT JOIN recommendation r ON obs.max_voted_reco_id = r.id
     LEFT JOIN taxonomy_definition t ON r.accepted_name_id = t.id
     LEFT JOIN user_group_observations ugo ON obs.id = ugo.observation_id
     LEFT JOIN user_group ug ON ug.id = ugo.user_group_id
     LEFT JOIN taxonomy_registry tres ON tres.taxon_definition_id = t.id
    WHERE (tres.classification_id = 265799 OR tres.classification_id IS NULL) and obs.is_deleted=false and obs.id="""+obv.id+"""
    GROUP BY obs.id, su.name, su.icon, su.profile_pic, sg.name, h.name, ll.name, l.name, r.name, t.canonical_form, r.taxon_concept_id, r.accepted_name_id, tres.path
  , tres.classification_id, t.status, t."position", t.rank, resp.file_name """;

        println "Running sql for getting observation json";

        def obvRows = sql.rows(query);
        obvRows.each { obvRow ->
            Map<String,Object> eData=new HashMap<String,Object>();
            obvRow.each { k, v ->
                if(k=="usergroupid"){
                    eData.put(k, v.getArray());
                }
                else if(k=="imageresource"){
                    eData.put(k, v.getArray());
                  }
                else if(k=="usergroupname"){
                    eData.put(k, v.getArray());
                 }
                 else if(k==" urlresource"){
                   eData.put(k,v.getArray());
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
def geoPrivacyAdjust = Utils.getRandomFloat()
def longitude = obvRow.get("longitude") + geoPrivacyAdjust
def latitude = obvRow.get("latitude") + geoPrivacyAdjust
def location="["+longitude+","+ latitude+"]";




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
return eData;
}

}


    void postToElastic(Map doc) {
      if(!doc) return;
      println doc
        def searchConfig = grailsApplication.config.speciesPortal
      def URL=searchConfig.search.nakshaURL;

        def http = new HTTPBuilder(URL)
        http.request(Method.POST, groovyx.net.http.ContentType.JSON) {
            uri.path = "/biodiv-api/naksha/observation/observation/${doc.id}";
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
