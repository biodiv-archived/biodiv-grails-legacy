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
		
		metaDataList.add([keyPrefix + obvUtilService.AUTHOR_URL + SEPARATOR, obvUtilService.createHardLink('user', 'show', cl.author.id)])
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
		obv.fetchChecklistAnnotation().each { Annotation annot ->
			if(annot.key.equalsIgnoreCase(ChecklistService.SN_NAME) || annot.key.equalsIgnoreCase(ChecklistService.CN_NAME)){
				res.add(annot.value)
			}
		}
		res.add(0, "" + serialNo)
		res.add("")
		return res
	}
	
}
