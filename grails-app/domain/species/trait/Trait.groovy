package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;

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
    TaxonomyDefinition taxon;


    static constraints = {
        name nullable:false, blank:false, unique:['taxon']
        //values nullable:true,blank:true
        source nullable:true
        icon nullable:true
        field nullable:false
        taxon nullable:false
        ontologyUrl nullable:true
        description nullable:true
        units nullable:true
        traitTypes nullable:false
        dataTypes nullable:false
    }

    static mapping = {
        description type:"text"
    }

    static TraitTypes fetchTraitTypes(String traitTypes){
        if(!traitTypes) return null;
        for(TraitTypes type : TraitTypes) {
            if(type.name().equals(traitTypes)) {
                return type;
            }
        }
        return null;
    }

    static DataTypes fetchDataTypes(String dataTypes){
        if(!dataTypes) return null;
        for(DataTypes type : DataTypes) {
            if(type.name().equals(dataTypes)) {
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

    static Trait getValidTrait(String traitName, TaxonomyDefinition taxon) {
        List<Trait> traits = Trait.findAllByName(traitName);
        if(!traits) {
            log.error "No trait with name ${traitName}";
            return null;
        }

        List<Trait> validTraits = [];
        List parentTaxon = taxon.fetchDefaultHierarchy();
        
        parentTaxon.each { t ->
            traits.each { trait ->
                if(trait.taxon.id == t)
                    validTraits << trait;
            }
        }
        if(validTraits) {
            return validTraits;
        } else {
            log.error "No trait defined with name ${traitName} at taxonscope ${parentTaxon}";
            return null;
        }
    }
}
