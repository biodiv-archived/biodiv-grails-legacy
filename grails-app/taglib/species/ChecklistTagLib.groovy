package species

class ChecklistTagLib {
	static namespace = "clist"
	
	def checklistService
	
	def showFilteredCheckList = {attrs, body->
		out << render(template:"/common/checklist/showFilteredChecklistTemplate", model:attrs.model);
	}

	def showChecklistLocation= {attrs, body->
		out << render(template:"/common/checklist/showChecklistMultipleLocationTemplate", model:attrs.model);
	}
	
	def showSnippet= {attrs, body->
		//out << render(template:"/common/checklist/showChecklistSnippetTabletTemplate", model:attrs.model);
		out << render(template:"/common/checklist/showChecklistSnippetTemplate", model:attrs.model);
	}
	
	def showData= {attrs, body->
		if(!attrs.model.observations){
			attrs.model.observations = checklistService.getObservationData(attrs.model.checklistInstance.id)
		}
		out << render(template:"/common/checklist/showChecklistDataTemplate", model:attrs.model);
	}
	
	def showChecklistMsg= {attrs, body->
		out << render(template:"/common/checklist/showChecklistMsgTemplate", model:attrs.model);
	}
	
	def showList = {attrs, body->
		out << render(template:"/common/checklist/showChecklistListTemplate", model:attrs.model);
	}
	
	def showSubmenuTemplate = {attrs, body->
		out << render(template:"/checklist/submenuTemplate", model:attrs.model);
	}
	
	def filterTemplate = {attrs, body->
		out << render(template:"/checklist/filterTemplate", model:attrs.model);
	}
}
