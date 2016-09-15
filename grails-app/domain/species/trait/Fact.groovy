package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;
import species.auth.SUser;
import species.License;

class Fact {

    Trait trait;
    String traitValue;
    String attribution;
    SUser contributor;
    License license;
    TaxonomyDefinition taxon;

    static constraints = {
      traitValue nullable:true
      attribution nullable:true
      contributor nullable:true
      license nullable:true
    }

    static mapping = {
        description type:"text"
        attribution type:"text"
    }
}
