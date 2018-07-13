package species.groups

import javassist.bytecode.stackmap.BasicBlock.Catch;
import grails.converters.JSON
import species.auth.SUser
import species.participation.Observation
import species.dataset.DataPackage

class CustomField {
	public static final String PREFIX = 'CustomField_'
	public static final String SQL_PREFIX = 'cf_'
	
	def customFieldService
	def utilsService
	
	public enum DataType {
		INTEGER ("Integer"),
		DECIMAL ("DECIMAL"),
		TEXT("Text"),
		PARAGRAPH_TEXT("Paragraph_text"),
		DATE("Date"),
		BOOLEAN("Boolean")
		
		private String value;

		DataType(String value) {
			this.value = value;
		}

		String value() {
			return this.value;
		}
		static List toList() {
			return [INTEGER, DECIMAL, TEXT, PARAGRAPH_TEXT, DATE]
		}
		
		static DataType getDataType(String value){
			switch(value){
				case 'INTEGER':
					return DataType.INTEGER
				case 'DECIMAL':
					return DataType.DECIMAL
				case 'BOOLEAN':
					return DataType.BOOLEAN
				case 'DATE':
					return DataType.DATE
				case 'TEXT':
					return DataType.TEXT
				case 'PARAGRAPH_TEXT':
					return DataType.PARAGRAPH_TEXT
				default:
					return null
			}	
		}
	}

	
	String name
	DataType dataType
	boolean isMandatory = false
	
	//to enable multiple value selection using multiple checkbox
	boolean allowedMultiple = false
	
	//used for mandatory column 
	String defaultValue
	
	// to store possible values for radio button or dropdown 
	String options
	
	//for ui display order
	int displayOrder = 0
	
	SUser author
	String notes
	
	//if true then any logged in user can change
	boolean allowedParticipation = false
	
	static belongsTo = [userGroup: UserGroup]
	
	static constraints = {
		name (nullable: false, unique:['userGroup'])
		notes nullable:true
		options nullable:true
		defaultValue nullable:true
    }
	
	static mapping = {
		version : false
		notes type:'text'
		options type:'text'
        cache usage: 'nonstrict-read-write', include: 'non-lazy'
	}
	
	def List fetchOptions(){
		return options? options.split(","):[]
		//return JSON.parse(options)
	}
	
	def static fetchCustomFields(UserGroup ug){
		return CustomField.findAllByUserGroup(ug, [sort: "id", order: "asc"])
	}
	
	def Map fetchPSQLType(String value = null){
		String psqlType
		def defVal = value ? value.trim() : defaultValue
		
		switch(dataType){
			case DataType.INTEGER:
				psqlType = ' bigint '
				try{
					defVal = defVal ? Integer.parseInt(defVal): null
				}catch(e){
					log.error e.message
					defVal = null
				}
				break
			case DataType.DECIMAL:
				psqlType = ' real '
				try{
					defVal = defVal ? Float.parseFloat(defVal):null
				}catch(e){
					log.error e.message
					defVal = null
				}
				break
			case DataType.BOOLEAN:
				psqlType = ' boolean '
				defVal = defVal ? Boolean.parseBoolean(defVal): false
				break

			case DataType.DATE:
				psqlType = ' timestamp without time zone '
				defVal =  parseDate(defVal)
				break
			
			case DataType.PARAGRAPH_TEXT:
				psqlType = ' text '
				defVal = defVal ?: ""
				break
			
			case DataType.TEXT:
				psqlType = ' character varying(400) '
				defVal = defVal ?: ""
				break
				
			default:
				psqlType = ' character varying(255) '
				defVal = defVal ?: ""
				break
		}
		
		return [psqlType:psqlType, defaultValue : defVal]
	}
	
	def fetchTypeCastValue(String value){
		return fetchPSQLType(value).defaultValue
	}
	
	def fetchValue(observationId){
		return customFieldService.fetchValue(this, observationId)
	}

	def fetchSqlColName(){
		return SQL_PREFIX + id 
	}
	
	private parseDate( val){
		Date date = utilsService.parseDate(val)
		if(date)
			return new java.sql.Timestamp(date.getTime())
		
	}

    public void setDataType(dataType) {
        this.dataType = dataType;
    }

    boolean isValidValue(val) {
        boolean isValid = false;
        if(options) {            
            options.split(',').each {
                if(it.equalsIgnoreCase(val)) isValid = true
            }
            return isValid;
        } else {
            return true;
        }
    }
}
