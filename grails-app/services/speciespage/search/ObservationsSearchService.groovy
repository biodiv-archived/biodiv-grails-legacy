package speciespage.search
import java.text.SimpleDateFormat
import java.util.Date;
import java.util.List
import java.util.Map
import grails.converters.JSON;

import java.text.SimpleDateFormat;
import org.codehaus.groovy.grails.web.json.JSONArray;
import groovy.sql.Sql;
import groovy.json.JsonSlurper
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
import species.utils.Utils;

import org.codehaus.groovy.grails.web.json.JSONObject;

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
    def customFieldService

    def publishSearchIndex() {
        log.info "Initializing publishing to observations search index"

        //TODO: change limit
        int limit = BATCH_SIZE //Observation.count()+1,
        int offset = 0;
        int noIndexed = 0;

        def observations;
        def startTime = System.currentTimeMillis()
        INDEX_DOCS = INDEX_DOCS != -1?INDEX_DOCS:Observation.count()+1;
        if(limit > INDEX_DOCS) limit = INDEX_DOCS
        while(noIndexed < INDEX_DOCS) {
            Observation.withNewTransaction([readOnly:true]) { status ->
                observations = Observation.findAllByIsDeleted(false, [max:limit, offset:offset, sort:'id',readOnly:true]);
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
        List<Long> ids=new ArrayList<Long>();
        Map<String,Map<String,Object>> customFieldMapToObjectId=new HashMap<String,Map<String,Object>>();

        obvs.each { obv ->
            log.debug "Reading Observation : "+obv.id;
            ids.push(obv.id);
            Map cf=customFieldService.fetchAllCustomFields(obv);
            println  cf
            customFieldMapToObjectId.put(obv.id.toString(),cf);
        }
        List<Map<String,Object>> dataToElastic=new ArrayList<Map<String,Object>>();
         dataToElastic=getJson(ids,customFieldMapToObjectId);
         println customFieldMapToObjectId
         postToElastic(dataToElastic);
//        return commitDocs(docs, commit);
    }

    def delete(long id) {
        super.delete("observation",id.toString());
    }

    def getJson(List<Long> ids,Map<String,Map<String,String>> customFieldMapToObjectId) {
        def sql =  Sql.newInstance(dataSource);
          def sids=ids.join(",")
        String query = """
        SELECT obs.id,
            obs.version,
            obs.author_id AS authorid,
            su.name AS authorname,
                CASE
                    WHEN su.icon IS NULL THEN su.profile_pic
                    ELSE su.icon
                END AS authorprofilepic,
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
            obs.basis_of_record AS basisofrecord,
            obs.no_of_images AS noofimages,
            obs.no_of_videos AS noofvideos,
            obs.no_of_audio AS noofaudio,
            obs.no_of_identifications AS noofidentifications,
            r.taxon_concept_id AS taxonconceptid,
            r.accepted_name_id AS acceptednameid,
            t.canonical_form AS taxonomycanonicalform,
            t.status,
           CASE
                    WHEN t. normalized_form IS NULL THEN r.name
                    ELSE  t. normalized_form
                END AS name,
            t.position,
            t.rank,
            resp.file_name AS thumbnail,
            array_remove(array_agg(DISTINCT ug.id), NULL::bigint) AS usergroupid,
            array_remove(array_agg(DISTINCT ug.name), NULL::character varying) AS usergroupname,
            array_remove(array_agg(DISTINCT res.file_name), NULL::character varying) AS imageresource,
            array_remove(array_agg(DISTINCT res.url), NULL::character varying) AS urlresource,
            array_remove(array_agg(DISTINCT f.user_group_id), NULL::bigint) AS featuredgroups,
            array_remove(array_agg(DISTINCT f.notes), NULL::character varying) AS featurednotes
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
           LEFT JOIN taxonomy_definition t ON r.taxon_concept_id = t.id
           LEFT JOIN user_group_observations ugo ON obs.id = ugo.observation_id
           LEFT JOIN user_group ug ON ug.id = ugo.user_group_id
           LEFT JOIN featured f ON f.object_id = obs.id
           WHERE obs.is_deleted=false and obs.id in  ( """+sids+""" )
          GROUP BY obs.id, su.name, su.icon, su.profile_pic, sg.name, h.name, ll.name, l.name, t.canonical_form,t.normalized_form , r.name,r.taxon_concept_id,
          r.accepted_name_id,t.status, t.position, t.rank, resp.file_name  """;

          println "Running sql for getting observation json";

          println query;

          String customFieldTableQuery="""select table_name from information_schema.tables where table_name like 'custom_fields_group%' """;
          def customRows=sql.rows(customFieldTableQuery);
          List<String> customFieldUserGroupArray=new ArrayList<String>();
          customRows.each{ customRow ->
              customRow.each { k,v ->
              customFieldUserGroupArray.add(v.split("_")[3].toString());
              }
          }


          /*
          Query for observation likes
          */

          String queryForObservationLike="""
            select row.rating_ref as key,  '[' || string_agg(format('%s',to_json(row_to_json(row)) ), ',')  || ']' as values from (select  rl.rating_ref,r.rater_id ,su.name , case when su.icon is not null  then su.icon else coalesce(su.profile_pic,'') end as icon
            from rating as r inner join suser  as su on r.rater_id=su.id inner join  rating_link as rl on r.id=rl.rating_id
            where rl.rating_ref in ("""+sids+ """) and  rl.type='observation') row group by row.rating_ref""";

            def queryForObservationLikeResult=sql.rows(queryForObservationLike);


            Map<String,Object> observationLike =new HashMap<String,Object>();

            queryForObservationLikeResult.each { row ->
              def key;
              def value;
               row.each{ k,v->
                 if(k.equalsIgnoreCase("key")){
                    key=v.toString();
                 }
                 if(k.equalsIgnoreCase("values")){
                  JSONArray array = new JSONArray(v.toString());
                  observationLike.put(key,array);

                 }
              }
              }




          /*
          Query for path and classificationid
          */

          String queryForPathAndClassification="""select obv.id,tres.path,tres.classification_id as classificationid
                                                  from observation obv
                                                   left join recommendation r on obv.max_voted_reco_id=r.id
                                                   left join taxonomy_definition t on r.taxon_concept_id=t.id
                                                   left join taxonomy_registry tres on tres.taxon_definition_id=t.id
                                                    where (tres.classification_id=6 or tres.classification_id=null)
                                                    and obv.id in ( """+sids+""" )""";


          def queryForPathAndClassificationResult=sql.rows(queryForPathAndClassification);

          Map<String,Map<String,String>> pathClassification=new HashMap<String,Map<String,String>>();



          queryForPathAndClassificationResult.each { row ->
              def id;
              Map<String,String> pathClass=new HashMap<String,String>();
              row.each { k,v ->
                if(k!="id"){
                  pathClass.put(k,v.toString());
                }
                else{
                  id=v.toString();
                }
          }
          pathClassification.put(id.toString(),pathClass);
          }

        def obvRows = sql.rows(query);
        List<Map<String,Object>> dataToElastic=new ArrayList<Map<String,Object>>();
        obvRows.each { obvRow ->
            Map<String,Object> eData=new HashMap<String,Object>();

            obvRow.each { k, v ->
                if(k=="usergroupid"){
                    if(v!=null)
                    eData.put(k, v.getArray());
                }
                else if(k=="imageresource"){
                    if(v!=null)
                    eData.put(k, v.getArray());
                  }
                else if(k=="usergroupname"){
                  if(v!=null)
                    eData.put(k, v.getArray());
                 }
                 else if(k=="urlresource"){
                   if(v!=null)
                   eData.put(k,v.getArray());
                 }
                 else if(k=="featurednotes"){
                   if(v!=null)
                   eData.put(k,v.getArray())
                 }
                 else if(k=="featuredgroups"){
                   if(v!=null)
                   eData.put(k,v.getArray())
                 }
                else if(k=="todate"){
                  println v.getClass()
                  if(v!=null)
                    eData.put(k,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                }
                else if(k=="fromdate"){
                    println v.getClass()
                    if(v!=null)
                    eData.put(k,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                }
                else if(k=="lastrevised"){
                    println v.getClass()
                    if(v!=null)
                    eData.put(k,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                }
                else if(k=="createdon"){
                      println v.getClass()
                      if(v!=null)
                    eData.put(k,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                }
                else{
                  if(v!=null)
                    eData.put(k, v.toString());
                }
              }

def fromdate=obvRow.get("fromdate");

def geoprivacy=obvRow.get("geoprivacy");
List<Double> location=new ArrayList<Double>();

if(geoprivacy){
  def geoPrivacyAdjust = Utils.getRandomFloat()
  def longitude = obvRow.get("longitude") + geoPrivacyAdjust
  def latitude = obvRow.get("latitude") + geoPrivacyAdjust
  location.add(longitude);
  location.add(latitude);
}
else{
  def longitude = obvRow.get("longitude")
  def latitude = obvRow.get("latitude")
  location.add(longitude);
  location.add(latitude);
}


eData.put("location",location);
eData.put("frommonth",new SimpleDateFormat("M").format(fromdate));
eData.put("checklistannotations",null);

def id=obvRow.get("id");

eData.put("observationlikes",observationLike.get(id.toString()));

/**
Observation classificationid and path related data
*/
Map<String, String> pathclassData=new HashMap<String,String>();
pathclassData=pathClassification.get(id.toString());
if(pathclassData!=null){
  eData.put("path",pathclassData.get("path"));
  eData.put("classificationid",pathclassData.get("classificationid"));
}
else{
  eData.put("path",null);
  eData.put("classificationid",null);
}



/*
NO media filter
*/
def noofimages=eData.get("noofimages")
def noofvideos=eData.get("noofvideos")
def noofaudio=eData.get("noofaudio")
if(noofaudio.equalsIgnoreCase("0") && noofimages.equalsIgnoreCase("0") && noofvideos.equalsIgnoreCase("0")){
  eData.put("nomedia","1");
}
else{
  eData.put("nomedia","0");
}
//no media thing finishes here

/*
varibale declaration for traits and custom fields
*/

Map<String, Object> traits=new HashMap<String,Object>();
Map<String, Object> traits_json=new HashMap<String,Object>();
Map<String, Object> traits_season=new HashMap<String,Object>();
Map<String, Object> custom_fields=new HashMap<String,Object>();


/**
query for custom fields if exists for a particular userGroup
*/



 Map<String,Object> customFieldMap=customFieldMapToObjectId.get(id.toString())
 customFieldMap.each{ k,v ->
   Map<String, Object> keyValues=new HashMap<String,Object>();
  def customid=v.get("id")
  def values;
  def key=v.get("key")
  def allowedMultiple=v.get("allowedMultiple");
  def options=v.get("options")
  def dataType=v.get("dataType").toString()
  def valueDate=v.get("valueDate");
  if(allowedMultiple && options!=null){
    if(v.get("value")!="" && v.get("value")!=null){
      keyValues.put("value",v.get("value").split(","));
      keyValues.put("key",key);
      custom_fields.put(customid.toString(),keyValues);
    }
  }
  else{
    if(valueDate!=null){
        keyValues.put("value",valueDate);
        keyValues.put("key",key);
        custom_fields.put(customid.toString(),keyValues);
    }
    else{
      if(v.get("value")!="" && v.get("value")!=null){
        keyValues.put("value",v.get("value"));
        keyValues.put("key",key);
        custom_fields.put(customid.toString(),keyValues);

      }
    }
  }

}




/*
for key value type of traits
*/


String traitKeyValueQuery =
 """
 select g.traits from (
 	select
 	 x.object_id,  '[' || string_agg(format('{%s:%s}', to_json(x.tid), to_json(x.tvalues)), ',') || ']'  as traits
 	from (
 	select f.object_id, t.id as tid ,json_agg(DISTINCT tv.value) AS tvalues from fact f, trait t, trait_value tv
        where f.trait_instance_id = t.id and f.trait_value_id = tv.id and f.object_type='species.participation.Observation'  group by f.object_id,t.id
 	) x group by x.object_id
 	) g where g.object_id="""+id;

   def traitKeyValue = sql.rows(traitKeyValueQuery);

traitKeyValue.each { rows ->
  rows.each{ row ->
    JSONArray array = new JSONArray(row.getValue().toString());
for(int i=0; i<array.length(); i++){
    JSONObject jsonObj = array.getJSONObject(i);
    jsonObj.each{ k,v ->
            String key="trait_"+k;
                traits.put(key,v);
                }
                                    }
          }

  }
/*
For Numeric Range Query
*/

  String traitRangeNumericQuery=
  """
  select g.traits from (
	select
	 x.object_id,  '[' || string_agg(format('{%s:%s}', to_json(x.tid), to_json(x.tvalues)), ',') || ']' as traits
	from (
	select  f.object_id, t.id as tid,ARRAY[f.value,f.to_value] as tvalues from fact f, trait t
	where f.trait_instance_id = t.id and (t.data_types='NUMERIC') and f.object_type='species.participation.Observation'
) x group by x.object_id
) g where g.object_id="""+id;
  def traitrangeNumeric=sql.rows(traitRangeNumericQuery);

  traitrangeNumeric.each { rows ->
    rows.each{ row ->
      JSONArray array = new JSONArray(row.getValue().toString());
  for(int i=0; i<array.length(); i++){
      JSONObject jsonObj = array.getJSONObject(i);
      jsonObj.each{ k,v ->
              String key="trait_"+k;
                  traits.put(key,v);
                  }
              }
            }

    }

  String traitColorQuery=
  """
  select g.traits from (
	select
	 x.object_id,  '[' || string_agg(format('{%s:%s}', to_json(x.tid), to_json(x.tvalues)), ',') || ']'  as traits
	 from (
	select f.object_id,t.id as tid,  array_agg(DISTINCT f.value) AS tvalues from fact f, trait t
	 where  f.trait_instance_id = t.id and (t.data_types='COLOR')  and f.object_type='species.participation.Observation'  group by f.object_id,t.id
) x group by x.object_id
) g where g.object_id="""+id;
  def traitColor=sql.rows(traitColorQuery);
  traitColor.each { rows ->
    rows.each{ row ->
      JSONArray array = new JSONArray(row.getValue().toString());
  for(int i=0; i<array.length(); i++){
      JSONObject jsonObj = array.getJSONObject(i);
      println jsonObj;
      jsonObj.each{ k,v ->
              String key="trait_"+k;
              List<Map<String,Object>> ColorArray=new ArrayList<Map<String,Object>>();
                    for(int j=0;j<v.length();j++){
                      String newrgb=v[j].trim().substring(4,v[j].length()-1);
			                String[] items =newrgb.split(",");
                      List<Integer> rgbList=new ArrayList<Integer>();
                			for(String x:items){
                				rgbList.add(Integer.parseInt(x));
                			}
                      Map hsbvals=[:];
                			 hsbvals=rgbToHSV(rgbList.get(0), rgbList.get(1), rgbList.get(2));
                			Map<String,Object> hslMap=new HashMap<String,Object>();
                			hslMap.put("h", hsbvals.get("hue"));
                			hslMap.put("s", hsbvals.get("saturation"));
                			hslMap.put("l", hsbvals.get("value"));
                      ColorArray.add(hslMap);
                    }
                    traits_json.put(key,ColorArray);
                  }
                }
            }

    }

    /*tRaits for datequery
    */

  String traitDateQuery=
  """
  select g.traits from (
	select
	 x.object_id,  '[' || string_agg(format('{%s:%s}', to_json(x.tid), to_json(x.dates)), ',') || ']' as traits
	 from (
	select f.object_id,t.id as tid, ARRAY[f.from_date,f.to_date] as dates from fact f, trait t
	 where f.trait_instance_id = t.id and (t.data_types='DATE') and (t.units!='MONTH') and f.object_type='species.participation.Observation'
	) x group by x.object_id
	) g where g.object_id="""+id;
  def traitDate=sql.rows(traitDateQuery);

  traitDate.each { rows ->
    rows.each{ row ->
      JSONArray array = new JSONArray(row.getValue().toString());
  for(int i=0; i<array.length(); i++){
      JSONObject jsonObj = array.getJSONObject(i);
      jsonObj.each{ k,v ->
              String key="trait_"+k;
                  traits.put(key,new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(v));
                  }
              }
            }

    }

    String traitSeasonDateQuery=
    """
    select g.traits from (
  	select
  	 x.object_id,  '[' || string_agg(format('{%s:%s}', to_json(x.tid), to_json(x.dates)), ',') || ']' as traits
  	 from (
  	select f.object_id,t.id as tid, ARRAY[f.from_date,f.to_date] as dates from fact f, trait t
  	 where f.trait_instance_id = t.id and (t.data_types='DATE') and (t.units='MONTH')  and f.object_type='species.participation.Observation'
  	) x group by x.object_id
  	) g where g.object_id="""+id;
    def traitSeasonDate=sql.rows(traitSeasonDateQuery);

    traitSeasonDate.each { rows ->
      rows.each{ row ->
        JSONArray array = new JSONArray(row.getValue().toString());
    for(int i=0; i<array.length(); i++){
        JSONObject jsonObj = array.getJSONObject(i);
        jsonObj.each{ k,v ->
                String key="trait_"+k;
                  Map<String,Object> dates=new HashMap<String,Object>();
                    dates.put("gte",v[0]);
                    dates.put("lte",v[1]);
                    traits_season.put(key,dates);
                    }
                }
              }

      }

/**
put traits and custom field data in elstic seacrh
*/
eData.put("traits",traits);
eData.put("traits_json",traits_json);
eData.put("traits_season",traits_season);
eData.put("custom_fields",custom_fields);


/*
put data in list of map for elasticseacrh
*/
dataToElastic.add(eData);
}

return dataToElastic;
}

def rgbToHSV(red, green, blue) {
    float r = red / 255f
    float g = green / 255f
    float b = blue / 255f
    float max = [r, g, b].max()
    float min = [r, g, b].min()
    float delta = max - min
    def hue = 0
    def saturation = 0
    if (max == min) {
        hue = 0
    } else if (max == r) {
        def h1 = (g - b) / delta / 6
        def h2 = h1.asType(int)
        if (h1 < 0) {
            hue = (360 * (1 + h1 - h2)).round()
        } else {
            hue = (360 * (h1 - h2)).round()
        }
        log.trace("rgbToHSV: red max=${max} min=${min} delta=${delta} h1=${h1} h2=${h2} hue=${hue}")
    } else if (max == g) {
        hue = 60 * ((b - r) / delta + 2)
        log.trace("rgbToHSV: green hue=${hue}")
    } else {
        hue = 60 * ((r - g) / (max - min) + 4)
        log.trace("rgbToHSV: blue hue=${hue}")
    }

    if (max == 0) {
        saturation = 0
    } else {
        saturation = delta / max * 100
    }

    def value = max * 100

    return [
        "red": red.asType(int),
        "green": green.asType(int),
        "blue": blue.asType(int),
        "hue": hue.asType(int),
        "saturation": saturation.asType(int),
        "value": value.asType(int),
    ]
}

    void postToElastic(List<Map<String,Object>> doc) {
      if(!doc) return;

        def searchConfig = grailsApplication.config.speciesPortal
      def URL=searchConfig.search.nakshaURL;

        def http = new HTTPBuilder(URL)
        http.request(Method.POST, groovyx.net.http.ContentType.JSON) {
            uri.path = "/naksha/services/bulk-upload/observation/observation";
            body = doc
            response.success = { resp, reader ->
                log.debug "Successfully posted observation  to elastic"
            }
            response.failure = { resp ->  log.error 'Request failed : '+resp }
        }
    }

}
