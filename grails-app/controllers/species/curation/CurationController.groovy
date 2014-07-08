package species.curation

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import species.NamesParser;
import species.TaxonomyDefinition;

class CurationController {

    def index = { }
	
	def springSecurityService;
	
	@Secured(['ROLE_SPECIES_ADMIN'])
	def names() {

		log.debug params;
		if(!params.namesFile) {
			flash.message = g.message(code: 'no.file.attached', default:'No file is attached')
			return;
		}

		try {
			if(ServletFileUpload.isMultipartContent(request)) {
				
				def CommonsMultipartFile uploadedFile = params.namesFile
				def contentType = uploadedFile.contentType
				def fileName = uploadedFile.originalFilename
				def size = uploadedFile.size
				
				
				if (!uploadedFile.isEmpty()) {
					def file = new File("/tmp/${springSecurityService.currentUser.username}_names.txt".toString());
					uploadedFile.transferTo(file);
					List<String> givenNames = [];
					String line;
					file.withReader('UTF-8') { reader ->
						while (line = reader.readLine()) {
							givenNames << line
						}
					}
					
					
					NamesParser namesParser = new NamesParser();
					List<TaxonomyDefinition> parsedNames = namesParser.parse(givenNames);
					return [parsedNames:parsedNames];
				}
				
			}
		} catch(Exception e) {
			log.error "$e.message"
		}
	}
}
