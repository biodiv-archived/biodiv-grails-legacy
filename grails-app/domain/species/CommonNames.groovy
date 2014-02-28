package species

class CommonNames extends NamesSorucedata {

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
