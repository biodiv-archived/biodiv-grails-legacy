package content.eml
import species.Species
import species.TaxonomyDefinition;

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;


class DocSciName {
	Document document;
	String scientificName;
	int frequency;
	String offsetValues;
	String canonicalForm;
	int displayOrder;
	int primary_name
    TaxonomyDefinition taxonConcept;
    boolean isDeleted;

	static constraints = {
		offsetValues (size:0..2000)
		canonicalForm nullable:true
		taxonConcept nullable:true
	}
    static fetchMode = [taxonConcept: 'eager']
    public static boolean speciesHasDocuments( Species speciesInstance) {
        if(DocSciName.findByScientificName(speciesInstance.taxonConcept.canonicalForm)) {
            return true;
        } else {
            return false;
        }
    }
}
