package species.auth

import grails.converters.JSON
import grails.plugins.springsecurity.Secured;
import grails.plugins.springsecurity.ui.AbstractS2UiController;
import grails.plugins.springsecurity.ui.SpringSecurityUiService
import grails.plugins.springsecurity.ui.UserController;
import grails.util.GrailsNameUtils

import java.util.List
import java.util.Map

import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.dao.DataIntegrityViolationException

import species.BlockedMails;
import species.participation.RecommendationVote;
import species.participation.Observation;

import species.participation.curation.UnCuratedVotes;
import species.utils.Utils;


class SUserController extends UserController {

	def springSecurityService
	def namesIndexerService;
	def observationService;
	def SUserService;
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def isLoggedIn = {
        render springSecurityService.isLoggedIn()
    }

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 12, 100)
		//params.sort = params.sort && params.sort != 'score' ? params.sort : "activity";
		params.query='%';
		def model = getUsersList(params);
		println model;
		// add query params to model for paging
		for (name in [
			'username',
			'enabled',
			'accountExpired',
			'accountLocked',
			'passwordExpired',
			'sort',
			'order'
		]) {
			model[name] = params[name]
		}
		params.remove('query');
		render view: 'list', model: model
		//render view: 'list', model: [results: SUser.list(params), totalCount: SUser.count(), searched:"true"]
	}

	@Secured(['ROLE_ADMIN'])
	def create = {
		def user = lookupUserClass().newInstance(params)
		[user: user, authorityList: sortedRoles()]
	}

	@Secured(['ROLE_ADMIN'])
	def save = {
		def user = lookupUserClass().newInstance(params)
		if (params.password) {
			String salt = saltSource instanceof NullSaltSource ? null : params.username
			user.password = SpringSecurityUiService.encodePassword(params.password, salt)
		}
		if (!user.save(flush: true)) {
			render view: 'create', model: [user: user, authorityList: sortedRoles()]
			return
		}

		addRoles(user)
		flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
		redirect action: edit, id: user.id
	}

	def show = {
		if(!params.id) {
			params.id = springSecurityService.currentUser?.id;
		}

		def SUserInstance = SUser.get(params.long("id"))
		if (!SUserInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'SUser.label', default: 'SUser'), params.id])}"
			redirect(action: "list")
		}
		else {
			return buildUserModel(SUserInstance)
		}
	}

	@Secured(['ROLE_USER', 'ROLE_ADMIN'])
	def edit = {
		log.debug params;
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

		if(SUserService.ifOwns(params.long('id'))) {
			def user = params.username ? lookupUserClass().findWhere((usernameFieldName): params.username) : null
			if (!user) user = findById()
			if (!user) return

			return buildUserModel(user)
		}
		flash.message = "${message(code: 'edit.denied.message')}";
		redirect (action:'show', id:params.id)
	}

	@Secured(['ROLE_USER', 'ROLE_ADMIN'])
	def update = {
		log.debug params;
		String passwordFieldName = SpringSecurityUtils.securityConfig.userLookup.passwordPropertyName

		def user = findById()

		if (!user) return

			if (!versionCheck('user.label', 'User', user, [user: user])) {
				return
			}

		if(SUserService.ifOwns(params.long('id'))) {
			//Cannot change email id with which user was registered
			params.email = user.email;
			
			def oldPassword = user."$passwordFieldName"
			
			def allowIdenMail = (params.allowIdentifactionMail?.equals('on'))?true:false;
			updateBlockMailList(allowIdenMail, user);
			
			user.properties = getTrimmedParams(params)
			if (params.password && !params.password.equals(oldPassword)) {
				String salt = saltSource instanceof NullSaltSource ? null : params.username
				user."$passwordFieldName" = springSecurityUiService.encodePassword(params.password, salt)
			}
			
			user.sendNotification = (params.sendNotification?.equals('on'))?true:false;
			
			
			if (!user.save(flush: true)) {
				render view: 'edit', model: buildUserModel(user)
				return
			}
			
			//if(params.allowIdentifactionMail?.equals('on'));

			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

			//only admin interface has roles information while editing user profile
			if(SUserService.isAdmin(user.id)) {
				lookupUserRoleClass().removeAll user
				addRoles user
			}
			
			userCache.removeUserFromCache user[usernameFieldName]
			flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
			redirect action: show, id: user.id
		} else {
			flash.message = "${message(code: 'update.denied.message')}";
			redirect (action:'show', id:params.id)
		}
	}

	@Secured(['ROLE_ADMIN'])
	def delete = {
		def user = findById()
		if (!user) return

			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		try {
			List obvToUpdate = [];
			lookupUserClass().withTransaction { status ->
				user.observations.each { obv -> 
					UnCuratedVotes.findAllByObv(obv).each { vote ->
						println "deleting $vote"
						vote.delete();
					}
				}
				
				user.recoVotes.each { vote ->
					if(vote.observation.author.id != user.id) {
						obvToUpdate.add(vote.observation);
					}
				}
				
				FacebookUser.removeAll user;
				lookupUserRoleClass().removeAll user
				
				SUserService.sendNotificationMail(SUserService.USER_DELETED, user, request, "");
				user.delete();

			}
			//updating maxVotedSpeciesName
			obvToUpdate.each { obv ->
				println "Updating speciesname for ${obv}"
				obv.calculateMaxVotedSpeciesName();
			}
			userCache.removeUserFromCache user[usernameFieldName]
			flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: search
		}
		catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			flash.error = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: edit, id: params.id
		}
	}

	def search = {
		//[enabled: 0, accountExpired: 0, accountLocked: 0, passwordExpired: 0]
		
		log.debug params
		
		def model = getUsersList(params);
		
		// add query params to model for paging
		for (name in [
			'username',
			'enabled',
			'accountExpired',
			'accountLocked',
			'passwordExpired',
			'sort',
			'order'
		]) {
			model[name] = params[name]
		}
		
		render view: 'search', model: model
	}

	private def getUsersList(params) {
		boolean useOffset = params.containsKey('offset')
		setIfMissing 'max', 12, 100
		setIfMissing 'offset', 0
		
		def hql = new StringBuilder('FROM ').append(lookupUserClassName()).append(' u WHERE 1=1 ')
		def cond = new StringBuilder("");
		def queryParams = [:]

		def userLookup = SpringSecurityUtils.securityConfig.userLookup
		String usernameFieldName = 'name'

		params.sort = params.sort && params.sort != 'score' ? params.sort : "activity";
		String userNameQuery = "";
		if (params['query']) {
			def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields			
			if(params['query'].startsWith(searchFieldsConfig.CANONICAL_NAME)) {
				def usernamesList = searchObservations(params);
				String usernames = getUsernamesSearchCondition(usernamesList);
				userNameQuery = " AND LOWER(u.${usernameFieldName}) IN ${usernames}"
				//queryParams['username'] = usernames
			} else {
				userNameQuery = " AND LOWER(u.${usernameFieldName}) LIKE :username"
				queryParams['username'] = params['query'].toLowerCase() + '%'
			}
		}
		

		String enabledPropertyName = userLookup.enabledPropertyName
		String accountExpiredPropertyName = userLookup.accountExpiredPropertyName
		String accountLockedPropertyName = userLookup.accountLockedPropertyName
		String passwordExpiredPropertyName = userLookup.passwordExpiredPropertyName

		for (name in [enabled: enabledPropertyName,
			accountExpired: accountExpiredPropertyName,
			accountLocked: accountLockedPropertyName,
			passwordExpired: passwordExpiredPropertyName]) {
			Integer value = params.int(name.key)
			if (value) {
				cond.append " AND u.${name.value}=:${name.key}"
				queryParams[name.key] = value == 1
			}
		}

		String q = hql.toString() + cond.toString() + userNameQuery;
		int totalCount = lookupUserClass().executeQuery("SELECT COUNT(DISTINCT u) $q", queryParams)[0]
		//if(totalCount) {
			cond.append userNameQuery
			hql = q;
		/*} else {
			queryParams.remove('username')
			//Searching observations core when no user is found.
			def usernamesList = searchObservations(params);
			String usernames = getUsernamesSearchCondition(usernamesList);
			cond.append " AND LOWER(u.${usernameFieldName}) IN ${usernames}"
			//queryParams['username'] = usernames
			hql.append cond
			totalCount = lookupUserClass().executeQuery("SELECT COUNT(DISTINCT u) $hql", queryParams)[0]
		}*/

		Integer max = params.int('max')
		Integer offset = params.int('offset')

		String orderBy = ''
		if (params.sort == 'lastLoginDate') {
			orderBy = " ORDER BY u.$params.sort ${params.order ?: 'DESC'},  u.$usernameFieldName ASC"
		} else {
			orderBy = " ORDER BY u.$params.sort ${params.order ?: 'ASC'}"
		}

		
		def results = [];
		if(params.sort == 'activity') {
			String query = "select u.id, u.$usernameFieldName from Observation obv right outer join obv.author u WHERE 1=1 $cond and (obv.isDeleted = false or obv.isDeleted is null) group by u.id, u.$usernameFieldName order by count(obv.id)  desc, u.$usernameFieldName asc";
			println query;
			println queryParams;
			def uids =  lookupUserClass().executeQuery(query, queryParams, [max: max, offset: offset])
			uids.each {
				results.add(SUser.read(it[0]));
			}
		} else {
			String query = "SELECT DISTINCT u $hql $orderBy";
			results = lookupUserClass().executeQuery(query, queryParams, [max: max, offset: offset])
		}

		def sorted = results;
//		//sorts only current page results
//		if(results && params['username']) {
//			println params['username'].toLowerCase();
//			sorted = results.sort( sorter.rcurry(params['username'].toLowerCase()))
//		}
		

		return [results: sorted, totalCount: totalCount, searched: true]

	}
	
	// Define a closure that will do the sorting
	def sorter = { SUser a, SUser b, String prefix ->
	  // Get the index into order for a and b
	  // if not found, set to being Integer.MAX_VALUE
	  def (aidx,bidx) = [a,b].collect {
		  it.name.toLowerCase().startsWith(prefix); 
		  }.collect {
	    it ? 1 : Integer.MAX_VALUE
	  }
	  
	  // Compare the two indexes.
	  // If they are the same, compare alphabetically
	  aidx <=> bidx ?: a.name <=> b.name
	}
	
	private def searchObservations(params) {
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
		def newParams = [:];
		newParams["facet.field"] = searchFieldsConfig.CONTRIBUTOR+"_exact";
		newParams["facet.limit"] = params.max;
		newParams["facet.offset"] = params.offset;
		newParams["query"] = params.query;
		def obvSearchModel = observationService.getObservationsFromSearch(newParams);
		def contributorNames = obvSearchModel.tags
		
		return (contributorNames.collect {"'"+it.getKey()+"'"});
	}
	
	private String getUsernamesSearchCondition(List usernamesList) {
		String usernames = usernamesList.toString().replaceFirst('\\[', '(');
		usernames = usernames[0..-2]+")";
		if(!usernamesList) {
			usernames = "('')"
		}
		
		return usernames;
	}
	/**
	 *
	 */
	def advSearch = {
		log.debug params;
		String query  = "";
		def newParams = [:]
		for(field in params) {
			if(!(field.key ==~ /action|controller|sort|fl|start|rows/) && field.value ) {
				if(field.key.equalsIgnoreCase('name')) {
					newParams[field.key] = field.value;
					query = query + " " +field.value;
				} else {
					newParams[field.key] = field.value;
					query = query + " " + field.key + ': "'+field.value+'"';
				}
			}
		}
		if(query) {
			newParams['query'] = query;
			redirect (action:"search", params:newParams);
		}
		render (view:'advSearch', params:newParams);
	}
	
	/**
	 * Ajax call used by autocomplete textfield.
	 */
	def nameTerms = {
		log.debug params

		setIfMissing 'max', 5, 10
		
		def jsonData = []

		def namesLookupResults = namesIndexerService.suggest(params)
		jsonData.addAll(namesLookupResults);
 
		jsonData.addAll(getUserSuggestions(params));
		render jsonData as JSON
	}
	
	/**
	 *
	 */
	def terms = {
		log.debug params;
		setIfMissing 'max', 5, 10
		render getUserSuggestions(params) as JSON;
	}

	private getUserSuggestions(params){
		def jsonData = []
		String username = params.term
		
		String usernameFieldName = 'name';//SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		String userId = 'id';


		def results = lookupUserClass().executeQuery(
				"SELECT DISTINCT u.$usernameFieldName, u.$userId " +
				"FROM ${lookupUserClassName()} u " +
				"WHERE LOWER(u.$usernameFieldName) LIKE :name " +
				"ORDER BY u.$usernameFieldName",
				[name: "${username.toLowerCase()}%"],
				[max: params.max])

		for (result in results) {
			jsonData << [value: result[0], label:result[0] , userId:result[1] , "category":"Users"]
		}
		
		return jsonData;
	} 
	
	private Map getUnBlockedMailList(String userIdsAndEmailIds, request){
		Map result = new HashMap();
		userIdsAndEmailIds.split(",").each{
			String candidateEmail = it.trim();
			//checking for email signature
			if(candidateEmail.contains("@")){
				if(BlockedMails.findByEmail(candidateEmail)){
					log.debug "Email $candidateEmail is unsubscribed for identification mail."
				}else{
					result[candidateEmail] = generateLink("observation", "unsubscribeToIdentificationMail", [email:candidateEmail], request) ;
				}
			}else{
				//its user id
				SUser user = SUser.get(candidateEmail.toLong());
				candidateEmail = user.email.trim();
				if(user.allowIdentifactionMail){
					result[candidateEmail] = generateLink("observation", "unsubscribeToIdentificationMail", [email:candidateEmail, userId:user.id], request) ;
				}else{
					log.debug "User $user.id has unsubscribed for identification mail."
				}
			}
		}
		return result;
	}
	
    def login = {
		render template:"/common/suser/userLoginBoxTemplate" 
    }
	
	def header = {
		render template:"/domain/ibpHeaderTemplate"
	}
	def sidebar = {
		render template:"/common/userGroup/sidebarTemplate"
	}

	protected void addRoles(user) {
		String upperAuthorityFieldName = GrailsNameUtils.getClassName(
				SpringSecurityUtils.securityConfig.authority.nameField, null)

		for (String key in params.keySet()) {
			if (key.contains('ROLE') && 'on' == params.get(key)) {
				lookupUserRoleClass().create user, lookupRoleClass()."findBy$upperAuthorityFieldName"(key), true
			}
		}
	}

	protected Map buildUserModel(user) {

		String authorityFieldName = SpringSecurityUtils.securityConfig.authority.nameField
		String authoritiesPropertyName = SpringSecurityUtils.securityConfig.userLookup.authoritiesPropertyName

		List roles = sortedRoles()
		Set userRoleNames = user[authoritiesPropertyName].collect { it[authorityFieldName] }
		def granted = [:]
		def notGranted = [:]
		for (role in roles) {
			String authority = role[authorityFieldName]
			if (userRoleNames.contains(authority)) {
				granted[(role)] = userRoleNames.contains(authority)
			}
			else {
				notGranted[(role)] = userRoleNames.contains(authority)
			}
		}

		return [user: user, roleMap: granted + notGranted]
	}

	protected findById() {
		def user = lookupUserClass().get(params.long('id'))
		if (!user) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: search
		}

		user
	}

	protected List sortedRoles() {
		lookupRoleClass().list().sort { it.authority }
	}
	
	
	private Map getTrimmedParams(Map m){
		def res = [:]
		m.each {key, value -> res[key] = value.toString().trim()}
		return res
	}
	
	private updateBlockMailList(allowIdenMail, user){
		if(user.allowIdentifactionMail != allowIdenMail){
			if(allowIdenMail){
				BlockedMails bm = BlockedMails.findByEmail(user.email.trim());
				if(bm && !bm.delete(flush:true)){
					this.errors?.allErrors.each { log.error it }
				}
			}else{
				BlockedMails bm = new BlockedMails(email:user.email.trim());
				if(!bm.save(flush:true)){
					bm.errors.allErrors.each { log.error it }
				}
			}
		}
	}
	
	/**
	*
	*/
   def getRecommendationVotes = {
	   log.debug params;
	   params.max = params.max ? params.int('max') : 1
	   params.offset = params.offset ? params.long('offset'): 0
	   
	   def userInstance = SUser.get(params.id)
	   if (userInstance) {
		   def recommendationVoteList 
		   if(params.obvId){
			   	recommendationVoteList = RecommendationVote.findAllByAuthorAndObservation(springSecurityService.currentUser, Observation.read(params.long('obvId')));
		   }else{
		   		recommendationVoteList = observationService.getRecommendationsOfUser(userInstance, params.max, params.offset);
		   }
		   processResult(recommendationVoteList, params)
		   return
	   }
	   else {
		   response.setStatus(500)
		   def message = ['error':g.message(code: 'error', default:'Error while processing the request.')]
		   render message as JSON
	   }
   }

	private processResult(List recommendationVoteList, Map params) {
		try {
			if(recommendationVoteList.size() > 0) {
				def result = [];
				recommendationVoteList.each { recoVote ->
					def map = recoVote.recommendation.getRecommendationDetails(recoVote.observation);
					map.put("noOfVotes", 1);
					def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
					def image = recoVote.observation.mainImage()
					def imagePath = image.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, config.speciesPortal.resources.images.thumbnail.suffix)
					def imageLink = config.speciesPortal.observations.serverURL +  imagePath
					map.put('observationImage', imageLink);
					map.put("obvId", recoVote.observation.id);
					result.add(map);
				}
				//def noOfVotes = observationService.getAllRecommendationsOfUser(userInstance);
				def model = ['result':result, 'totalVotes':result.size(), 'uniqueVotes':result.size()];
				def html = g.render(template:"/common/observation/showObservationRecosTemplate", model:model);
				def r = [
							success : 'true',
							uniqueVotes:model.uniqueVotes,
							recoHtml:html,
							recoVoteMsg:params.recoVoteMsg]
				render r as JSON
				return
			} else {
				response.setStatus(500);
				def message = "";
				if(params.offset > 0) {
					message = [info: g.message(code: 'user.recommendations.nomore.message', default:'No more recommendations made.')];
				} else {
					message = [info:g.message(code: 'user.recommendations.zero.message', default:'No recommendations made.')];
				}
				render message as JSON
				return
			}
		} catch(e){
			e.printStackTrace();
			response.setStatus(500);
			def message = ['error' : g.message(code: 'error', default:'Error while processing the request.')];
			render message as JSON
		}
	}

}

