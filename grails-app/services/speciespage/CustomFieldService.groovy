package speciespage

import groovy.sql.Sql
import grails.converters.JSON

import species.groups.CustomField
import species.groups.UserGroup
import species.participation.Observation
import species.auth.SUser

class CustomFieldService {
	
	def utilsService
	def dataSource
	def springSecurityService
	
	def List fetchAllCustomFields(Observation obv){
		List result = []
		obv.userGroups.collect{it}.each { ug ->
			CustomField.fetchCustomFields(ug).each { cf ->
				def val = fetchValue(cf, obv.id)
				if(val && (cf.dataType == CustomField.DataType.DATE)){
					val = val.format('MMMM d, y')
				}
				result << [key:cf.name, value : val]
			}
		}
		return result
	}
	
	
	def fetchValue(CustomField cf,  observationId){
		if(!observationId)
			return cf.defaultValue
	
		String query =  ''' select ''' +  cf.fetchSqlColName() + ''' as resValue from ''' + utilsService.getTableNameForGroup(cf.userGroup) + ''' where observation_id = :observationId '''
		Sql sql = Sql.newInstance(dataSource)
		def result = sql.rows(query, ['observationId': observationId])
		if(!result.isEmpty()){
			return result[0].resValue
		}
		return ""
	}
	
	def updateCustomFields(params, obvId){
		UserGroup ug = UserGroup.findByWebaddress(params.webaddress)
		
		if(!ug || CustomField.fetchCustomFields(ug).isEmpty()){
			return
		}
		
		def  customFieldMap = [:]
		params.each { String k, v ->
			if(k.startsWith(CustomField.PREFIX)){
				String cfName = k.replace(CustomField.PREFIX, "")
				CustomField cf = CustomField.findByUserGroupAndName(ug, cfName)
				if(v && !(v instanceof String)){
					v = v.join(",")
				}
				customFieldMap.put(cf.fetchSqlColName(), cf.fetchTypeCastValue(v))
			}
		}
		
		deleteRow(ug, obvId)
		customFieldMap.observation_id = obvId

		insertRow(ug, customFieldMap)
		
	}
	
	
	def addToGroup(cutomFieldMapList, UserGroup ug){
		def cfList = []
		def mList = JSON.parse(cutomFieldMapList)
		def author = springSecurityService.currentUser;
		mList.each { m ->
			if(m.name && m.dataType){
				def dataType = CustomField.DataType.getDataType(m.dataType) 
				boolean allowedMultiple = (dataType == CustomField.DataType.TEXT)?m.allowedMultiple:false
				def options = m.options? m.options.split(",").collect{it.trim()}.join(","):""
				CustomField cf =  new CustomField(userGroup:ug, name:m.name, dataType:dataType, isMandatory:m.isMandatory, allowedMultiple:m.allowedMultiple, defaultValue:m.defaultValue, options:options, notes:m.description, author:author)
				if(!cf.save(flush:true)){
					cf.errors.allErrors.each { log.error it }
				}else{
					cfList << cf
				}
			}else{
				log.debug "Either name or type is missing ${m}"
			}
		}
		
		log.debug "Final Custom field list " + cfList
		
		cfList.each { cf ->
			try{
				addColumn(cf)
			}catch(e){
				log.error e.message
			}
		}
	}
	
	def addColumn(CustomField cf){
		createTable(cf.userGroup)
		Map m = cf.fetchPSQLType()
		String type = m.psqlType
		String columnConstrain = ""
		def queryParams
		if(cf.isMandatory){
			columnConstrain = ''' NOT NULL '''
		}
		
		String tableName = utilsService.getTableNameForGroup(cf.userGroup)
		String query = '''ALTER TABLE ''' + tableName + ''' ADD COLUMN  ''' + cf.fetchSqlColName()  + ''' ''' +  type  //+ columnConstrain
		return executeQuery(query)
	}

	
	def dropColumn(CustomField cf){
		String tableName = utilsService.getTableNameForGroup(cf.userGroup)
		String query = ''' ALTER TABLE ''' + tableName + ''' DROP COLUMN IF EXISTS ''' +  cf.fetchSqlColName()
		return executeQuery(query)
	}
	
	def createTable(UserGroup ug){
		String tableName = utilsService.getTableNameForGroup(ug)
		if(notTableExist(tableName)){
			def query = ''' CREATE TABLE IF NOT EXISTS ''' + tableName + '''( observation_id bigint not null)'''
			def foreignKeyQuery = ''' ALTER TABLE ''' + tableName + '''  ADD CONSTRAINT observation_id FOREIGN KEY (observation_id) REFERENCES observation(id) ON DELETE CASCADE '''
			executeQuery(query)
			executeQuery(foreignKeyQuery)
		}
	}
	
	private boolean executeQuery(query, queryParams = null){
		log.debug "----------------------------------------------------"
		log.debug query
		log.debug queryParams
		log.debug "----------------------------------------------------"
		
		Sql sql = Sql.newInstance(dataSource)
		boolean isSuccess = queryParams ? sql.execute(queryParams, query):sql.execute(query)
//		if(isSuccess){
//			log.error "Query Successful >>>> ${query}  :::: params  ${queryParams}" 
//		}else{
//			log.debug " QueryFailed >>>> ${query}  :::: params  ${queryParams}"
//		}
		return isSuccess
	}
	
	private boolean notTableExist(String tableName){
		def query = ''' SELECT * FROM   information_schema.tables 
						WHERE  table_catalog = :dbName AND table_name = :tableName '''
		
		Sql sql = Sql.newInstance(dataSource)
		return sql.rows(query, [tableName:tableName, dbName:'biodiv']).isEmpty()		
	}
	


	
	private boolean deleteRow(ug, obvId){
		String query = "delete from "+ utilsService.getTableNameForGroup(ug) + " where observation_id =:observationId "
		return executeQuery(query, [observationId:obvId] )
	}

	private boolean insertRow(ug, Map m){
		List keyList = m.keySet().collect{it}
		
		String query = "insert into "+ utilsService.getTableNameForGroup(ug) +  " ( " +   keyList.collect{it}.join(", ") + " ) "   +  " values ( " + keyList.collect{":" + it}.join(", ") +  ") "
		return executeQuery(query, m)
	}
		
	
	def test1(){
		def m = [webaddress : 'bangalore_birdrace_2013', 'CustomField_Name' : 'sandeep', 'CustomField_Age': '100', 'CustomField_Status' : 'false']
		updateCustomFields(m, 336296)
	}
	
	def test(){
		//test1()
		println "============= " + fetchAllCustomFields(Observation.read(368571))
//		addCf()
//		createTable(UserGroup.read(2))
//		List l = CustomField.fetchCustomFields(UserGroup.read(2))
//		l.each {
//			addColumn(it)
//		}
	}
	
	def List addCf(){
		UserGroup ug = UserGroup.read(2)

		CustomField cf = new CustomField(userGroup:ug, name:'Name', dataType:CustomField.DataType.STRING, isMandatory:true, defaultValue:'Nitin', author:SUser.read(1))
		if(!cf.save(flush:true)){
			cf.errors.allErrors.each { log.error it }
		}

		
		cf = new CustomField(userGroup:ug, name:'Age', dataType:CustomField.DataType.INT, isMandatory:true, defaultValue:'10', author:SUser.read(1))
		if(!cf.save(flush:true)){
			cf.errors.allErrors.each { log.error it }
		}
		
		cf = new CustomField(userGroup:ug, name:'Status', dataType:CustomField.DataType.BOOLEAN, isMandatory:false, defaultValue:'false', author:SUser.read(1))
		if(!cf.save(flush:true)){
			cf.errors.allErrors.each { log.error it }
		}
		
		return [cf]
	}
	
	
}


/*
def c = ctx.getBean("customFieldService");


def m = [webaddress : 'bangalore_birdrace_2013', 'CustomField_name' : 'sandeep', 'CustomField_age': '100', 'CustomField_status' : 'false']
c.updateCustomFields(m, 336296)
*/