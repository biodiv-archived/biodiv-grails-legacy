package species

import org.springframework.web.multipart.MultipartHttpServletRequest;

class FileUploadFilterFilters {

    def filters = {
		multipartFileSupport(controller: '*', action: '*') {
			before = {
				if (request instanceof MultipartHttpServletRequest) {
					def multipleFileMap = request.multiFileMap
					multipleFileMap.each {fieldName, files ->
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
