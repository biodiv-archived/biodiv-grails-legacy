package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;
import species.Classification;
import species.SynonymsMerged;
import grails.util.Holders;

class Trait {


        public enum TraitTypes implements org.springframework.context.MessageSourceResolvable{
            SINGLE_CATEGORICAL("Single Categorical"),
            MULTIPLE_CATEGORICAL("Multiple Categorical"),
            BOOLEAN("Boolean"),
            RANGE("Range"),
            DATE("Date"),

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
                    BOOLEAN,
                    RANGE,
                    DATE
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
                BOOLEAN
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
    }

    static mapping = {
        description type:"text"
        id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "trait_id_seq"] 
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
            if(type.name().equals(units)) {
                return type;
            }
        }
        return null;
    }

    static Trait getValidTrait(String traitName, TaxonomyDefinition taxonConcept) {
        List<Trait> traits = Trait.findAllByNameIlike(traitName);
        if(!traits) {
            println "No trait with name ${traitName}";
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
            traits.each { trait ->
                boolean s = false;
                ibpParentTaxon.each { t ->
                    trait.taxon.each { taxon ->
                        println taxon.id
                        if(taxon.id == t.id)
                            validTraits << trait;
                        s = true
                    }
                }
                if(!s) {
                    if(!trait.taxon) {
                        //Root level traits
                        validTraits << trait;
                    }
                }
            }
        } else {
            println "No IBP parent taxon for  ${taxonConcept}"
        }

        if(validTraits) {
            return validTraits[0];
        } else {
            println "No trait defined with name ${traitName} at taxonscope ${ibpParentTaxon}";
            return null;
        }
    }

    static boolean isValidTrait(Trait trait, List<TaxonomyDefinition> taxonConcepts) {
        boolean isValid = true;
        taxonConcepts. each {
            isValid = isValid || isValidTrait(trait, it);
        }
        return isValid;
    }

    static boolean isValidTrait(Trait trait, TaxonomyDefinition taxonConcept) {
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
            ibpParentTaxon.each { t ->
                trait.taxon.each { taxon ->
                    if(trait.taxon.id == t.id)
                       isValid = true;
                }
            }
        }
        return isValid;
    }

    List values() {
        return TraitValue.findAllByTrait(this);
    }
}
