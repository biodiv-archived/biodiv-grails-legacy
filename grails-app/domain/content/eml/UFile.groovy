package content.eml

import java.util.Date;
import species.License



/**
 * File Class. For storing file objects and physical attributes.
 * For usage in other classes. Right now only one UFile is associated with Document, this limitation can be altered in future.
 * Standalone: No.
 */
class UFile{	

	String path
	String size
	String mimetype
	Integer downloads
	int weight	//saves the order in a group
	
	boolean deleted
	
	static transients = [ 'deleted' ]
	
	static belongsTo = [Document]

	static mapping = {
	}

	static constraints = {
		//size(nullable:true)
		path(nullable:false)
		mimetype(nullable:true)
		downloads(default:0, nullable:true)
		weight(default:0, nullable:true)
	}

	def afterDelete() {
		try {
			File f = new File(path)
			if (f.delete()) {
				log.debug "file [${path}] deleted"
			} else {
				log.error "could not delete file: ${path}"
			}
		} catch (Exception exp) {
			log.error "Error deleting file: ${path}"
			log.error exp
		}
	}
	


}
