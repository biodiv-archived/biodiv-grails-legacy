package content.eml

import java.util.List;

import species.groups.SpeciesGroup;
import species.Habitat

class DocumentService {

    static transactional = false
	def userGroupService;
	


	Document createDocument(params) {
		
		def document = new Document(params)
		
		document.coverage.location = 'POINT(' + params.coverage.longitude + ' ' + params.coverage.latitude + ')'
		document.coverage.reverseGeocodedName = params.coverage.reverse_geocoded_name
		document.coverage.locationAccuracy = params.coverage.location_accuracy
		
		document.coverage.speciesGroups = []
		params.speciesGroup.each {key, value ->
			log.debug "Value: "+ value
			document.coverage.addToSpeciesGroups(SpeciesGroup.read(value.toLong()));
		}
		
		document.coverage.habitats  = []
		params.habitat.each {key, value ->
			document.coverage.addToHabitats(Habitat.read(value.toLong()));
		}
		return document
	}
	
	def setUserGroups(Document documentInstance, List userGroupIds) {
		if(!documentInstance) return
		
		def docInUserGroups = documentInstance.userGroups.collect { it.id + ""}
		println docInUserGroups;
		def toRemainInUserGroups =  docInUserGroups.intersect(userGroupIds);
		if(userGroupIds.size() == 0) {
			println 'removing document from usergroups'
			userGroupService.removeDocumentFromUserGroups(documentInstance, docInUserGroups);
		} else {
			userGroupIds.removeAll(toRemainInUserGroups)
			userGroupService.postDocumenttoUserGroups(documentInstance, userGroupIds);
			docInUserGroups.removeAll(toRemainInUserGroups)
			userGroupService.removeDocumentFromUserGroups(documentInstance, docInUserGroups);
		}
	}
		
	
}
