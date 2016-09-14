package species.traits;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;
import species.auth.SUser;
class TraitFacts {

    Trait trait;
    String traitValue
    String attribution
    SUser contributor
    String license
    TaxonomyDefinition taxon

    static constraints = {
      traitValue nullable:true
      attribution nullable:true
      contributor nullable:true
      license nullable:true
      

    }

    static mapping = {
        description type:"text"
    }
}
