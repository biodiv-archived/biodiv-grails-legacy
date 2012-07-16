package species

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import species.utils.Utils;

class FileUploadFilterFilters {

	def filters = {
		multipartFileSupport(controller: '*', action: '*') {
			before = {
				if (ServletFileUpload.isMultipartContent(request)) {
					def multipleFileMap = request.multiFileMap
					println "----------------"+multipleFileMap
					println "----------------"+request.fileMap;
					multipleFileMap.each { fieldName, files ->
						if (files.size() == 1) {
							params.put(fieldName, files.first())
						} else {
							params.put(fieldName, files)
						}
					}
				}
			}
		}
	}
}
