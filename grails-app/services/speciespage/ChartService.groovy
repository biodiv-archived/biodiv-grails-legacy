package speciespage

import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.web.util.WebUtils;
import org.codehaus.groovy.runtime.DateGroovyMethods;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.Species;
import species.auth.SUser
import species.groups.SpeciesGroup;
import species.groups.UserGroup;
import species.utils.ImageType;
import species.participation.ActivityFeed;
import species.participation.ActivityFeedService;
import species.participation.Observation;
import species.participation.Checklists;
import content.eml.Document;
import species.participation.RecommendationVote;
import groovy.sql.Sql;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import org.springframework.web.context.request.RequestContextHolder;

class ChartService {

	static transactional = false

	private static final Date PORTAL_START_DATE = new Date(111, 7, 8)

	static final String ACTIVE_USER_STATS = "Active user stats"
	static final String OBSERVATION_STATS = "Observation stats"
	static final String SPECIES_STATS = "Species stats"
	static final String USER_OBSERVATION_BY_SPECIESGROUP = "User observation by species group"
	
def messageSource;
    def utilsService
	def userGroupService
    def dataSource
	def request;
	def getObservationStats(params, SUser author, request){
		if(request == null) request = RequestContextHolder.currentRequestAttributes().request
		UserGroup userGroupInstance
		if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
		}

		//getting all observation
		def allResult = getFilteredObservationStats(userGroupInstance, author, null)

		//getting all observation
		def unidentifiedResult = getFilteredObservationStats(userGroupInstance, author, false)

		mergeResult(allResult, unidentifiedResult)
    	allResult.columns = [
			['string', messageSource.getMessage("table.species.group", null, RCU.getLocale(request))],
			['number', messageSource.getMessage("default.all.label", null, RCU.getLocale(request))],
			['number', messageSource.getMessage("button.unidentified", null, RCU.getLocale(request))]
		]

		addHtmlResultForObv(allResult, request)
		return allResult
	}

    def getUserStats(SUser user, UserGroup userGroupInstance = null) {
    	//getting all observation
		def allObvResult = getFilteredObservationStats(userGroupInstance, user, null)

        //getting ireco
		def allRecoResult = getFilteredRecommendationStats(user , userGroupInstance)
		
        mergeResult(allObvResult, allRecoResult)
    	allObvResult.columns = [
			['string', 'Species Group'],
			['number', ''],
			['number', 'Identifications']
		]

		addHtmlResultForObv(allObvResult, null, user)
		return allObvResult
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
				eq('isShowable', true)
				eq('isChecklist', false)

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

    /**
	 * 
	 * @param userGroupInstance
	 * @param author
	 * @param identifactionFlag if null then returning all obs, if true then returning only identified if false then returning unidentified
	 * @return
	 */
	private getFilteredRecommendationStats(SUser author , UserGroup userGroupInstance = null){
        def sql =  Sql.newInstance(dataSource);
        def result
        if(userGroupInstance){
            result = sql.rows("select g.id, count(*) from recommendation_vote r, observation o, user_group_observations ugo, species_group g where r.observation_id = o.id and ugo.observation_id = o.id and ugo.user_group_id =:ugID and o.group_id = g.id and r.author_id=:authorId and o.is_deleted = false and o.is_showable = true and o.is_checklist = false group by g.id", [authorId:author.id, ugID : userGroupInstance.id]);
        } else {
            result = sql.rows("select g.id, count(*) from recommendation_vote r, observation o, species_group g where r.observation_id = o.id and o.group_id = g.id and r.author_id=:authorId and o.is_deleted = false and o.is_showable = true and o.is_checklist = false group by g.id", [authorId:author.id]);
        }
        //def result = RecommendationVote.executeQuery("select g.id, count(*) from RecommendationVote r, Observation o, SpeciesGroup g where r.observation = o and o.group = g and r.author=:author and o.isDeleted = false and o.isShowable = true and o.isChecklist = false group by g.id", [author:author]);
        def resultFinal = []
        for (row in result){
            resultFinal.add([SpeciesGroup.findById(row.getProperty("id")),row.getProperty("count")])
        }

        /*
        result.each {it->
            it[0] = SpeciesGroup.read(it[0]);
        }*/
		return getFormattedResult(resultFinal)
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
		createUnion(allResult, unidentifiedResult)
		allResult.data.each{ r ->
			String speciesGroup = r[0]
			int unIdentified = serachInList(unidentifiedResult, speciesGroup)

			//adding identified one
			//r.add(r[1] - unIdentified)
			//adding unidentified
			r.add(unIdentified)
		}
	}

	private createUnion(allResult, unidentifiedResult){
		Set allResultKeys = new HashSet(allResult.data.collect{it[0]})
		Set unidentifiedResultKeys = new HashSet(unidentifiedResult.data.collect{it[0]})
		unidentifiedResultKeys.removeAll(allResultKeys)
		unidentifiedResultKeys.each { allResult.data.add([it, 0]) }
	}
	
	private addHtmlResultForObv(Map res, request, SUser user=null){
		List htmlData = []
        def filterParams = [:]
        if(user) {
            filterParams['user'] = user.id;
        }

        boolean isUnidentified = (res.columns[2][1] == "UnIdentified")?:false
		res.data.each{ r ->
			htmlData.add([getSpeciesGroupImage(r[0]), getHyperLink(r[0], r[0], false, true, filterParams), getHyperLink(r[0], r[1], false, true, filterParams), getHyperLink(r[0], r[2], isUnidentified, true, filterParams)])
		}
		
		res.htmlData = htmlData
		res.htmlColumns = [
			['string', '']
		]
        res.columns.each { col -> 
            res.htmlColumns.add(['string', col[1]]);
        }
        //println res;
	}
	
	private getHyperLink(String speciesGroup, count, boolean isUnIdentified, boolean isObv, Map filterParams=[:]){
		def filterParamsForHyperLink = new HashMap(filterParams)
		if(isUnIdentified){
			if(isObv){
				filterParamsForHyperLink.speciesName="Unknown"
			}
		}
		filterParamsForHyperLink.sGroup = SpeciesGroup.findByName(speciesGroup).id
		def link = utilsService.generateLink((isObv)?"observation":"species", "list", filterParamsForHyperLink, null)
		return "" + '<a href="' +  link +'">' + count + "</a>"
	}
	
	
	private getHyperLinkForUser(userId, Date startDate, count, request, speciesGroup=null){
		def fitlerParams = [:]
		if(startDate){
			fitlerParams.daterangepicker_start = new SimpleDateFormat("dd/MM/yyyy").format(startDate)
			fitlerParams.daterangepicker_end = new SimpleDateFormat("dd/MM/yyyy").format(new Date())
		}
		if(speciesGroup){
			fitlerParams.sGroup = speciesGroup.id
		}
		fitlerParams.user = userId
		def link = utilsService.generateLink("observation", "list", fitlerParams, request)
		return "" + '<a  href="' +  link +'">' + count + "</a>"
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
		if(request == null) request = RequestContextHolder.currentRequestAttributes().request
		def totalCountQuery = "select t.group.id, count(*) as count from Species s, TaxonomyDefinition t where s.taxonConcept = t group by t.group.id order by count(*) desc";
		def contentCountQuery = "select t.group.id, count(*) as count from Species s, TaxonomyDefinition t where s.taxonConcept = t and s.percentOfInfo > 0.0 group by t.group.id order by count(*) desc";

		def allResult = [data:Species.executeQuery(totalCountQuery)]
		def contentResult = Species.executeQuery(contentCountQuery)
		def finalResult = []
		contentResult.each{ r ->
			def sgName = (r[0] ? SpeciesGroup.read(r[0]).name : "Others" )
			int all = serachInList(allResult, "" + r[0])
			finalResult.add([
				sgName,
				r[1],
				all - r[1]
			])
		}

		def res = [data : finalResult, columns : [
				['string', messageSource.getMessage("table.species.group", null, RCU.getLocale(request))],
				['number', messageSource.getMessage("default.content.label", null, RCU.getLocale(request))],
				['number', messageSource.getMessage("table.stubs", null, RCU.getLocale(request))]
			]]
		addHtmlResultForSpecies(res, , request)
		return res
	}

	private addHtmlResultForSpecies(Map res, request){
		if(request == null) request = RequestContextHolder.currentRequestAttributes().request
		List htmlData = []
		res.data.each{ r ->
			htmlData.add([getSpeciesGroupImage(r[0]), getHyperLink(r[0], r[0], false, false), r[1], r[2]])
		}
		
		res.htmlData = htmlData
		res.htmlColumns = [
			['string', ''],
			['string', messageSource.getMessage("table.species.group", null, RCU.getLocale(request))],
			['number', messageSource.getMessage("default.content.label", null, RCU.getLocale(request))],
			['number', messageSource.getMessage("table.stubs", null, RCU.getLocale(request))]
		]
	}
	
	private getSpeciesGroupImage(sName){
		def speciesGroup = SpeciesGroup.findByName(sName)
		def cssClass = '"' + "btn species_groups_sprites " + speciesGroup.iconClass() + " active" + '"'
		def title = '"' +  speciesGroup.name + '"'
		return "<button class=$cssClass title=$title></button>"  
	} 
	
	def activeUserStats(params, request){
		if(request == null) request = RequestContextHolder.currentRequestAttributes().request
		int days = params.days ? params.days.toInteger() : 7
		int max = params.max ? params.max.toInteger() : 10

		UserGroup userGroupInstance
		if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
		}

		def startDate = new Date().minus(days)
		DateGroovyMethods.clearTime(startDate)
		
		def result = activeUserStatsAuthorAndCount(max, userGroupInstance, startDate);		

		def obvCount = Observation.createCriteria().count {
			and{
				// taking undeleted observation
				eq('isDeleted', false)
				eq('isShowable', true)
				eq('isChecklist', false)
				
				ge('createdOn', startDate)

				//filter by usergroup
				if(userGroupInstance){
					userGroups{
						eq('id', userGroupInstance.id)
					}
				}
			}
		}
		
		def finalResult = []
		result.each { r ->
			def link = utilsService.generateLink("SUser", "show", ["id": r[0].id], request)
			link =  "" + '<a  href="' +  link +'"><i>' + r[0].name + "</i></a>"
			finalResult.add([ getUserImage(r[0]), link, getHyperLinkForUser(r[0].id, startDate, r[1], request)])
			r[0] = r[0].name
		}
		
		return [obvCount:obvCount, data : result, htmlData:finalResult, columns : [
					['string', messageSource.getMessage("value.user", null, RCU.getLocale(request))],
					['number', messageSource.getMessage("default.observation.label", null, RCU.getLocale(request))]
				],
				htmlColumns : [
					['string', ''],
					['string', messageSource.getMessage("value.user", null, RCU.getLocale(request))],
					['string', messageSource.getMessage("default.observation.label", null, RCU.getLocale(request))]
				]
			]
	}

	private getUserImage(SUser actor){
		return '<img style="max-height: 32px; min-height: 16px; max-width: 32px; width: auto;" src="' + actor.profilePicture(ImageType.SMALL) +'" title="' + actor.name + '" />'
	}
	
	def getPortalActivityStatsByDay(params){
		UserGroup userGroupInstance
		def typeToIdFilterMap

		if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
			typeToIdFilterMap = ActivityFeed.getGroupAndObsevations([userGroupInstance])
		}

		int days = getPassedDays(params, userGroupInstance) 
		log.debug "passed days $days"
		days = (days > 365) ? 365 : days
		
		def result = []
		Date currentDate = new Date()
		DateGroovyMethods.clearTime(currentDate)
		
		
		
		for(int i = -1; i <= days ; i++){
			Date endDate = currentDate.minus(i)
			Date startDate = currentDate.minus(i+1)
			
			def userCount
			if(userGroupInstance){
				userCount = getActivityCount(startDate, endDate, typeToIdFilterMap, userGroupInstance, ActivityFeedService.MEMBER_JOINED)
			}else{
				userCount = getRegisterUserCount(startDate, endDate)
			}
			
			result.add([
				startDate,
				getActivityCount(startDate, endDate, typeToIdFilterMap, userGroupInstance),
				getActivityCount(startDate, endDate, typeToIdFilterMap, userGroupInstance, ActivityFeedService.OBSERVATION_CREATED),
				userCount
			])
		}

		return [data:result, columns : [
				['date', 'Date'],
				['number','Activity'],
				['number','Observation'],
				['number', 'Users']
				]
			]
	}

	private int getActivityCount(startDate, endDate, typeToIdFilterMap, userGroupInstance, feedType=null){
		return ActivityFeed.createCriteria().count{
			and{
				eq('isShowable', true)
				if(startDate && endDate){
					between('lastUpdated', startDate, endDate)
				}
				if(feedType){
					eq('activityType', feedType)
				}
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
		}
	}

	private int getRegisterUserCount(startDate, endDate){
		return ActivityFeed.createCriteria().count {
				and{
					eq('activityType', ActivityFeedService.USER_REGISTERED)
					between('dateCreated', startDate, endDate)
				}
			}
	}
	
	private int getPassedDays(params, UserGroup userGroupInstance){
		if(params.days)
			return params.days.toInteger()
			
		Date startDate = PORTAL_START_DATE
		if(userGroupInstance){
			startDate = new Date(userGroupInstance.foundedOn.getTime())
			DateGroovyMethods.clearTime(startDate)
		}

		return ((new Date().getTime() - startDate.getTime())/((1000 * 60 * 60 * 24)))
	}
	
	def combineStats(params, request){
		List speciesData = getSpeciesPageStats(params, request).htmlData
		List obvData = getObservationStats(params, null, request).htmlData
		
		List finalRes = []
		obvData.each{ List obv ->
			List res = []
			res.add(obv[0])
			res.add(obv[1])
			res.add(obv[2])
			
			speciesData.each{ sg -> 
				if(sg[1] == obv[1]){
					res.add(sg[2])
				}
			}
			finalRes.add(res)
		}
		return [htmlData : finalRes,
				htmlColumns : [
				['string', ''],
				['string', 'Species Group'],
				['string', 'Observations'],
				['string', 'Species pages']
				]
			]
	}
	
	def activeUserStatsBySpeciesGroup(speciesGroupId, params=null){
		UserGroup userGroupInstance
        if(params.webaddress) {
			userGroupInstance = userGroupService.get(params.webaddress);
		}
		int max = 5
		def sGroup =  SpeciesGroup.get(speciesGroupId)
		def request = WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest()
		def result = Observation.withCriteria(){
			projections {
				groupProperty('author')
				rowCount('total') //alias given to count
			}
			and{
				// taking undeleted observation
				eq('isDeleted', false)
				eq('isShowable', true)
				eq('isChecklist', false)
				
				eq('group', sGroup)
				
//				//filter by usergroup
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
			def link = utilsService.generateLink("SUser", "show", ["id": r[0].id], request)
			link =  "" + '<a  href="' +  link +'"><i>' + r[0].name + "</i></a>"
			finalResult.add([ getUserImage(r[0]), link, getHyperLinkForUser(r[0].id, null, r[1], request, sGroup)])
			r[0] = r[0].name
		}
		
		return [data : result, htmlData:finalResult, columns : [
					['string', 'User'],
					['number', 'Observations']
				],
				htmlColumns : [
					['string', ''],
					['string', 'User'],
					['string', 'Observations']
				]
			]
	}

	
	def populateData(model){
		if(model.data || model.htmlData){
			return
		}
		def params = model.params
        switch (model.statsType) {
			case USER_OBSERVATION_BY_SPECIESGROUP:
				model.putAll(activeUserStatsBySpeciesGroup(model.speciesGroupId, params))
				break
			case ACTIVE_USER_STATS:
				model.putAll(activeUserStats(params, null))
				break
			case OBSERVATION_STATS:
				model.putAll(getObservationStats(params, null, null))
				break
			case SPECIES_STATS:
				model.putAll(getSpeciesPageStats(params, null))
				break
			default:
				break
		}
	}
	
	def List getUserByRank(max, offset, userName = null){
		return ActivityFeed.withCriteria(){
			projections {
				groupProperty('author')
				rowCount('total') //alias given to count
			}
			and{
				eq('isShowable', true)
				if(userName){
					author{
						or{
							ilike("username", userName)
							ilike("name", userName)
						}
					}
				}
			}
			maxResults max
			firstResult offset
			order 'total', 'desc'
		}.collect { it[0]}
	}


	def long getUserActivityCount(user){
		return ActivityFeed.countByAuthorAndIsShowable(user,true);
	}

	def long getObservationCount(params){
		def userGroup, count 
		if(params.webaddress) {
			userGroup = userGroupService.get(params.webaddress)
		}
		
		if(userGroup){
			count = userGroupService.getCountByGroup(Observation.simpleName, userGroup);
		}else{
			count = Observation.createCriteria().count {
				and {
					eq("isDeleted", false)
					eq("isShowable", true)
					eq("isChecklist", false)
				}
			}
		}
		return count
	}

	def long getChecklistCount(params){
		def userGroup, count 
		if(params.webaddress) {
			userGroup = userGroupService.get(params.webaddress)
		}
		
		if(userGroup){
			count = userGroupService.getCountByGroup(Checklists.simpleName, userGroup);
		}else{
			count = Checklists.countByIsDeleted(false);
		}
		return count
	}

	def long getSpeciesCount(params){
		def userGroup, count 
		if(params.webaddress) {
			userGroup = userGroupService.get(params.webaddress)
		}
		
		if(userGroup){
			count = userGroupService.getCountByGroup(Species.simpleName, userGroup);
		}else{
			count = Species.count();
		}
		return count
	}

	def long getDocumentCount(params){
		def userGroup, count 
		if(params.webaddress) {
			userGroup = userGroupService.get(params.webaddress)
		}
		
		if(userGroup){
			count = userGroupService.getCountByGroup(Document.simpleName, userGroup);
		}else{
			count = Document.count();
		}
		return count
	}

	def long getUserCount(params){
		def userGroup, count 
		if(params.webaddress) {
			userGroup = userGroupService.get(params.webaddress)
		}
		
		if(userGroup){
			count = userGroup.getAllMembersCount();
		}else{
			count = SUser.count();
		}
		return count
	}

	
	def long getActivityFeedCount(params){
		def userGroup, typeToIdFilterMap
		if(params.webaddress) {
			userGroup = userGroupService.get(params.webaddress)
			typeToIdFilterMap = ActivityFeed.getGroupAndObsevations([userGroup])
			
		}
		
		return getActivityCount(null, null, typeToIdFilterMap, userGroup)
	}

    def activeUserStatsAuthorAndCount(max, userGroupInstance , startDate){ 

        def result = Observation.withCriteria(){
            projections {
                groupProperty('author')
                rowCount('total') //alias given to count
            }
            and{
                // taking undeleted observation
                eq('isDeleted', false)
                eq('isShowable', true)
                eq('isChecklist', false)

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
        return result
    }
}
