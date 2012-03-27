package species

class Synonyms {

	public enum RelationShip {
		
		AMBIGUOUS_SYNONYM("Ambiguous Synonym"),
		ANAMORPH("Anamorph"),
		BASIONYM("Basionym"),
		HETEROTYPIC_SYNONYM("Heterotypic Synonym"),
		HOMOTYPIC_SYNONYM("Homotypic Synonym"),
		JUNIOR_SYNONYM("Junior Synonym"),
		MISAPPLIED_NAME("Misapplied Name"),
		NOMENCLATURAL_SYNONYM("Nomenclatural Synonym"),
		OBJECTIVE_SYNONYM("Objective Synonym"),
		SENIOR_SYNONYM("Senior Synonym"),
		SUBJECTIVE_SYNONYM("Subjective Synonym"),
		SYNONYM("Synonym"),
		TELEOMORPH("Teleomorph"),
		UNAVAILABLE_NAME("Unavailable Name"),
		VALID_NAME("Valid Name"),
		ACCEPTED_NAME("Accepted Name"),
		PROTONYM("Protonym"),
		OTHERS("Other");
		
		private String value;
		
		RelationShip(String value) {
			this.value = value;
		}
		
		String value() {
			return this.value;
		}
	};

	String name;
	RelationShip relationship;
	TaxonomyDefinition taxonConcept;
	String canonicalForm;
	String normalizedForm;
	String italicisedForm;
	String binomialForm;
	
	static constraints = {
		name(blank : false);
		canonicalForm(blank : false, nullable:false, unique:['relationship', 'taxonConcept']);
		relationship(nullable:true);
		normalizedForm nullable:true;
		italicisedForm nullable:true;
		binomialForm nullable:true;
    }
	
	static mapping = {
		version false;
	}
}
