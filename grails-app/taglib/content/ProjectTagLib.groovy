package content

import content.Project

class ProjectTagLib {
	static namespace = 'project'
	
	def documentService
	
	def projectListItem = {attrs, body->
		if(attrs.model.projectInstance) {
			out << render(template:"/project/projectListItemTemplate", model:attrs.model);
		}
	}
	
	def search = {attrs, body->
		out << render(template:"/project/search", model:attrs.model);
	}
	
	def showTagsCloud = {attrs, body->
		def model = attrs.model
		model.tags = documentService.getFilteredTagsByUserGroup(params.webaddress, model.tagType)
		out << render(template:"/common/observation/showTagsCloudTemplate", model:model);
	}
	
}
