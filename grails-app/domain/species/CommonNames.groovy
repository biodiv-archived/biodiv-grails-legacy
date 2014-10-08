package species

class CommonNames extends NamesMetadata {

	String name;
	Language language;
	TaxonomyDefinition taxonConcept;

	//i.e. aam
	String transliteration;
	
    static constraints = {
		name(blank:false, nullable:false, unique:['language','taxonConcept']);
		language(nullable:true);
		transliteration(nullable:true);
    }

	static mapping = {
		version false;
        language sort:'name asc'
	}

    static fetchMode = [language:'eager']
}
