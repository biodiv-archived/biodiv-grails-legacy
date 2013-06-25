package species.participation

import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.License;
import species.Reference;
import species.Contributor;

import species.auth.SUser
import species.groups.SpeciesGroup
import species.groups.UserGroup;
import species.participation.ActivityFeedService
import org.grails.rateable.*


class Checklists extends Observation {
	
	private static final String KEY_PREFIX = "## "
	private static final String SEPARATOR = ":"
	private static final String META_DATA = "checklist_metadata"
	private static final String DATA = "checklist_data"
	
	def activityFeedService;
	def obvUtilService;
	
	String title;
	int speciesCount;
	
	//String attribution;
	License license;
	
	String refText;
	String sourceText;
	String rawChecklist;
	String columnNames;
	
	//to maintain order
	List observations;
	
	//dates
	Date publicationDate;
	
	//others
	String reservesValue;
	
	//will map to last revised in observation
	//Date lastUpdated; 
	//will map to notes in observation
	//String description; //info;
	
	static hasMany = [observations:Observation, contributors:SUser, attributions:Contributor, states : String, districts:String, talukas: String]
	
	static constraints = {
		//XXX this is extended class so have strictly say nullable false
		title nullable:false;
		speciesCount nullable:false;
		columnNames  nullable:false ;
		rawChecklist nullable:false;
		license  nullable:false;
		
		//attribution nullable:true;
		reservesValue nullable:true;
		states nullable:true;
		districts nullable:true;
		talukas nullable:true;
		
		refText nullable:true;
		sourceText nullable:true;
		
		//XXX to be removed
		publicationDate  nullable:true;
	}

	static mapping = {
		version : false;
		description type:'text';
		//attribution type:'text';
		refText type:'text';
		sourceText type:'text';
		columnNames type:'text';
	}
	
	def fetchColumnNames(){
		return columnNames.split("\t")
	}
	
	def fetchAttributions(){
		def attributionsString = ""
		if(!attributions){
			return attributionsString
		}
		def itr = attributions?.iterator()
		int count = 0
		while(itr.hasNext()){
			attributionsString = (count == 0) ? attributionsString : attributionsString + ", "
			attributionsString += itr.next().name
			count++
		}
		return attributionsString
	}
	
	/*
	def Map fetchExportableValue(){
		Map res = [:]
		Checklists cl = this
		
		List metaDataList = []
		
		metaDataList.add([KEY_PREFIX + "title" + SEPARATOR,  cl.title] )
		metaDataList.add([KEY_PREFIX + "license" + SEPARATOR,  "" + cl.license.name] )
		metaDataList.add([KEY_PREFIX + "attribution" + SEPARATOR,  "" + cl.attribution] )
		
		metaDataList.add([KEY_PREFIX + "speciesCount" + SEPARATOR, "" + cl.speciesCount] )
		metaDataList.add([KEY_PREFIX + "description" + SEPARATOR,  cl.description])
		metaDataList.add([KEY_PREFIX + "refText" + SEPARATOR,  cl.refText])
		metaDataList.add([KEY_PREFIX + "sourceText" + SEPARATOR,  cl.sourceText])
		metaDataList.add([KEY_PREFIX + "reservesValue" + SEPARATOR,  "" + cl.reservesValue])
		
		metaDataList.add([KEY_PREFIX + "latitude" + SEPARATOR, "" + cl.latitude])
		metaDataList.add([KEY_PREFIX + "longitude" + SEPARATOR, "" + cl.longitude])
		metaDataList.add([KEY_PREFIX + "placeName" + SEPARATOR, cl.placeName])
		metaDataList.add([KEY_PREFIX + "state" + SEPARATOR,  cl.state.join(", ")])
		metaDataList.add([KEY_PREFIX + "district" + SEPARATOR,  cl.district.join(", ")])
		metaDataList.add([KEY_PREFIX + "taluka" + SEPARATOR,  cl.taluka.join(", ")])
		
		
		metaDataList.add([KEY_PREFIX + "fromDate" + SEPARATOR,  "" + cl.fromDate])
		metaDataList.add([KEY_PREFIX + "toDate" + SEPARATOR,  "" + cl.toDate])
		metaDataList.add([KEY_PREFIX + "publicationDate" + SEPARATOR,  "" + cl.publicationDate])
		
		
		def ug = []
		cl.userGroups.collect{ ug.add(it.name)}
		
		def sg = []
		cl.speciesGroups.collect{ sg.add(it.name)}
		
		metaDataList.add([KEY_PREFIX + "userGroups" + SEPARATOR,  ug.join(", ")])
		metaDataList.add([KEY_PREFIX + "speciesGroups" + SEPARATOR,  sg.join(", ")])
		
		metaDataList.add([KEY_PREFIX + obvUtilService.AUTHOR_URL + SEPARATOR, obvUtilService.createHardLink('user', 'show', cl.author.id)])
		metaDataList.add([KEY_PREFIX + obvUtilService.AUTHOR_NAME + SEPARATOR, cl.author.name])
		
		res[META_DATA] = metaDataList
		res[DATA] = fetchData()
		return res
	}
		
	private List fetchData(){
		Checklists cl = this
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
			valueList.add(r.value)
		}
		
		data.add(valueList)
		return data
	}
	*/
}
