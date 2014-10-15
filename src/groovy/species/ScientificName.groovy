package species

import org.springframework.context.MessageSourceResolvable;

abstract class ScientificName extends NamesMetadata {

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

		static def toList() {
			return [
				AMBIGUOUS_SYNONYM,
				ANAMORPH,
				BASIONYM,
				HETEROTYPIC_SYNONYM,
				JUNIOR_SYNONYM,
				MISAPPLIED_NAME,
				NOMENCLATURAL_SYNONYM,
				OBJECTIVE_SYNONYM,
				SENIOR_SYNONYM,
				SUBJECTIVE_SYNONYM,
				SYNONYM,
				TELEOMORPH,
				UNAVAILABLE_NAME,
				VALID_NAME,
				ACCEPTED_NAME,
				PROTONYM,
				OTHERS
			]
		}
	}

	public enum TaxonomyRank implements MessageSourceResolvable {
		KINGDOM("Kingdom"),
		PHYLUM("Phylum"),
		CLASS("Class"),
		ORDER("Order"),
		SUPER_FAMILY("Super-Family"),
		FAMILY("Family"),
		SUB_FAMILY("Sub-Family"),
		GENUS("Genus"),
		SUB_GENUS("Sub-Genus"),
		SPECIES("Species"),
		INFRA_SPECIFIC_TAXA("infra-specific-taxa");

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
				SUPER_FAMILY,
				FAMILY,
				SUB_FAMILY,
				GENUS,
				SUB_GENUS,
				SPECIES,
				INFRA_SPECIFIC_TAXA
			]
		}

		String value() {
			return this.value;
		}

		String toString() {
			return this.value();
		}

		Object[] getArguments() {
			[] as Object[]
		}

		String[] getCodes() {
			["${getClass().name}.${name()}"] as String[]
		}

		String getDefaultMessage() {
			value()
		}
	}

	String canonicalForm;
	String normalizedForm;
	String italicisedForm;
	String binomialForm;

	static constraints = {
		normalizedForm nullable:true;
		italicisedForm nullable:true;
		binomialForm nullable:true;
		status nullable:true;
	}
}
