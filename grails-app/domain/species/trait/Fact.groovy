package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;
import species.auth.SUser;
import species.License;

class Fact {

    Trait trait;
    TraitValue traitValue;
    String attribution;
    SUser contributor;
    License license;
    //TaxonomyDefinition taxon;
    Long objectId;

    static constraints = {
      trait nullable:false, unique:['traitValue', 'objectId']
      //attribution nullable:true
      //contributor nullable:true
      //license nullable:true
      objectId nullable:false
    }

    static mapping = {
        description type:"text"
        attribution type:"text"
    }
}
