package species

import java.util.List;

import species.groups.SpeciesGroup;
import species.utils.Utils;

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
	String threatenedStatus;
	ExternalLinks externalLinks;


	static hasMany = [author:String, year:String]

	static constraints = {
		name(blank:false)
		canonicalForm (nullable:true, unique:['rank']);
		normalizedForm nullable:true;
		italicisedForm nullable:true;
		binomialForm nullable:true;
		group nullable:true;
		threatenedStatus nullable:true;
		externalLinks nullable:true;
	}

	static mapping = {
		sort "rank"
		version false;
	}

	Long findSpeciesId() {
		return Species.findByTaxonConcept(this)?.id;
	}

	void setName(String name) {
		this.name = Utils.cleanName(name);
	}

	/**
	 * Returns parents as per all classifications
	 * @return
	 */
	List<TaxonomyDefinition> parentTaxon() {
		List<TaxonomyDefinition> result = [];
		TaxonomyRegistry.findAllByTaxonDefinition(this).each { TaxonomyRegistry reg ->
			//TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
			reg.path.tokenize('_').each { taxonDefinitionId ->
				result.add(TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId)));
			}
		}
		return result;
	}

	/**
	 * Returns parents as per all classifications
	 * @return
	 */
	Map<Classification, List<TaxonomyDefinition>> parentTaxonRegistry() {
		Map<List<TaxonomyDefinition>> result = [:];
		TaxonomyRegistry.findAllByTaxonDefinition(this).each { TaxonomyRegistry reg ->
			//TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
			def l = []
			reg.path.tokenize('_').each { taxonDefinitionId ->
				l.add(TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId)));
			}
			result.put(reg.classification , l);
		}
		return result;
	}
}
