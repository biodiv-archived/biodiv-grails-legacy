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
	
}

