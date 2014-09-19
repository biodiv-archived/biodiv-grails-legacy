package species.participation

import grails.converters.JSON

import java.util.List;
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
	def utilsService;
	
	String title;
	int speciesCount = 0;
	
	//String attribution;
	License license;
	
	String refText;
	String sourceText;
	String rawChecklist;
	
	//to maintain order
	List observations;
	
	//dates
	Date publicationDate;
	
	//others
	String reservesValue;
	
	// marked column for parsing name
	String sciNameColumn;
	String commonNameColumn;
	
	//serialized object to store list of column names
	String columns;
	
	//backword reference to drupal checklist for all old checlist 
	long drupalId = -1; 
	
	static hasMany = [observations:Observation, contributors:SUser, attributions:Contributor, states : String, districts:String, talukas: String]
	
	static constraints = {
		//XXX this is extended class so have strictly say nullable false
		title nullable:false, blank:false;
		speciesCount nullable:false;
		rawChecklist nullable:true;
		license  nullable:false, blank:false;
		//make this false after migration
		columns nullable:false, blank:false;
		
		//attribution nullable:true;
		reservesValue nullable:true;
		states nullable:true;
		districts nullable:true;
		talukas nullable:true;
		
		refText nullable:true;
		sourceText nullable:true;
		
		//XXX to be removed
		publicationDate  nullable:true, validator : {val, obj -> 
			if(!val){
				return true
			}else{
			 	return val < new Date() 
			}
		}
		//at least one of the column name must present 
		sciNameColumn nullable:true, blank:true;
		commonNameColumn nullable:true, blank:true;
		drupalId nullable:true, blank:true;
	}

	static mapping = {
		version : false;
		description type:'text';
		refText type:'text';
		sourceText type:'text';
		columns type:'text';
	}

	def fetchColumnNames(){
		def itr =  JSON.parse(columns).iterator()
		def res = []
		while(itr.hasNext()){
			res << itr.next()
		}
		return res
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
	
	
	def Map fetchExportableValue(boolean isPdf=false){
		Map res = [:]
		Checklists cl = this
		def keyPrefix = (!isPdf) ? KEY_PREFIX : ""
			
		List metaDataList = []
		
		metaDataList.add([keyPrefix + "title" + SEPARATOR,  cl.title] )
		metaDataList.add([keyPrefix + "license" + SEPARATOR,  "" + cl.license.name] )
		metaDataList.add([keyPrefix + "attribution" + SEPARATOR,  "" + cl.fetchAttributions()] )
		
		metaDataList.add([keyPrefix + "speciesGroup" + SEPARATOR,  cl.group.name])
		metaDataList.add([keyPrefix + "speciesCount" + SEPARATOR, "" + cl.speciesCount] )
		metaDataList.add([keyPrefix + "description" + SEPARATOR,  cl.notes])
		metaDataList.add([keyPrefix + "refText" + SEPARATOR,  cl.refText])
		metaDataList.add([keyPrefix + "sourceText" + SEPARATOR,  cl.sourceText])
		metaDataList.add([keyPrefix + "reservesValue" + SEPARATOR,  "" + cl.reservesValue])
		
		metaDataList.add([keyPrefix + "latitude" + SEPARATOR, "" + cl.latitude])
		metaDataList.add([keyPrefix + "longitude" + SEPARATOR, "" + cl.longitude])
		metaDataList.add([keyPrefix + "placeName" + SEPARATOR, cl.placeName])
		metaDataList.add([keyPrefix + "state" + SEPARATOR,  cl.states.join(", ")])
		metaDataList.add([keyPrefix + "district" + SEPARATOR,  cl.districts.join(", ")])
		metaDataList.add([keyPrefix + "taluka" + SEPARATOR,  cl.talukas.join(", ")])
		
		
		metaDataList.add([keyPrefix + "fromDate" + SEPARATOR,  "" + cl.fromDate])
		metaDataList.add([keyPrefix + "toDate" + SEPARATOR,  "" + cl.toDate])
		metaDataList.add([keyPrefix + "publicationDate" + SEPARATOR,  "" + cl.publicationDate])
		
		
		def ug = []
		cl.userGroups.collect{ ug.add(it.name)}
		metaDataList.add([keyPrefix + "userGroups" + SEPARATOR,  ug.join(", ")])
		
		metaDataList.add([keyPrefix + obvUtilService.AUTHOR_URL + SEPARATOR, utilsService.createHardLink('user', 'show', cl.author.id)])
		metaDataList.add([keyPrefix + obvUtilService.AUTHOR_NAME + SEPARATOR, cl.author.name])
		
		res[META_DATA] = metaDataList
		res[DATA] = fetchData(isPdf)
		return res
	}
		
	private List fetchData(boolean isPdf){
		Checklists cl = this
		List data = []
		int i = 0
		cl.observations.each { Observation obv ->
			if(!isPdf){
				data << obv.fetchChecklistAnnotation().collect { it.value}
			}else{
				data << getLimitedColumnForPdf(obv, ++i)		
			}
		}
		return data
	}
	
	private List getLimitedColumnForPdf(Observation obv, int serialNo){
		def res = []
		obv.fetchChecklistAnnotation().each { annot ->
			if(annot.key.equalsIgnoreCase(sciNameColumn) || annot.key.equalsIgnoreCase(commonNameColumn)){
				res.add(annot.value)
			}
		}
		res.add(0, "" + serialNo)
		res.add("")
		return res
	}
	
	String fetchSpeciesCall(){
		return title
	}
	
	/**
	* @return
	* List of dirty fields that should update observation.
	*/
   static List fetchDirtyFields(){
	   return ["fromDate", "geoPrivacy", "group", "habitat", "latitude", "locationAccuracy", "longitude", "placeName", "reverseGeocodedName", "toDate", "topology", "sciNameColumn", "commonNameColumn"]
   }

	
   	
}
