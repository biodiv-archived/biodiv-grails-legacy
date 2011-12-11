package species

class TaxonomyDefinition {

	public enum TaxonomyRank {
		KINGDOM("Kingdom"),
		PHYLUM("Phylum"),
		CLASS("Class"),
		ORDER("Order"),
		FAMILY("Family"),
		SUB_FAMILY("Sub-Family"),
		GENUS("Genus"),
		SUB_GENUS("Sub-Genus"),
		SPECIES("Species");

		private String value;

		TaxonomyRank(String value) {
			this.value = value;
		}

		static list() {
			[
				KINGDOM,
				PHYLUM,
				CLASS,
				ORDER,
				FAMILY,
				SUB_FAMILY,
				GENUS,
				SUB_GENUS,
				SPECIES
			]
		}

		String value() {
			return this.value;
		}

		String toString() {
			return this.value();
		}
	}

	int rank;
	String name;
	String canonicalForm;
	String normalizedForm;
	String italicisedForm;
	String binomialForm;
	SpeciesGroup group;

	static hasMany = [author:String, year:String]
	
	static constraints = {
		name(blank:false)
		canonicalForm (nullable:true, unique:['rank']);
		normalizedForm nullable:true;
		italicisedForm nullable:true;
		binomialForm nullable:true;
		group nullable:true;
	}
	
	static mapping = {
		sort "rank"
		version false;
	}

	Long findSpeciesId() {
		return Species.findByTaxonConcept(this)?.id;
	}
}
