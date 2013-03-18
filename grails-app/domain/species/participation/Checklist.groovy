package species.participation

import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.License;
import species.Reference;

import species.auth.SUser
import species.groups.SpeciesGroup
import species.groups.UserGroup;
import species.participation.ActivityFeedService

class Checklist {
	
	private static final String KEY_PREFIX = "## "
	private static final String SEPARATOR = ":"
	private static final String META_DATA = "checklist_metadata"
	private static final String DATA = "checklist_data"
	
	
	def activityFeedService;
	def obvUtilService;
	
	String title;
	int speciesCount;
	String description; //info;
	String attribution;
	 
	String refText;
	String sourceText;
	String rawChecklist;
	
	License license;
	//SpeciesGroup speciesGroup;
	SUser validator;
	
	//location related
	float latitude;
	float longitude;
	String placeName;
	
	//dates
	Date fromDate;
	Date toDate;
	Date publicationDate;
	Date lastUpdated; 
	
	//content data
	SortedSet row;
	String columnNames 
	
	//others
	String reservesValue;
	
	static hasMany = [row:ChecklistRowData, state : String, district:String, taluka: String, userGroups:UserGroup, speciesGroups:SpeciesGroup]
	static belongsTo = [author:SUser];

	static constraints = {
		fromDate nullable:true;
		toDate nullable:true;
		validator nullable:true;
		description nullable:true;
		attribution nullable:true;
		reservesValue nullable:true;
		placeName nullable:true;
		state nullable:true;
		district nullable:true;
		taluka nullable:true;
		
		refText nullable:true;
		sourceText nullable:true;
		columnNames  nullable:true;
		
		//XXX to be removed
		rawChecklist nullable:true; 
		publicationDate  nullable:true;
		latitude nullable:true;
		longitude nullable:true;
	}

	static mapping = {
		version : false;
		description type:'text';
		attribution type:'text';
		refText type:'text';
		sourceText type:'text';
		columnNames type:'text';
	}
	
	def fetchColumnNames(){
		return columnNames.split("\t")
	}
	
	def Map fetchExportableValue(boolean isPdf){
		Map res = [:]
		Checklist cl = this
		def keyPrefix = (!isPdf) ? KEY_PREFIX : ""
		 
		List metaDataList = []
		
		metaDataList.add([keyPrefix + "title" + SEPARATOR,  cl.title] )
		metaDataList.add([keyPrefix + "license" + SEPARATOR,  "" + cl.license.name] )
		metaDataList.add([keyPrefix + "attribution" + SEPARATOR,  "" + cl.attribution] )
		
		metaDataList.add([keyPrefix + "speciesCount" + SEPARATOR, "" + cl.speciesCount] )
		metaDataList.add([keyPrefix + "description" + SEPARATOR,  cl.description])
		metaDataList.add([keyPrefix + "refText" + SEPARATOR,  cl.refText])
		metaDataList.add([keyPrefix + "sourceText" + SEPARATOR,  cl.sourceText])
		metaDataList.add([keyPrefix + "reservesValue" + SEPARATOR,  "" + cl.reservesValue])
		
		metaDataList.add([keyPrefix + "latitude" + SEPARATOR, "" + cl.latitude])
		metaDataList.add([keyPrefix + "longitude" + SEPARATOR, "" + cl.longitude])
		metaDataList.add([keyPrefix + "placeName" + SEPARATOR, cl.placeName])
		metaDataList.add([keyPrefix + "state" + SEPARATOR,  cl.state.join(", ")])
		metaDataList.add([keyPrefix + "district" + SEPARATOR,  cl.district.join(", ")])
		metaDataList.add([keyPrefix + "taluka" + SEPARATOR,  cl.taluka.join(", ")])
		
		
		metaDataList.add([keyPrefix + "fromDate" + SEPARATOR,  "" + cl.fromDate])
		metaDataList.add([keyPrefix + "toDate" + SEPARATOR,  "" + cl.toDate])
		metaDataList.add([keyPrefix + "publicationDate" + SEPARATOR,  "" + cl.publicationDate])
		
		
		def ug = []
		cl.userGroups.collect{ ug.add(it.name)}
		
		def sg = []
		cl.speciesGroups.collect{ sg.add(it.name)}
		
		metaDataList.add([keyPrefix + "userGroups" + SEPARATOR,  ug.join(", ")])
		metaDataList.add([keyPrefix + "speciesGroups" + SEPARATOR,  sg.join(", ")])
		
		metaDataList.add([keyPrefix + obvUtilService.AUTHOR_URL + SEPARATOR, obvUtilService.createHardLink('user', 'show', cl.author.id)])
		metaDataList.add([keyPrefix + obvUtilService.AUTHOR_NAME + SEPARATOR, cl.author.name])
		
		res[META_DATA] = metaDataList
		res[DATA] = fetchData(isPdf)
		return res
	}
		
	private List fetchData(boolean isPdf){
		Checklist cl = this
		List data = []
		
		int prevRowId = -1
		def valueList = []
		cl.row.each { ChecklistRowData r ->
			if(prevRowId == -1){
				prevRowId = r.rowId
			}
			
			if(prevRowId != r.rowId){
				data.add(valueList)
				valueList = []
				prevRowId = r.rowId
			}
			
			//only adding sn and cn for pdf
			if(isPdf){
				if(r.key.equalsIgnoreCase(ChecklistService.SN_NAME) || r.key.equalsIgnoreCase(ChecklistService.CN_NAME)){
					valueList.add(r.value)
				}
			}else{
				valueList.add(r.value)
			}
		}
		
		data.add(valueList)
		
		//adding serial number and notes column for pdf
		if(isPdf){
			int i = 0
			data.each { List valList ->
				valList.add(0, "" + (++i))
				valList.add("")
			}
		}
		
		return data
	}
	
}
