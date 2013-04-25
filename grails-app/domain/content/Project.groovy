package content


/**
 * Domain class for CPEF Grantee Project
 * 
 * 
 */

import content.fileManager.UFile
import org.grails.taggable.*
import org.apache.commons.collections.list.LazyList
import org.apache.commons.collections.FactoryUtils
import species.auth.SUser
import species.groups.UserGroup;



class Project implements Taggable{

	StrategicDirection direction;
	String title;
	String summary;
	
	List locations = new ArrayList();
	List dataLinks = new ArrayList();
	
	String granteeLogo; // Path to grantee Logo image
	String granteeOrganization;
	String granteeContact;
	String granteeEmail; 


	Date grantFrom;
	Date grantTo;
	int grantedAmount;

	String projectProposal;

	String projectReport;

	String misc;

	Date dateCreated;
	Date lastUpdated;
	
	String uploadDir;
	
	

	static mapping = {

		summary type:"text"
		projectProposal type:"text"
		projectReport type:"text"
		projectReport type:"text"
		analysis type:"text"
		misc type:"text"
		
		//locations cascade:"all-delete-orphan"
		
	}


	static hasMany = [ locations: Location,
		dataLinks:DataLink,
		proposalFiles: UFile,
		reportFiles: UFile,
		miscFiles: UFile,
		userGroups:UserGroup,
	];

	static belongsTo = [UserGroup]

	static constraints = {
		title(nullable: false);
		summary(nullable:true);
		direction(nullable: true);
		
		locations(nullable: true);
		
		granteeOrganization(nullable: true);
		granteeContact(nullable:true);
		granteeEmail(nullable:true, email:true, blank:false);
		granteeLogo(nullable:true)
		
		grantFrom(nullable: true);
		grantTo(nullable: true);

		grantedAmount validator : { val, obj ->  val >= 0 }, nullable:true
		
		
		projectProposal(nullable: true);
		proposalFiles(nullable: true);
		
		projectReport(nullable: true);
		reportFiles(nullable: true);
				
		misc(nullable: true);
		miscFiles(nullable: true);
		
		uploadDir(nullable:true)
	}



	def getLocationsList() {
		return LazyList.decorate(
		locations,
		FactoryUtils.instantiateFactory(Location.class))
	}
	
	
	def getDataLinksList() {
		return LazyList.decorate(
		dataLinks,
		FactoryUtils.instantiateFactory(DataLink.class))
	}
	
	
	String toString() {
		return title
	}

}
