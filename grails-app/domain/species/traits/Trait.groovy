package species.traits;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;

class Trait {

    public enum ValueConstraint {
       CATEGORICAL,
       INTEGER,
       FLOAT,
       TEXT,
       BOOLEAN,
       ORDERED,
       RANGE,
       DATE

       static boolean validate(Trait trait, def value) {
           if(!value) return false;
           try {
               switch(this) {
                   case CATEGORICAL : //TODO:CHK in trait values table. value should be one of the value
                   break;
                   case INTEGER : Integer.parseInt(value) 
                   break;
                   case FLOAT : Double.parseDouble(value) 
                   break;
                   case TEXT : 
                   break;
                   case BOOLEAN : Boolean.parseBoolean(value)
                   break;
                   case ORDERED :
                   break;
                   case RANGE : return value.indexOf('-') != -1
                   break;
                   case DATE : return UtilsService.parseDate(value) != null
                   break;
               }
           } catch(Exception e) {
               return false;
           }
           return true;
       }

       static ValueConstraint getEnum(value){
           if(!value) return null;

           if(value instanceof ValueConstraint)
               return value

               value = value.toUpperCase().trim()
               switch(value){
                   case 'CATEGORICAL':
                   return ValueConstraint.CATEGORICAL
                   case 'INTEGER':
                   return ValueConstraint.INTEGER
                   case 'FLOAT':
                   return ValueConstraint.FLOAT
                   case 'TEXT':
                   return ValueConstraint.TEXT
                   case 'BOOLEAN':
                   return ValueConstraint.BOOLEAN
                   case 'ORDERED':
                   return ValueConstraint.ORDERED
                   case 'RANGE':
                   return ValueConstraint.RANGE
                   case 'DATE':
                   return ValueConstraint.DATE
                   default:
                   return null;	
               }
       }
    }

    String name;
    Field field;
    String ontologyUrl;
    String description;
    Date createdOn = new Date();
	  Date lastRevised = createdOn;
    static hasMany = [taxonomyDefinition: TaxonomyDefinition,valueContraints:ValueConstraint]

    static constraints = {
        name nullable:false, blank:false, unique:true
        field nullable:false
        ontologyUrl nullable:true
		description nullable:true
    }

    static mapping = {
        description type:"text"
    }
}
