package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;
import species.auth.SUser;
import species.License;

class Fact {

    Trait trait;
    TraitValue traitValue;
    String value;
    String attribution;
    SUser contributor;
    License license;
    TaxonomyDefinition pageTaxon;
    Long objectId;
    
    boolean isDeleted = false;

    static constraints = {
      trait nullable:false, unique:['pageTaxon', 'objectId','traitValue']
      //attribution nullable:true
      //contributor nullable:true
      //license nullable:true
      value nullable:true
      objectId nullable:false
    }

    static mapping = {
        description type:"text"
        attribution type:"text"
        id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "fact_id_seq"] 
    }

    @Override
    String toString() {
        return "<${this.class} : ${id} - (${pageTaxon.name}, ${trait.name}, ${traitValue.value})>";
    }
}
