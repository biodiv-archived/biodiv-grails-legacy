package speciespage

import java.text.SimpleDateFormat
import org.codehaus.groovy.runtime.DateGroovyMethods;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.Species;
import species.auth.SUser
import species.groups.SpeciesGroup;
import species.groups.UserGroup
import species.participation.ActivityFeed;
import species.participation.Observation;

class ChartService {

	static transactional = false

	private static final Date PORTAL_START_DATE = new Date(111, 7, 8)

	def userGroupService
	def activityFeedService
	def observationService
	
	def getObservationStats(params, SUser author, request){
		UserGroup userGroupInstance
		if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
		}

		//getting all observation
		def allResult = getFilteredObservationStats(userGroupInstance, author, null)

		//getting all observation
		def unidentifiedResult = getFilteredObservationStats(userGroupInstance, author, false)

		mergeResult(allResult, unidentifiedResult)
		addHtmlResultForObv(allResult, request)
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
		return [data:formattedResult, columns:[
				['string', 'Species Group'],
				['number', 'Count']
			]]
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

		allResult.columns = [
			['string', 'Species Group'],
			['number', 'All'],
			['number', 'UnIdentified']
		]
	}

	private addHtmlResultForObv(Map res, request){
		List htmlData = []
		res.data.each{ r ->
			htmlData.add([r[0], getHyperLink(r[0], r[1], false, request, true), getHyperLink(r[0], r[2], true, request, true)])
		}
		
		res.htmlData = htmlData
		res.htmlColumns = [
			['string', 'Species Group'],
			['string', 'All'],
			['string', 'UnIdentified']
		]
	}
	
	private getHyperLink(String speciesGroup, count, boolean isUnIdentified, request, boolean isObv){
		def fitlerParams = [:]
		if(isUnIdentified){
			if(isObv){
				fitlerParams.speciesName="Unknown"
			}
		}
		fitlerParams.sGroup = SpeciesGroup.findByName(speciesGroup).id
		def link = observationService.generateLink((isObv)?"observation":"species", "list", fitlerParams, request)
		return "" + '<a href="' +  link +'">' + count + "</a>"
	}
	
	
	private getHyperLinkForUser(userId, Date startDate, count, request){
		def fitlerParams = [:]
		if(startDate){
			fitlerParams.daterangepicker_start= new SimpleDateFormat("dd/MM/yyyy").format(startDate)
		}
		fitlerParams.user = userId
		def link = observationService.generateLink("observation", "list", fitlerParams, request)
		return "" + '<a href="' +  link +'">' + count + "</a>"
	}
	
	private serachInList(unidentifiedResult, String key){
		for(ur in unidentifiedResult.data){
			if((""+ur[0]).equalsIgnoreCase("" + key)){
				return ur[1]
			}
		}
		return 0
	}


	def getSpeciesPageStats(params, request){
		def stubCountQuery = "select t.group.id, count(*) as count from Species s, TaxonomyDefinition t where s.taxonConcept = t group by t.group.id";
		def contentCountQuery = "select t.group.id, count(*) as count from Species s, TaxonomyDefinition t where s.taxonConcept = t and s.percentOfInfo > 0.0 group by t.group.id";

		def allResult = Species.executeQuery(stubCountQuery)
		def contentResult = [data:Species.executeQuery(contentCountQuery)]
		def finalResult = []
		allResult.each{ r ->
			def sgName = (r[0] ? SpeciesGroup.read(r[0]).name : "Others" )
			int contentCount = serachInList(contentResult, "" + r[0])
			finalResult.add([
				sgName,
				contentCount,
				r[1] - contentCount
			])
		}

		def res = [data : finalResult, columns : [
				['string', 'Species Group'],
				['number', 'Content'],
				['number', 'Stubs']
			]]
		addHtmlResultForSpecies(res, , request)
		return res
	}

	private addHtmlResultForSpecies(Map res, request){
		List htmlData = []
		res.data.each{ r ->
			htmlData.add([r[0], getHyperLink(r[0], r[1], false, request, false), getHyperLink(r[0], r[2], false, request, false)])
		}
		
		res.htmlData = htmlData
		res.htmlColumns = [
			['string', 'Species Group'],
			['string', 'Content'],
			['string', 'Stubs']
		]
	}
	
	def activeUserStats(params, request){
		int days = params.days ? params.days.toInteger() : 7
		int max = params.max ? params.max.toInteger() : 10

		UserGroup userGroupInstance
		if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
		}

		def startDate = new Date().minus(days)
		DateGroovyMethods.clearTime(startDate)
		
		def result = Observation.withCriteria(){
			projections {
				groupProperty('author')
				rowCount('total') //alias given to count
			}
			and{
				// taking undeleted observation
				eq('isDeleted', false)
				ge('createdOn', startDate)

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
		
		
		def finalResult = []
		result.each { r ->
			finalResult.add([activityFeedService.getUserHyperLink(r[0], userGroupInstance), getHyperLinkForUser(r[0].id, startDate, r[1], request)])
		}
		
		return [data : result, htmlData:finalResult, columns : [
					['string', 'User'],
					['number', 'Observations']
				],
				htmlColumns : [
					['string', 'User'],
					['string', 'Observations']
				]
			]
	}

	def getPortalActivityStatsByDay(params){
		UserGroup userGroupInstance
		def typeToIdFilterMap

		if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
			typeToIdFilterMap = ActivityFeed.getGroupAndObsevations([userGroupInstance])
		}

		int days = getPassedDays(params, userGroupInstance)

		log.debug "days $days"
		
		def result = []
		Date currentDate = new Date()
		DateGroovyMethods.clearTime(currentDate)
		
		for(int i = -1; i <= days ; i++){
			Date endDate = currentDate.minus(i)
			Date startDate = currentDate.minus(i+1)
			result.add([
				startDate,
				getActivityCount(startDate, endDate, typeToIdFilterMap, userGroupInstance)
			])
		}

		return [data:result, columns : [
				['date', 'Date'],
				[
					'number',
					'Activity Count']
			]]
	}

	private int getActivityCount(startDate, endDate, typeToIdFilterMap, userGroupInstance){
		return ActivityFeed.withCriteria(){
			projections { rowCount('total') //alias given to count
			}
			and{
				between('lastUpdated', startDate, endDate)

				//filter by usergroup
				if(userGroupInstance){
					or{
						typeToIdFilterMap.each{key, value ->
							if(!value.isEmpty()){
								and{
									eq('rootHolderType', key)
									'in'('rootHolderId', value)
								}
							}
						}
					}
				}
			}
		}[0]
	}

	private int getPassedDays(params, UserGroup userGroupInstance){
		Date startDate = PORTAL_START_DATE
		if(userGroupInstance){
			startDate = new Date(userGroupInstance.foundedOn.getTime())
			DateGroovyMethods.clearTime(startDate)
		}

		return params.days ? params.days.toInteger() : ((new Date().getTime() - startDate.getTime())/((1000 * 60 * 60 * 24)))
	}

}
