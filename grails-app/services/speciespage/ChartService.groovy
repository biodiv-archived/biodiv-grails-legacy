package speciespage

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.Species;
import species.auth.SUser
import species.groups.SpeciesGroup;
import species.groups.UserGroup
import species.participation.Observation

class ChartService {

	static transactional = false
	
	def userGroupService

	def getObservationStats(params, SUser author){
		UserGroup userGroupInstance
		if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
		}
		
		//getting all observation
		def allResult = getFilteredObservationStats(userGroupInstance, author, null)
		
		//getting all observation
		def unidentifiedResult = getFilteredObservationStats(userGroupInstance, author, false)
		
		mergeResult(allResult, unidentifiedResult)
		return allResult
	}
		
	
	/**
	 * 
	 * @param userGroupInstance
	 * @param author
	 * @param identifactionFlag if null then returning all obs, if true then returning only identified if false then returning unidentified
	 * @return
	 */
	private getFilteredObservationStats(UserGroup userGroupInstance,  SUser author, identifactionFlag ){
		
		def result= Observation.withCriteria(){
			projections {
				groupProperty('group')
				rowCount('total') //alias given to count
			}
			and{
				// taking undeleted observation
				eq('isDeleted', false)
				
				//filter by author
				if(author){
					eq('author', author)
				}
				
				//filter by all, unidentifed and identified
				if(identifactionFlag != null){
					(identifactionFlag == false) ? isNull('maxVotedReco') : isNotNull('maxVotedReco')
				}
				
				//filter by usergroup
				if(userGroupInstance){
					userGroups{
						eq('id', userGroupInstance.id)
					}
				}
			}
			
			order 'total', 'desc'
		}
		return getFormattedResult(result)
	}
	
	private getFormattedResult(result){
		def formattedResult = []
		result.each { r ->
			formattedResult.add([r[0].name, r[1]])
		}
		return [data:formattedResult, columns:[['string', 'Species Group'], ['number', 'Count']]]
	}
	

	private mergeResult(allResult, unidentifiedResult){
		
		allResult.data.each{ r ->
			String speciesGroup = r[0]
			int unIdentified = serachInList(unidentifiedResult, speciesGroup)
			
			//adding identified one
			//r.add(r[1] - unIdentified)
			//adding unidentified
			r.add(unIdentified)
		}
		
		allResult.columns = [['string', 'Species Group'], ['number', 'All'], ['number', 'UnIdentified']]
	}
	
	private serachInList(unidentifiedResult, String key){
		for(ur in unidentifiedResult.data){ 
			if((""+ur[0]).equalsIgnoreCase("" + key)){
				return ur[1]
			}
		}
		return 0
	}
	
	
	def getSpeciesPageStats(params){
		def stubCountQuery = "select t.group.id, count(*) as count from Species s, TaxonomyDefinition t where s.taxonConcept = t group by t.group.id";
		def contentCountQuery = "select t.group.id, count(*) as count from Species s, TaxonomyDefinition t where s.taxonConcept = t and s.percentOfInfo > 0.0 group by t.group.id";
		
		def allResult = Species.executeQuery(stubCountQuery)
		def contentResult = [data:Species.executeQuery(contentCountQuery)]
		def finalResult = []
		allResult.each{ r ->
			def sgName = (r[0] ? SpeciesGroup.read(r[0]).name : "Others" )
			int contentCount = serachInList(contentResult, "" + r[0])
			finalResult.add([sgName, contentCount, r[1] - contentCount])
		}
		
		return [data : finalResult, columns : [['string', 'Species Group'], ['number', 'Content'], ['number', 'Stubs']]]
	}
	
	def activeUserStats(params){
		int days = params.days ? params.days.toInteger() : 7
		int max = params.max ? params.max.toInteger() : 10
		
		UserGroup userGroupInstance
		if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
		}
		
		def result = Observation.withCriteria(){
			projections {
				groupProperty('author')
				rowCount('total') //alias given to count
			}
			and{
				// taking undeleted observation
				eq('isDeleted', false)
				ge('createdOn', new Date().minus(days))
				
				//filter by usergroup
				if(userGroupInstance){
					userGroups{
						eq('id', userGroupInstance.id)
					}
				}
			}
			maxResults max
			order 'total', 'desc'
		}
		return [data : result, columns : [['string', 'User'], ['number', 'Observations']]]
	}
	
}
