package content.eml
import species.Species

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;


class DocSciName {
	Document document;
	String scientificName;
	int frequency;
	String offsetValues;
	String canonicalForm;

	static constraints = {
		offsetValues (size:0..2000)
		canonicalForm nullable:true
	}

    public static boolean speciesHasDocuments( Species speciesInstance) {
        if(DocSciName.findByScientificName(speciesInstance.taxonConcept.canonicalForm)) {
            return true;
        } else {
            return false;
        }
    }
}
