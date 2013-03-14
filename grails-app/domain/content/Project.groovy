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
	
/*	static mapping = {
		analysisFiles cascade:"all-delete-orphan"
		proposalFiles cascade:"all-delete-orphan"
		reportFiles cascade:"all-delete-orphan"
		
	}
*/


    static hasMany = [ locations: Location,
						proposalFiles: UFile,
						reportFiles: UFile,
						analysisFiles: UFile,
						
                ];
			
/*	static mappedBy = [proposalFiles: "proposalProject",
				reportFiles: "reportProject",
				analysisFiles: "analysisProject"]*/
	
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
	
	def getReportFilesList() {
		return LazyList.decorate(
			reportFiles,
			FactoryUtils.instantiateFactory(UFile.class))
	}
	
	def getAnalysisFilesList() {
		return LazyList.decorate(
			analysisFiles,
			FactoryUtils.instantiateFactory(UFile.class))
	}
}
