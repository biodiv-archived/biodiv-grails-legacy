package species.groups

import species.TaxonomyDefinition;

class SpeciesGroup {

	String name;
	SpeciesGroup parentGroup;
	
	static hasMany = [taxonConcept:TaxonomyDefinition, speciesGroupMapping:SpeciesGroupMapping]
	
    static constraints = {
		name(blank:false, unique:true);
		parentGroup nullable:true;
    }
	
	static mapping = {
		version  false;
		sort name:"asc"
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
	
}
