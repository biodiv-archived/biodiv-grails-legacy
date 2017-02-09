package species.groups

import java.util.List;
import java.util.Map;

import species.Classification;
import species.Resource.ResourceType;
import species.TaxonomyDefinition;
import species.Resource;
import species.utils.ImageType;
import species.utils.ImageUtils;

class SpeciesGroup {

	String name;
	SpeciesGroup parentGroup;
	int groupOrder;
	def grailsApplication;
    def groupHandlerService;

	static hasMany = [taxonConcept:TaxonomyDefinition, speciesGroupMapping:SpeciesGroupMapping]
	
	static fetchMode = [parentGroup: 'eager']
	
    static constraints = {
		name(blank:false, unique:true);
		parentGroup nullable:true;
    }
	
	static mapping = {
		version  false;
		sort groupOrder:"asc"
        cache usage: 'read-only', include: 'non-lazy'
	}
	
	Resource icon(ImageType type) {
		String name = this.name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
		name = ImageUtils.getFileName(name, type, '.png');
        if(type == ImageType.ORIGINAL) {
            name = name + ".png"
        }
		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.resources.rootDir+"/../group_icons/speciesGroups/${name}")).exists()

		if(!iconPresent) {
            log.debug "icon not present. ${name}"
			name = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')
			name = ImageUtils.getFileName(name, type, '.png');
		}

	def r = new Resource('fileName':"group_icons/speciesGroups/${name}", 'type':ResourceType.IMAGE, 'title':"You can contribute!!!");
        r['baseUrl'] = grailsApplication.config.grails.serverURL
        return r;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((parentGroup == null) ? 0 : parentGroup.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this.is(obj))
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SpeciesGroup))
			return false;
		SpeciesGroup other = (SpeciesGroup) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentGroup == null) {
			if (other.parentGroup != null)
				return false;
		} else if (!parentGroup.equals(other.parentGroup))
			return false;
		return true;
	}	
	
	String iconClass() {
		return this.name?.trim()?.toLowerCase()?.replaceAll(/ /, '_')+'_gall_th';
	}

    static List<SpeciesGroup> list() { 
        return SpeciesGroup.createCriteria().list {
            order('groupOrder', 'asc')
            cache true
        }
    }

    static SpeciesGroup findByName(String whatever) { 
        return SpeciesGroup.createCriteria().get {
            eq 'name', whatever
            cache true
        }
    } 

    List<TaxonomyDefinition> getTaxon() {
        return groupHandlerService.getTaxonByMapping(this);        
    }
}
