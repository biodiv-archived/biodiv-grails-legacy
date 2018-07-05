package species.dataset

import content.eml.UFile;
import content.eml.Document;
import species.Species;
import species.SpeciesField;
import species.dataset.Dataset.DatasetType;
import species.participation.Observation;
import species.trait.Trait;
import content.eml.UFile;
import species.dataset.DataPackage.DataTableType;
import species.groups.UserGroup;
import grails.converters.JSON
import species.participation.UploadLog;
import speciespage.ObvUtilService;

import species.License
import species.trait.Fact;
import species.trait.Trait;
import groovy.sql.Sql
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;


import content.eml.Document;
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@JsonIgnoreProperties([])
class DataTable extends CollMetadata {
	
	
//	DataTableType type;
	boolean agreeTerms = false;
	static hasOne = [dataset : Dataset1]
    UFile uFile;
    UFile traitValueFile;
    UFile imagesFile;
    DataTableType dataTableType;

    UploadLog uploadLog;

	//serialized object to store list of column names
	String columns;
    Long checklistId;
    static hasMany = [userGroups:UserGroup];
	static belongsTo = [UserGroup]
	
    boolean isMarkingDirty = false;
    Map changedCols;
    static transients = ['isMarkingDirty', 'changedCols']

    def dataSource;
    def dataTableService;

	static constraints = {
		dataset nullable:true
		agreeTerms nullable:true
		checklistId nullable:true
		uFile nullable:false
		traitValueFile nullable:true
		imagesFile nullable:true
		uploadLog nullable:true
		columns nullable:false, blank:false;
	}
	
	static mapping = {
		tablePerHierarchy false;
		id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "datatable_id_seq"]
        //cache include: 'non-lazy'
	}
    
	def fetchColumnNames(){
		def res = []
        if(columns) {
            def itr =  JSON.parse(columns).iterator()
            while(itr.hasNext()){
                res << itr.next()
            }
        }
		return res
	}

    private List fetchLatLongs() {
        def obvs = Observation.findAllByDataTable(this);
        List results = []
        obvs.each { obv ->    
            if(obv.latitude && obv.longitude) {
                results.add([obv.id.toString(), obv.latitude.toString(), obv.longitude.toString(), true, false])
            }
        }
        return results
    }	

    def getMapFeatures() {
        return dataTableService.getMapFeatures(this);
    }

   static Map getParamsToPropagate(DataTable dataTable) {
        Map paramsToPropagate = new HashMap();

        def cfs = dataTable.fetchCustomFields();
        cfs.each { cf -> 
            paramsToPropagate[cf.key] = cf.value;
        }

        paramsToPropagate[ObvUtilService.LICENSE] = License.read(dataTable.access.licenseId).name.value().replace("cc ", "");
        paramsToPropagate[ObvUtilService.AUTHOR_EMAIL] = dataTable.party.fetchContributor().email;

        //geographical coverage
        paramsToPropagate[ObvUtilService.LOCATION] = dataTable.geographicalCoverage.placeName;
        paramsToPropagate[ObvUtilService.TOPOLOGY] = dataTable.geographicalCoverage.topology;
        paramsToPropagate[ObvUtilService.LATITUDE] = dataTable.geographicalCoverage.latitude;
        paramsToPropagate[ObvUtilService.LONGITUDE] = dataTable.geographicalCoverage.longitude;
        paramsToPropagate[ObvUtilService.LOCATION_SCALE] = dataTable.geographicalCoverage.locationScale;

        //temporal coverage
        paramsToPropagate[ObvUtilService.OBSERVED_ON] = dataTable.temporalCoverage.fromDate;
        paramsToPropagate[ObvUtilService.TO_DATE] = dataTable.temporalCoverage.toDate;
        paramsToPropagate[ObvUtilService.DATE_ACCURACY] = dataTable.temporalCoverage.dateAccuracy;

        //taxonomic coverage

        return paramsToPropagate;
    }

   static inheritParams(dataObjectParams, dataTableParamsToPropagate) {
    //    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
        dataTableParamsToPropagate.each { key, value ->
            if(key.equals(ObvUtilService.TOPOLOGY) && !dataObjectParams[key]) {
                if(dataObjectParams[ObvUtilService.LATITUDE] && dataObjectParams[ObvUtilService.LONGITUDE]) {
                    //don't propagate as topology can be constructed from lat long fields
                } else {
//                    WKTReader wkt = new WKTReader(geometryFactory);
//                    try {
//                        Geometry geom = wkt.read(value);
                        dataObjectParams['areas'] = value;
//                    } catch(ParseException e) {
//                        log.error "Error parsing polygon wkt : ${params.areas}"
//                   }
                }
            }
            else if(!dataObjectParams[key]) dataObjectParams[key] = value;
        }
    }

   List getDataObjects(Map params=[:]){
       println params
       //Done because of java melody error - junk coming with offset value
       params.offset = params.offset ? params.offset.toInteger() : 0;
       params.max = params.max ? params.max.toInteger() :10
       params.offset = params.offset ? params.offset.toInteger() :0
//        UtilsService utilsService = grails.util.Holders.applicationContext.getBean('utilsService') as UtilsService

       switch(dataTableType) {
           case DataTableType.OBSERVATIONS: return getObservationData(id, params);
           case DataTableType.SPECIES : 
           //return Species.findAllByDataTableAndIsDeleted(this, false, [max:params.max, offset:params.offset, sort:'id']);
           def species = Species.executeQuery("select s from Species s inner join s.dataTables dataTable where s.isDeleted=:isDeleted and dataTable = :dataTable", [isDeleted:false, dataTable:this], [max:params.max, offset:params.offset, sort:'id']);
           println species 
           println  "#############"
           println  "#############"
           println  "#############"
           return species;
           case DataTableType.FACTS : 
           //def facts = Fact.findAllByDataTableAndIsDeleted(this, false, [max:params.max, offset:params.offset, sort:'objectType,objectId,id']);
           def c = Fact.createCriteria()
           def facts = c.list(max:params.max, offset:params.offset) {
               and {
                   eq('dataTable', this)
                   eq('isDeleted', false)
                   order('objectType','asc')
                   order('objectId','asc')
                   order('id','asc')
               }
           }
println facts
           List factsByObject = [];
           Map m = [:];
           facts.each { fact ->
               println fact
               if(!m[fact.objectType+fact.objectId] ) m[fact.objectType+fact.objectId] = []; 
                m[fact.objectType+fact.objectId] << fact
           }
           m.each {k,v ->
               println k
               println v;
            factsByObject << new FactsByObject(objectId:v[0].objectId, objectType:v[0].objectType, facts:m[v[0].objectType+v[0].objectId]);
           }
           return factsByObject;
           case DataTableType.TRAITS : 
           def traits = Trait.findAllByDataTableAndIsDeleted(this, false, [max:params.max, offset:params.offset, order:'id']);
           return traits;
           case DataTableType.DOCUMENTS : return Document.findAllByDataTableAndIsDeleted(this, false, [max:params.max, offset:params.offset, sort:'id']);
           
       }
       return [];
   }

   int getDataObjectsCount() {
       switch(dataTableType) {
           case DataTableType.OBSERVATIONS: return Observation.countByDataTableAndIsDeleted(this, false, [cache:true]);
           case DataTableType.SPECIES : //return 0;//Species.countByDataTableAndIsDeleted(this, false, [cache:true]);
           def result = Species.executeQuery("select count(*) from Species s inner join s.dataTables dataTable  where s.isDeleted=:isDeleted and dataTable = :dataTable",  [isDeleted:false, dataTable:this]);
           return result[0];
           case DataTableType.FACTS : return Fact.countByDataTableAndIsDeleted(this, false, [cache:true]);
           case DataTableType.TRAITS : return Trait.countByDataTableAndIsDeleted(this, false, [cache:true]);
           case DataTableType.DOCUMENTS : return Document.countByDataTableAndIsDeleted(this, false, [cache:true]);
       }
       return 0;
   }

   private List getObservationData(id, params=[:]){
       def sql =  Sql.newInstance(dataSource);
       def query = "select id  as obv_id from observation where data_table_id = " + id + " and is_deleted=false order by id limit " + params.max + " offset " + params.offset;
       log.debug "Running query : ${query}";
       def res = []
       sql.rows(query).each{
           println "Reading observation ${it.getProperty('obv_id')}"
           res << Observation.read(it.getProperty("obv_id"));
       }
       return res 
   }

    def deleteAllObservations() {
        def obvs = Observation.findAllByDataTable(this);
        obvs.each { obv ->    
            obv.isDeleted = true;
            if(!obv.save(flush:true)){
                obv.errors.allErrors.each { log.error obv } 
            }
        }
        return
    }

    def deleteAllFacts() {
        def obvs = Fact.findAllByDataTable(this);
        obvs.each { obv ->    
            obv.isDeleted = true;
            if(!obv.save(flush:true)){
                obv.errors.allErrors.each { log.error obv } 
            }
        }
        return
    }
    
    def deleteAllDocuments() {
        def obvs = Document.findAllByDataTable(this);
        obvs.each { obv ->    
            obv.isDeleted = true;
            if(!obv.save(flush:true)){
                obv.errors.allErrors.each { log.error obv } 
            }
        }
        return
    }
}

class FactsByObject {
    Long objectId;
    String objectType;
    List<Fact> facts;
   
    def utilsService;
    def grailsApplication;

    def fetchChecklistAnnotation(){
        println facts.size()
        Map res = [:];
        res['id'] = objectId;
        res['type'] = getController(objectType);//utilsService.getResType(grailsApplication.getArtefact("Domain",objectType));
        def species = facts[0].pageTaxon?.findSpecies(); 
        if(species) {
            res['speciesid'] = species.id
            res['title'] = species.title();
        }
        facts.each  { fact->
            println "----------------------------${fact}"
            res[fact.trait.name.toLowerCase()] = fact.traitValue?fact.traitValue.value:(fact.value + (fact.toValue ? ":" + fact.toValue:''))
fact.value
        }
        return res
    }

    private String getController (String objectType) {
        switch(objectType) {
            case Species.class.canonicalName:return "species";
            case Observation.class.canonicalName:return "observation";
            case Document.class.canonicalName:return "document";
        }
    }


}
