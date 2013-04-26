package content.fileManager

import java.util.Date;
import org.grails.taggable.Taggable
import species.License
import species.auth.SUser
import content.Project

class UFile implements Taggable{

	
	public enum FileType {
		REPORT("Report"),
		POSTER("Poster"),
		PROPOSAL("Proposal"),
		MISCELLANEOUS("Miscellaneous"),

		private String value;
		

		FileType(String value) {
			this.value = value;
		}

		public String value() {
			return this.value;
		}
	}

	FileType type
	String name	
	String size
	String path
	String description
	String mimetype
	Date dateCreated
	Integer downloads
	String doi
	int weight	//saves the order in a group
	SUser author
	License license

	String contributors;
	String attribution;

	//source holder(i.e project, group)
	Long sourceHolderId;
	String sourceHolderType;
	
	boolean deleted
	
	static transients = [ 'deleted' ]

	static mapping = {
		description type:"text"
	}

	static constraints = {
		//size(nullable:true)
		path(nullable:false)
		name(nullable:false)
		description(nullable:true)
		mimetype(nullable:true)
		downloads(default:0, nullable:true)
		doi(nullable:true)
		weight(default:0, nullable:true)
		sourceHolderId(nullable:true)
		sourceHolderType(nullable:true)
		author(nullable:true)
		license(nullable:true)
		contributors(nullable:true)
		attribution(nullable:true)

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
	
	def setSource(parent) {
		this.sourceHolderId = parent.id
		this.sourceHolderType = parent.class.getCanonicalName()
	}

}
