package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;
import species.Classification;
import species.SynonymsMerged;
import grails.util.Holders;
import species.dataset.DataTable;
import grails.converters.JSON

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

//@Cache(region="trait", include = "non-lazy")
//@JsonIgnoreProperties([])
class Trait {


        public enum TraitTypes implements org.springframework.context.MessageSourceResolvable{
            SINGLE_CATEGORICAL("Single Categorical"),
            MULTIPLE_CATEGORICAL("Multiple Categorical"),
            RANGE("Range"),

            private String value;


            TraitTypes(String value) {
                this.value = value;
            }

            public String value() {
                return this.value;
                }

            static def toList() {
                return [
                    SINGLE_CATEGORICAL,
                    MULTIPLE_CATEGORICAL,
                    RANGE
                ]
            }

            Object[] getArguments() { [] as Object[] }

            String[] getCodes() {
            ["${getClass().name}.${name()}"] as String[]
            }   

            String getDefaultMessage() { value() }
        }

        public enum DataTypes implements org.springframework.context.MessageSourceResolvable{
            STRING("String"),
            DATE("Date"),
            NUMERIC("Numeric"),
            BOOLEAN("Boolean"),
            COLOR("Color"),

        private String value;

        DataTypes(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

        static def toList() {
            return [
                STRING,
                DATE,
                NUMERIC,
                BOOLEAN,
                COLOR
            ]
        }

        Object[] getArguments() { [] as Object[] }

        String[] getCodes() {
            ["${getClass().name}.${name()}"] as String[]
        } 

        String getDefaultMessage() { value() }
    }

        public enum Units implements org.springframework.context.MessageSourceResolvable{
            CM("cm"),
            M3("m³"),
            MM("mm"),
            MONTH("month");

        private String value;


        Units(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

        static def toList() {
            return [
                CM,
                M3,
                MM, MONTH
            ]
        }

        Object[] getArguments() { [] as Object[] }

        String[] getCodes() {

            ["${getClass().name}.${name()}"] as String[]
        }   
        String getDefaultMessage() { value() }
    }


    Units units;
    TraitTypes traitTypes;
    DataTypes dataTypes;
    String name;
    //String values;
    String source
    String icon;
    Field field;
    String ontologyUrl;
    String description;
    Date createdOn = new Date();
    Date lastRevised = createdOn;
    //TaxonomyDefinition taxon;

    boolean isDeleted = false;
    boolean isNotObservationTrait = false;
    boolean isParticipatory = true;
    boolean showInObservation = false;

    DataTable dataTable;

    static hasMany = [taxon:TaxonomyDefinition]

    static constraints = {
        name nullable:false, blank:false
        //values nullable:true,blank:true
        source nullable:true
        icon nullable:true
        field nullable:false
        ontologyUrl nullable:true
        description nullable:true
        units nullable:true
        traitTypes nullable:false
        dataTypes nullable:false
        dataTable nullable:true
/*      taxon validator : { val, obj ->
			val && val.size() > 0 
		}
*/    }

    static mapping = {
        description type:"text"
        id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "trait_id_seq"] 
        //cache include: 'non-lazy'
    }

    static TraitTypes fetchTraitTypes(String traitTypes){
        if(!traitTypes) return null;
        for(TraitTypes type : TraitTypes) {
            if(type.value().equalsIgnoreCase(traitTypes)) {
                return type;
            }
        }
        return null;
    }

    static DataTypes fetchDataTypes(String dataTypes){
        if(!dataTypes) return null;
        for(DataTypes type : DataTypes) {
            if(type.value().equalsIgnoreCase(dataTypes)) {
                return type;
            }
        }
        return null;
    }

    static Units fetchUnits(String units){
        if(!units) return null;
        for(Units type : Units) {
            if(type.name().equalsIgnoreCase(units)) {
                return type;
            }
        }
        return null;
    }

    @JsonIgnore
    static Trait getValidTrait(String name, TaxonomyDefinition taxonConcept) {
        List<Trait> traits = Trait.findAllByNameIlike(name);
        if(!traits) {
            println "No trait with name ${name}";
            return null;
        }

        List<Trait> validTraits = [];

        String ibpClassificationName = Holders.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY;
        def classification = Classification.findByName(ibpClassificationName);
        def ibpParentTaxon;
        if(taxonConcept instanceof SynonymsMerged) {
            def acceptedTaxonConcept = taxonConcept.fetchAcceptedNames()[0];
            if(acceptedTaxonConcept) {
                ibpParentTaxon = acceptedTaxonConcept.parentTaxonRegistry(classification).values()[0];
            }
        } else {
            ibpParentTaxon = taxonConcept.parentTaxonRegistry(classification).values()[0];
        }

        if(ibpParentTaxon) {
            traits.each { traitInstance ->
                boolean s = false;
                ibpParentTaxon.each { t ->
                    traitInstance.taxon.each { taxon ->
                        println taxon.id
                        if(taxon.id == t.id)
                            validTraits << traitInstance;
                        s = true
                    }
                }
                if(!s) {
                    if(!traitInstance.taxon) {
                        //Root level traits
                        validTraits << traitInstance;
                    }
                }
            }
        } else {
            println "No IBP parent taxon for  ${taxonConcept}"
        }

        if(validTraits) {
            return validTraits[0];
        } else {
            println "No trait defined with name ${name} at taxonscope ${ibpParentTaxon}";
            return null;
        }
    }

    static boolean isValidTrait(Trait traitInstance, List<TaxonomyDefinition> taxonConcepts) {
        boolean isValid = true;
        taxonConcepts. each {
            isValid = isValid || isValidTrait(traitInstance, it);
        }
        return isValid;
    }

    static boolean isValidTrait(Trait traitInstance, TaxonomyDefinition taxonConcept) {
        boolean isValid = false;
        String ibpClassificationName = Holders.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY;
        def classification = Classification.findByName(ibpClassificationName);
        def ibpParentTaxon;
        if(taxonConcept instanceof SynonymsMerged) {
            def acceptedTaxonConcept = taxonConcept.fetchAcceptedNames()[0];
            if(acceptedTaxonConcept) {
                ibpParentTaxon = acceptedTaxonConcept.parentTaxonRegistry(classification).values()[0];
            }
        } else {
            ibpParentTaxon = taxonConcept.parentTaxonRegistry(classification).values()[0];
        }

        if(ibpParentTaxon) {
            println "Found ibp parent classification to be ${ibpParentTaxon}";
            println "trait taxon ${traitInstance.taxon}";
            ibpParentTaxon.each { t ->
                println t.id
                traitInstance.taxon.each { taxon ->
                    println taxon.id
                    if(taxon.id == t.id) {
                       isValid = true;
                       println "isValid ${isValid}"
                       return;
                    }
                }
                if(isValid) return;
                       println "2isValid ${isValid}"
            }
                       println "3isValid ${isValid}"
        }
        return isValid;
    }

    List values() {
        //find 'from TraitValue tv where tv.traitInstance.id = :traitId', ['traitId':this.id]; 
        return TraitValue.findAllByTraitInstance(this);
    }

    def fetchChecklistAnnotation(){
        def res = [:];
        res['title'] = this.name;
        res['id'] = this.id;
        res['type'] = 'trait';
        res['values'] = this.values();
        res['units'] = this.units;
        res['traitTypes'] = this.traitTypes;
        res['dataTypes'] = this.dataTypes;
        res['field'] = this.field.toString();
        res['taxon'] = this.taxon.collect{it.name}.join(',');
        res['isNotObservationTrait'] = this.isNotObservationTrait;
        res['isParticipatory'] = this.isParticipatory;
        res['showInObservation'] = this.showInObservation;
        return res
    }

    public void setUnits(units) {
        println "units##########################"
        println "##########################"
        println "##########################"
        println "##########################"
        println "##########################"
 
        this.units = units;
    }

    public void setDataTypes(dataTypes) {
        this.dataTypes = dataTypes;
    }

    public void setTraitTypes(traitTypes) {
        this.traitTypes = traitTypes;
    }
}
