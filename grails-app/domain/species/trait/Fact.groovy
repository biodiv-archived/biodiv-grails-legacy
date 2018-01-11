package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;
import species.auth.SUser;
import species.License;
import species.dataset.DataTable;
import species.trait.Trait.DataTypes;

import grails.converters.JSON

class Fact {

    Trait trait;
    TraitValue traitValue;
    String value;//default from value if trait type is range
    String toValue;
    Date fromDate;
    Date toDate;
    String attribution;
    SUser contributor;
    License license;
    TaxonomyDefinition pageTaxon;
    Long objectId;
    String objectType
    DataTable dataTable

    boolean isDeleted = false;

     static constraints = {
      trait nullable:false, unique:['objectType', 'objectId','traitValue']
      //attribution nullable:true
      //contributor nullable:true
      //license nullable:true
      traitValue nullable:true
      value nullable:true
      toValue nullable:true
      fromDate nullable:true
      toDate nullable:true
      objectId nullable:false
      objectType nullable:false
      pageTaxon nullable:true
      dataTable nullable:true
    }

    static mapping = {
        description type:"text"
        attribution type:"text"
        id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "fact_id_seq"] 
    }

    String getActivityDescription() {
        if(this.traitValue) {
            return trait.name +':'+ traitValue.value;
        } else if (trait.dataTypes == DataTypes.DATE) {
            return trait.name +':'+ fromDate + (toDate ? "-" + toDate:'')
        }else {
            return trait.name +':'+ value + (toValue ? "-" + toValue:'')
        }
    }

    String getIcon() {
        if(this.traitValue) {
            return traitValue.mainImage()?.fileName
        } else if (trait.dataTypes == DataTypes.DATE) {
            return fromDate + (toDate ? ":" + toDate:'')
        }else {
            return value + (toValue ? ":" + toValue:'')
        }
    }

    def fetchChecklistAnnotation(){
        def res = this as JSON;
        res['id'] = objectId;
        res['type'] = objectType;
        def species = pageTaxon.findSpecies(); 
        res['speciesid'] = species.id
        res['title'] = getActivityDescription();
        return res
    }

    @Override
    String toString() {
        return "<${this.class} : ${id} - (${objectType}:${objectId}, ${trait.name}, ${traitValue?traitValue.value:value}${toValue?'-'+toValue:''})>";
    }
}
