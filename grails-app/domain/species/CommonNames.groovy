package species

class CommonNames {

	String name;
	Language language;
	TaxonomyDefinition taxonConcept;

    static constraints = {
		name(blank:false, nullable:false, unique:['language','taxonConcept']);
		language(nullable:true);
    }

	static mapping = {
		version false;
	}
}
