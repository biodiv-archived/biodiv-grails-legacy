package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;

class TraitValue {

    Trait trait;
    String value;
    String description;
    String icon;
    String source;

    static constraints = {
        trait nullable:false, blank:false, unique:true
        value nullable:false, unique:true 
		description nullable:true
        icon nullable:true
        source nullable:false
    }

    static mapping = {
        description type:"text"
    }
}
