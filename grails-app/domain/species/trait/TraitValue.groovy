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
    TaxonomyDefinition taxon;

    static constraints = {
        trait nullable:false, blank:false, unique:['value']
        value nullable:false
		description nullable:true
        icon nullable:true
        source nullable:false
        taxon nullable:false
    }

    static mapping = {
        description type:"text"
    }
}
