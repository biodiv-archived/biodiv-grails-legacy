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

class DataTable extends CollMetadata {
	
	
//	DataTableType type;
	boolean agreeTerms = false;
	static hasOne = [dataset : Dataset1]
    UFile uFile;
    DataTableType dataTableType;

    UploadLog uploadLog;

	//serialized object to store list of column names
	String columns;
    Long checklistId;
    static hasMany = [userGroups:UserGroup];
	static belongsTo = [UserGroup]

    def dataSource;
    def dataTableService;
    
	static constraints = {
		dataset nullable:true
		agreeTerms nullable:true
		checklistId nullable:true
		uFile nullable:false
		uploadLog nullable:true
		columns nullable:false, blank:false;
	}
	
	static mapping = {
		tablePerHierarchy false;
		id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "datatable_id_seq"]
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
        dataTableParamsToPropagate.each { key, value ->
            if(!dataObjectParams[key]) dataObjectParams[key] = value;
        }
    }

   List getDataObjects(Map params=[:]){
       println params
       //Done because of java melody error - junk coming with offset value
       params.offset = params.offset ? params.offset.tokenize("/?")[0] : 0;
       params.max = params.max ? params.max.toInteger() :10
       params.offset = params.offset ? params.offset.toInteger() :0
//        UtilsService utilsService = grails.util.Holders.applicationContext.getBean('utilsService') as UtilsService

       switch(dataTableType) {
           case DataTableType.OBSERVATIONS: return getObservationData(id, params);
           case DataTableType.SPECIES : return Species.findAllByDataTableAndIsDeleted(this, false, [max:params.max, offset:params.offset, order:'id']);
           case DataTableType.FACTS : 
           /*def factObjects = [];
           def r = Fact.executeQuery("select objectType as objectType, objectId as objectId from Fact where dataTable=:dataTable group by objectId, objectType", ['dataTable':this], [max:params.max, offset:params.offset, order:'id']);
           r.each {
               println it
               def obj = utilsService.getDomainObject(it[0], it[1]);
               def facts = Fact.executeQuery("select * from Fact where dataTable=:dataTable and objectType=:objectType, objectId=:objectId", ['dataTable':this,'objectType':it[0], 'objectId':it[1]]);
               println facts
               factObjects << obj;
           }
           return factObjects;
           */def facts = Fact.findAllByDataTableAndIsDeleted(this, false, [max:params.max, offset:params.offset, order:'id,objectType,objectId']);
           List factsByObject = [];
           Map m = [:];
           facts.each { fact ->
               if(!m[fact.objectType+fact.objectId] ) m[fact.objectType+fact.objectId] = []; 
                m[fact.objectType+fact.objectId] << fact
           }
           m.each {k,v ->
            factsByObject << new FactsByObject(objectId:v[0].objectId, objectType:v[0].objectType, facts:v);
           }
           return factsByObject;
           
       }
       return [];
   }

   int getDataObjectsCount() {
       switch(dataTableType) {
           case DataTableType.OBSERVATIONS: return Observation.countByDataTableAndIsDeleted(this, false);
           case DataTableType.SPECIES : return Species.countByDataTableAndIsDeleted(this, false);
           case DataTableType.FACTS : return Fact.countByDataTableAndIsDeleted(this, false);
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
}

class FactsByObject {
    Long objectId;
    String objectType;
    List<Fact> facts;
   
    def utilsService;
    def grailsApplication;

    def fetchChecklistAnnotation(){
        Map res = [:];
        res['id'] = objectId;
        res['type'] = getController(objectType);//utilsService.getResType(grailsApplication.getArtefact("Domain",objectType));
        def species = facts[0].pageTaxon?.findSpecies(); 
        if(species) {
            res['speciesid'] = species.id
            res['title'] = species.title();
        }
        facts.each  { fact->
            res[fact.trait.name.toLowerCase()] = fact.traitValue?fact.traitValue.value:value
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
