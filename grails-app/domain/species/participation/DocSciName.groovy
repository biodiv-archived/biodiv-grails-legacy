package species.participation

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import content.eml.Document;


class DocSciName {
	Document document;
	String scientificName;
	int frequency;
	String offsetValues;

	static constraints = {
		offsetValues (size:0..2000)
	}

    public static boolean speciesHasDocuments(speciesInstance) {
        if(DocSciName.findByScientificName(speciesInstance.taxonConcept.canonicalForm)) {
            return true;
        } else {
            return false;
        }
    }
}
