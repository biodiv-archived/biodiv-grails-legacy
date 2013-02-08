package species.participation

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.License;
import species.Reference;

import species.auth.SUser
import species.groups.SpeciesGroup
import species.groups.UserGroup;
import species.participation.ActivityFeedService

class Checklist {
	def activityFeedService;
	
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
}
