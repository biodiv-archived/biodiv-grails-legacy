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


class Project implements Taggable{

	StrategicDirection direction;
	String title;
	String summary;
	
	List locations = new ArrayList();
	
	//Grantee grantee;
	//  File granteeLogo; // TODO
	String granteeURL;
	String granteeName;
	SUser granteeContact; //TODO


	Date grantFrom;
	Date grantTo;
	Integer grantedAmount;

	String projectProposal;

	String projectReport;

	String dataContributionIntensity;

	String analysis;

	String misc;

	Date dateCreated;

	static mapping = {

		summary type:"text"
		projectProposal type:"text"
		projectReport type:"text"
		projectReport type:"text"
		dataContributionIntensity type:"text"
		analysis type:"text"
		misc type:"text"
		
		locations cascade:"all-delete-orphan"
		


	}



	static hasMany = [ locations: Location,
		proposalFiles: UFile,
		reportFiles: UFile,
		analysisFiles: UFile,

	];


	static constraints = {
		title(nullable: false);
		summary(nullable:true);
		direction(nullable: true);
		granteeURL();
		granteeName(nullable: false);
		granteeContact(nullable:true);
		locations();
		grantFrom();
		grantTo();
		grantedAmount(nullable: true);
		projectProposal(nullable: true);
		proposalFiles(nullable: true);
		projectReport(nullable: true);
		reportFiles(nullable: true);
		dataContributionIntensity(nullable: true);
		analysis(nullable: true);
		analysisFiles(nullable: true);
		misc(nullable: true);

	}



	def getLocationsList() {
		return LazyList.decorate(
		locations,
		FactoryUtils.instantiateFactory(Location.class))
	}
	

}
