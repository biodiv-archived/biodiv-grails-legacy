package content.fileManager

import java.util.Date;
import org.grails.taggable.Taggable
import species.auth.SUser
import content.Project

class UFile implements Taggable{
	
	String size
	String path
	String name
	String description
	String extension
	Date dateCreated
	Integer downloads
	String doi
	int weight	//saves the order in a group
	//boolean deleted
	SUser owner
	
	//source holder(i.e project, group)
	Long sourceHolderId;
	String sourceHolderType;
	
/*	Project analysisProject
	Project reportProject
	Project proposalProject*/
	
	
	//static transients = [ 'deleted' ]
	
	// BelongsTO 
	// File can be belonged to CEPFproject, a Group(Media material of a group like posters etc.), Checklists, Species Page
	//static belongsTo = [project:Project]
	 
	static mapping = {

	}
	static constraints = {
		size(nullable:true)
		path(nullable:false)
		name(nullable:false)
		description(nullable:true)
		extension(nullable:true)
		downloads(default:0)
		doi(nullable:true)
		weight(default:0)
/*		analysisProject(nullable:true)
		reportProject(nullable:true)
		proposalProject(nullable:true)*/
		sourceHolderId(nullable:true)
		sourceHolderType(nullable:true)
		owner(nullable:true)
	}

	def afterDelete() {
		try {
			File f = new File(path)
			if (f.delete()) {
				log.debug "file [${path}] deleted"
			} else {
				log.error "could not delete file: ${file}"
			}
		} catch (Exception exp) {
			log.error "Error deleting file: ${e.message}"
			log.error exp
		}
	}

}
