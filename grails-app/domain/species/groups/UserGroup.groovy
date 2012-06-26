package species.groups

import org.grails.taggable.Taggable;
import org.springframework.security.acls.domain.BasePermission;

import species.Habitat;
import species.Resource;
import species.Resource.ResourceType;
import species.auth.SUser;
import species.participation.Observation;
import species.utils.ImageType;
import species.utils.ImageUtils;

class UserGroup implements Taggable {

	String name;
	String description;
	String webaddress;
	Date foundedOn = new Date();
	boolean isDeleted = false;
	long visitCount = 0;
	
	def grailsApplication;

	static hasMany = [members:SUser, sGroups:SpeciesGroup, habitats:Habitat, observations:Observation]

	static constraints = {
		name nullable: false, blank:false, unique:true
		webaddress nullable: false, blank:false, unique:true
		description nullable: false, blank:false
	}

	static mapping = { 
		version  false;
		description type:'text';
	}

	Resource icon(ImageType type) {
		String name = this.name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
		name = ImageUtils.getFileName(name, type, '.png');

		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.resources.rootDir+"/group_icons/groups/${name}")).exists()
		if(!iconPresent) {
			name = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
			name = ImageUtils.getFileName(name, type, '.png');
		}

		return new Resource(fileName:"group_icons/groups/${name}", type:ResourceType.ICON, title:"You can contribute!!!");
	}

	Resource mainImage() {
		return icon(ImageType.NORMAL);
	}
	
	def incrementPageVisit(){
		visitCount++;
		
		if(!save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}
	
	def getPageVisitCount(){
		return visitCount;
	}
	
	def getFounders() {
		
	}
}
