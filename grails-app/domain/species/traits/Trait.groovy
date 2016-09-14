package species.traits;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;

class Trait {

    public enum TraitTypes {
       SINGLE_CATEGORICAL,
       MULTIPLE_CATEGORICAL,
       BOOLEAN,
       RANGE,
       DATE

/*       static boolean validate(Trait trait, def value) {
           if(!value) return false;
           try {
               switch(this) {
                   case SINGLE_CATEGORICAL : //TODO:CHK in trait values table. value should be one of the value
                   break;
                   case MULTIPLE_CATEGORICAL : //TODO:CHK in trait values table. value should be one of the value
                   break;
                   case CATEGORICAL_ORDERED : Integer.parseInt(value) 
                   break;
                   case NUMERIC_SINGLE_DECIMAL : Double.parseDouble(value) 
                   break;
                   case NUMERIC_SINGLE_INTEGER : Double.parseDouble(value) 
                   break;
                   case NUMERIC_RANGE_DECIMAL : 
                   break;
                   case NUMERIC_RANGE_INTEGER : 
                   break;
                   case BOOLEAN : Boolean.parseBoolean(value)
                   break;
                   case DATE :
                   break;
                   case DATE_RANGE : return value.indexOf('-') != -1
                   break;
                   //case DATE : return UtilsService.parseDate(value) != null
                   //break;
               }
           } catch(Exception e) {
               return false;
           }
           return true;
       }
*/
       static TraitTypes getEnum(value){
           if(!value) return null;

           if(value instanceof TraitTypes)
               return value

               value = value.toUpperCase().trim()
               switch(value){
                   case 'SINGLE_CATEGORICAL':
                   return TraitTypes.SINGLE_CATEGORICAL
                   case 'MULTIPLE_CATEGORICAL':
                   return TraitTypes.MULTIPLE_CATEGORICAL
                   case 'BOOLEAN':
                   return TraitTypes.BOOLEAN
                   case 'DATE':
                   return TraitTypes.DATE
                   case 'RANGE':
                   return TraitTypes.RANGE
                   default:
                   return null;	
               }
       }
    }
    public enum DataTypes {
       STRING,
       DATE,
       NUMERIC,
       BOOLEAN


       static DataTypes getEnum(value){
           if(!value) return null;

           if(value instanceof DataTypes)
               return value

               value = value.toUpperCase().trim()
               switch(value){
                   case 'STRING':
                   return DataTypes.STRING
                   case 'DATE':
                   return DataTypes.DATE
                   case 'BOOLEAN':
                   return DataTypes.BOOLEAN
                   case 'NUMERIC':
                   return DataTypes.NUMERIC
                   default:
                   return null; 
               }
       }
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
                M3
      ]
    }

        Object[] getArguments() { [] as Object[] }

        String[] getCodes() {

            ["${getClass().name}.${name()}"] as String[]
        }   
        String getDefaultMessage() { value() }
    }

    Units units
    TraitTypes traitTypes
    DataTypes dataTypes
    String name;
    String values;
    String source
    String icon
    Field field;
    String ontologyUrl;
    String description;
    Date createdOn = new Date();
	  Date lastRevised = createdOn;
    static hasMany = [taxonomyDefinition: TaxonomyDefinition]

    static constraints = {
        name nullable:false, blank:false, unique:true
        values nullable:true,blank:true
        source nullable:true
        icon nullable:true
        field nullable:false
        ontologyUrl nullable:true
		    description nullable:true
        units nullable:true
        traitTypes nullable:true
        dataTypes nullable:true
    }

    static mapping = {
        description type:"text"
    }
    static Units fetchUnits(def units){
    if(!units) return null;
    for(Units unit : Units) {
      if(unit.value().equals(units)) {
        return unit
      }
    }
    return null;
  }
}
