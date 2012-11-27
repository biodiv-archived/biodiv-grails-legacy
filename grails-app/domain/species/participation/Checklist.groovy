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
	String linkText;
	String rawChecklist;
	
	License license;
	SpeciesGroup speciesGroup;
	SUser validator;
	
	//location related
	float sw_latitude;
	float sw_longitude;
	float ne_latitude;
	float ne_longitude;
	String placeName;
	
	//dates
	Date fromDate;
	Date toDate;
	Date publicationDate;
	Date lastUpdated;
	SortedSet row;
	
	static hasMany = [row:ChecklistRowData, reference:Reference]
	static belongsTo = [author:SUser];

	static constraints = {
		fromDate nullable:true;
		toDate nullable:true;
		validator nullable:true;
		description nullable:true;
		attribution nullable:true;
		
		refText nullable:true;
		linkText nullable:true;
		reference nullable:true;
		
		//XXX to be removed
		speciesGroup nullable:true; 
		rawChecklist nullable:true; 
		publicationDate  nullable:true;
		sw_latitude nullable:true;
		sw_longitude nullable:true;
		ne_latitude nullable:true;
		ne_longitude nullable:true;
	}

	static mapping = {
		version : false;
		description type:'text';
		attribution type:'text';
		refText type:'text';
		linkText type:'text';
		//row sort:"rowId", "key"
	}
	
	//  contributed by => author
	//	geography_given_name | character varying(255) |
	//	all_india            | character varying(3)   |
		
	//	raw_checklist        | text                   |
	//	processed_checklist  | text                   |
	//	checklist_references | text                   |
	//	link                 | text                   |
	//	validated            | character varying(3)   |
	//	validated_by         | integer
	
}
