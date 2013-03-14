package content

import content.fileManager.UFile
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

class FileManagerTagLib {
	
	static namespace = 'fileManager'
	
	
	def uploader = { attrs, body ->
		out << render(template:"/UFile/uploader", model: attrs.model);	
	}
	
	
	def download = { attrs, body ->
		
		//checking required fields
		if (!attrs.id) {
			def errorMsg = "'id' attribute not found in file-uploader download tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
				
	
		params.errorAction = "browser"
		params.errorController = "UFile"
		
		out << g.link([controller: "UFile", action: "download", params: params, id: attrs.id], body)
		
	}
	def showAllFiles = { attrs, body -> 
		
		out << body
	}
	

}
