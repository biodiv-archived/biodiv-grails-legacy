package species

class SpeciesGroup {

	String name;
	SpeciesGroup parentGroup;
	
	static hasMany = [taxonConcept:TaxonomyDefinition]
	
    static constraints = {
		name(blank:false, unique:true);
		parentGroup nullable:true;
    }
	
	static mapping = {
		version  false;
		sort name:"asc"
	}
}
