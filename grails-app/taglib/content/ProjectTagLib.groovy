package content

import content.Project

class ProjectTagLib {
	
	static namespace = 'project'
	
	def projectListItem = {attrs, body->
		if(attrs.model.projectInstance) {
			out << render(template:"/project/projectListItemTemplate", model:attrs.model);
		}
	}
	
	def search = {attrs, body->
		out << render(template:"/project/search", model:attrs.model);
		
	}

}
