package species

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.MessageSource
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import grails.util.Holders;

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
		INFRA_SPECIFIC_TAXA("Infraspecies");

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
		
		static TaxonomyRank getTRFromInt(int i){
			return list()[i]
		}

    static int getTaxonRank(String rankStr) {
        MessageSource messageSource = grails.util.Holders.application.mainContext.getBean('messageSource')
        def request = null;
        try {
            request = RequestContextHolder.currentRequestAttributes().request
        } catch (e) {
            //log.debug "No thread bound request"
        }
		
		//XXX: handling rank if name is coming from col
		rankStr = rankStr?.toLowerCase()
		if("superfamily".equals(rankStr)){
			rankStr = "Super-Family"
		}else if("subfamily".equals(rankStr)){
			rankStr = "Sub-Family"
		}else if("subgenus".equals(rankStr)){
			rankStr = "Sub-Genus"
		}

        for(def type : list()) {
            String message = request ? messageSource.getMessage(type.getCodes()[0], null, RCU.getLocale(request)) : type.value()
            if(type.value().equalsIgnoreCase(rankStr) || message.equalsIgnoreCase(rankStr)) {
                return type.ordinal();
            }
        }
        return -1;
    }

        static int nextPrimaryRank(int i) {
            switch (i) {
                case 0:
                return 1;
                break

                case 1:
                return 2;
                break

                case 2:
                return 3;
                break

                case 3:
                return 5;
                break

                case 4:
                return 5;
                break

                case 5:
                return 7;
                break

                case 6:
                return 7;
                break

                case 7:
                return 9;
                break

                case 8:
                return 9;
                break

                case 9:
                return 10;
                break

                case 10:
                return 10;
                break

            }
        }
    }

	String canonicalForm;
	String normalizedForm;
	String italicisedForm;
	String binomialForm;
	
	// current activity description
	String activityDescription;

	static constraints = {
		normalizedForm nullable:true;
		italicisedForm nullable:true;
		binomialForm nullable:true;
		status nullable:true;
		activityDescription nullable:true;
	}
	
	def beforeInsert(){
		super.beforeInsert()
	}
	
}
