package species.auth

import groovy.sql.Sql;
import grails.converters.JSON
import grails.plugins.springsecurity.Secured;
import grails.plugins.springsecurity.ui.AbstractS2UiController;
import grails.plugins.springsecurity.ui.RegisterController;
import grails.plugins.springsecurity.ui.SpringSecurityUiService
import grails.plugins.springsecurity.ui.UserController;
import grails.util.GrailsNameUtils

import java.util.List
import java.util.Map

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartHttpServletRequest;

import species.BlockedMails;
import species.participation.RecommendationVote;
import species.participation.Observation;

import species.participation.curation.UnCuratedVotes;
import species.utils.Utils;
import species.utils.ImageUtils;
import species.Habitat;
import species.groups.SpeciesGroup;

class SUserController extends UserController {

	def springSecurityService
	def namesIndexerService;
	def observationService;
	def SUserService;
	def userGroupService;
	def saltSource;
    def dataSource;
    def chartService;

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def isLoggedIn = { render springSecurityService.isLoggedIn() }

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 12, 100)
		//params.sort = params.sort && params.sort != 'score' ? params.sort : "activity";
		params.query='%';
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
		params.remove('query');

		if(params.loadMore?.toBoolean()){
			render(template:"/common/suser/showUserListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			render (view:"list", model:model)
			return;
		} else{
			model['resultType'] = 'user'
			def obvListHtml =  g.render(template:"/common/suser/showUserListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			//			def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
			//			def tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
			//			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model.totalObservationInstanceList]);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]
			render result as JSON
			return;
		}

		//		render view: 'list', model: model
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
			def result = buildUserModel(SUserInstance)
			result.put('userGroupWebaddress', params.webaddress)
            result.put('obvData', chartService.getUserStats(SUserInstance));
//            def totalObservationInstanceList = observationService.getFilteredObservations(['user':SUserInstance.id.toString()], -1, -1, true).observationInstanceList
//            result.put('totalObservationInstanceList', totalObservationInstanceList); 
			return result
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
		redirect (url:uGroup.createLink(action:'show', controller:"SUser", id:params.id, 'userGroupWebaddress':params.webaddress))
		//redirect (action:'show', id:params.id)
	}

	@Secured(['ROLE_USER', 'ROLE_ADMIN'])
	def update = {
		log.debug params;
		String passwordFieldName = SpringSecurityUtils.securityConfig.userLookup.passwordPropertyName



		if(SUserService.ifOwns(params.long('id'))) {
			def user = findById()

			if (!user) return

				if (!versionCheck('user.label', 'User', user, [user: user])) {
					return
				}

			//Cannot change email id with which user was registered
			params.email = user.email;

			def oldPassword = user."$passwordFieldName"

			def allowIdenMail = (params.allowIdentifactionMail?.equals('on'))?true:false;
			updateBlockMailList(allowIdenMail, user);

			user.properties = getTrimmedParams(params)
//			if (params.password && !params.password.equals(oldPassword)) {
//				String salt = saltSource instanceof NullSaltSource ? null : params.username
//				user."$passwordFieldName" = springSecurityUiService.encodePassword(params.password, salt)
//			}

			user.sendNotification = (params.sendNotification?.equals('on'))?true:false;

			user.icon = getUserIcon(params.icon);
			addInterestedSpeciesGroups(user, params.speciesGroup)
			addInterestedHabitats(user, params.habitat)

			user.website = (params.website.trim() != "") ? params.website.trim().split(",").join(", ") : null

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
			redirect (url:uGroup.createLink(action:'show', controller:"SUser", id:user.id, 'userGroupWebaddress':params.webaddress))
			//redirect action: show, id: user.id
		} else {
			flash.message = "${message(code: 'update.denied.message')}";
			redirect (url:uGroup.createLink(action:'show', controller:"SUser", id:params.id, 'userGroupWebaddress':params.webaddress))
			//redirect (action:'show', id:params.id)
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
						log.debug "deleting $vote"
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
			//updating SpeciesName
			obvToUpdate.each { obv ->
				log.debug "Updating speciesname for ${obv}"
				obv.calculateMaxVotedSpeciesName();
			}
			userCache.removeUserFromCache user[usernameFieldName]
			flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), user.name])}"
			redirect action: search
		}
		catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			flash.error = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), user.name])}"
			redirect (url:uGroup.createLink(action:'edit', controller:"SUser", id:params.id, 'userGroupWebaddress':params.webaddress))
			//redirect action: edit, id: params.id
		}
	}

	def search = {
		println "search"
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
		model['isSearch'] = true;
		params.action = 'search'
		params.controller = 'SUser'

		if(params.loadMore?.toBoolean()){
			params.remove('isGalleryUpdate');
			render(template:"/common/suser/showUserListTemplate", model:model);
			return;
		} else if(!params.isGalleryUpdate?.toBoolean()){
			params.remove('isGalleryUpdate');
			render (view:"search", model:model)
			return;
		} else {
			params.remove('isGalleryUpdate');
			model['resultType'] = 'user'
			def obvListHtml =  g.render(template:"/common/suser/showUserListTemplate", model:model);
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			//			def filteredTags = observationService.getTagsFromObservation(model.totalObservationInstanceList.collect{it[0]})
			//			def tagsHtml = g.render(template:"/common/observation/showAllTagsTemplate", model:[count: count, tags:filteredTags, isAjaxLoad:true]);
			//			def mapViewHtml = g.render(template:"/common/observation/showObservationMultipleLocationTemplate", model:[observationInstanceList:model.totalObservationInstanceList]);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]
			render result as JSON
			return;
		}
	}

	private def getUsersList(params) {
		boolean useOffset = params.containsKey('offset')
		setIfMissing 'max', 12, 100
		setIfMissing 'offset', 0
		int totalCount = 0;
		def results = [];
		def queryParams = [:]

		if(params.action != 'search' || (params.action == 'search' && params.query)) {
			def hql = new StringBuilder('FROM ').append(lookupUserClassName()).append(' u WHERE 1=1 ')
			def cond = new StringBuilder("");


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
			log.debug "CountQuery : ${q} with params ${queryParams}"
			totalCount = lookupUserClass().executeQuery("SELECT COUNT(DISTINCT u) $q", queryParams)[0]
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



			if(params.sort == 'activity') {
				String query = "select u.id, u.$usernameFieldName from Observation obv right outer join obv.author u WHERE 1=1 $cond and (obv.isDeleted = false or obv.isDeleted is null) group by u.id, u.$usernameFieldName order by count(obv.id)  desc, u.$usernameFieldName asc";
				log.debug "UserQuery : ${query} with params ${queryParams}"
				def uids =  lookupUserClass().executeQuery(query, queryParams, [max: max, offset: offset])
				uids.each {
					results.add(SUser.read(it[0]));
				}
			} else {
				String query = "SELECT DISTINCT u $hql $orderBy";
				log.debug "UserQuery : ${query} with params ${queryParams}"
				results = lookupUserClass().executeQuery(query, queryParams, [max: max, offset: offset])
			}


			//		//sorts only current page results
			//		if(results && params['username']) {
			//			println params['username'].toLowerCase();
			//			sorted = results.sort( sorter.rcurry(params['username'].toLowerCase()))
			//		}
		}

		return ['userInstanceList': results, instanceTotal: totalCount, queryParams:queryParams, searched: true]

	}

	// Define a closure that will do the sorting
	def sorter = { SUser a, SUser b, String prefix ->
		// Get the index into order for a and b
		// if not found, set to being Integer.MAX_VALUE
		def (aidx,bidx) = [a, b].collect {
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

		jsonData.addAll(SUserService.getUserSuggestions(params));
		render jsonData as JSON
	}

	/**
	 *
	 */
	def terms = {
		log.debug params;
		setIfMissing 'max', 5, 10
		render SUserService.getUserSuggestions(params) as JSON;
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

	def login = { render template:"/common/suser/userLoginBoxTemplate"  }

	def header = {
		//TODO:HACK FOR NOW
		String domainUrl = Utils.getDomainServerUrl(request);

		if(params.webaddress) {
			def userGroupInstance = userGroupService.get(params['webaddress'])
			if(userGroupInstance) {
				render (template:"/domain/ibpHeaderTemplate", model:['userGroupInstance':userGroupInstance])
				return
			}
		}
		render template:"/domain/ibpHeaderTemplate"
	}

	def sidebar = { render template:"/common/userGroup/sidebarTemplate" }

	def footer = { render template:"/domain/ibpFooterTemplate" }

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
		params.max = params.limit ? params.int('limit') : 10
		params.offset = params.offset ? params.long('offset'): 0

		def userInstance = SUser.get(params.filterPropertyValue)
		if (userInstance) {
			def recommendationVoteList
			if(params.obvId){
				recommendationVoteList = RecommendationVote.findAllByAuthorAndObservation(springSecurityService.currentUser, Observation.read(params.long('obvId')));
			}else{
				recommendationVoteList = observationService.getRecommendationsOfUser(userInstance, params.max, params.offset);
			} 
            
            def uniqueVotes = observationService.getAllRecommendationsOfUser(userInstance);

            def observations = recommendationVoteList.collect{it.observation};	
            def result = []
            observations.each {
                result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
            }
            def relatedObv = ['observations':result, count:uniqueVotes];
            if(relatedObv.observations) {
                    relatedObv.observations = observationService.createUrlList2(relatedObv.observations);
            } 
            
            render relatedObv as JSON
		}
		else {
			//response.setStatus(500)
			def message = ['status':'error', 'msg':g.message(code: 'error', default:'Error while processing the request.')]
			render message as JSON
		}
        return
	}

    def getRecommendationCount(userInstance){
		Sql sql =  Sql.newInstance(dataSource);
		def result = sql.rows("select count(distinct(recoVote.recommendation_id)) from recommendation_vote as recoVote where recoVote.author_id = :userId", [userId:userInstance?.id])
		return result[0]["count"]
	}

	@Secured(['ROLE_USER'])
	def upload_resource = {
		log.debug params;

		try {
			if(ServletFileUpload.isMultipartContent(request)) {
				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				def rs = [:]
				Utils.populateHttpServletRequestParams(request, rs);
				def resourcesInfo = [];
				def rootDir = grailsApplication.config.speciesPortal.users.rootDir
				File usersDir;
				def message;

				if(!params.resources) {
					message = g.message(code: 'no.file.attached', default:'No file is attached')
				}

				params.resources.each { f ->
					log.debug "Saving userGroup logo file ${f.originalFilename}"

					// List of OK mime-types
					//TODO Move to config
					def okcontents = [
						'image/png',
						'image/jpeg',
						'image/pjpeg',
						'image/gif',
						'image/jpg'
					]

					if (! okcontents.contains(f.contentType)) {
						message = g.message(code: 'resource.file.invalid.extension.message', args: [
							okcontents,
							f.originalFilename
						])
					}
					else if(f.size > grailsApplication.config.speciesPortal.users.logo.MAX_IMAGE_SIZE) {
						message = g.message(code: 'resource.file.invalid.max.message', args: [
							grailsApplication.config.speciesPortal.users.logo.MAX_IMAGE_SIZE/1024,
							f.originalFilename,
							f.size/1024
						], default:"File size cannot exceed ${grailsApplication.config.speciesPortal.users.logo.MAX_IMAGE_SIZE/1024}KB");
					}
					else if(f.empty) {
						message = g.message(code: 'file.empty.message', default:'File cannot be empty');
					}
					else {
						if(!usersDir) {
							if(!params.dir) {
								usersDir = new File(rootDir);
								if(!usersDir.exists()) {
									usersDir.mkdir();
								}
								usersDir = new File(usersDir, UUID.randomUUID().toString()+File.separator+"resources");
								usersDir.mkdirs();
							} else {
								usersDir = new File(rootDir, params.dir);
								usersDir.mkdir();
							}
						}

						File file = observationService.getUniqueFile(usersDir, Utils.generateSafeFileName(f.originalFilename));
						f.transferTo( file );
						ImageUtils.createScaledImages(file, usersDir);
						resourcesInfo.add([fileName:file.name, size:f.size]);
					}
				}
				log.debug resourcesInfo
				// render some XML markup to the response
				if(usersDir && resourcesInfo) {
					render(contentType:"text/xml") {
						userGroup {
							dir(usersDir.absolutePath.replace(rootDir, ""))
							resources {
								for(r in resourcesInfo) {
									image('fileName':r.fileName, 'size':r.size){}
								}
							}
						}
					}
				} else {
					response.setStatus(500)
					message = [error:message]
					render message as JSON
				}
			} else {
				response.setStatus(500)
				def message = [error:g.message(code: 'no.file.attached', default:'No file is attached')]
				render message as JSON
			}
		} catch(e) {
			e.printStackTrace();
			response.setStatus(500)
			def message = [error:g.message(code: 'file.upload.fail', default:'Error while processing the request.')]
			render message as JSON
		}
	}

	@Secured(['ROLE_USER'])
	def resetPassword = {  ResetPasswordCommand command ->
		log.debug params;
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		if(SUserService.ifOwns(params.long('id'))) {

			if (!request.post) {
				return [command: new ResetPasswordCommand()]
			}
			def user = springSecurityService.currentUser
//			command.username = user.username?:email
//			println command.username;
//			String usernamePropertyName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			def command2 = new ResetPasswordCommand(username:user.username?:email, currentPassword:command.currentPassword, password:command.password, password2:command.password2, springSecurityService:springSecurityService
,saltSource:saltSource);
			command2.validate()
//
			if (command2.hasErrors()) {
				return [command: command2]
			}
			
			println 'no errors'

			String salt = saltSource instanceof NullSaltSource ? null : registrationCode.username
			SUser.withTransaction { status ->
				//def user = lookupUserClass().findWhere((usernamePropertyName): command.username)
				user.password = command2.password
				if(!user.save()) {
					log.error "Error saving password"
				}
			}

			//springSecurityService.reauthenticate command.username

			flash.message = message(code: 'spring.security.ui.resetPassword.success')
			redirect (url:uGroup.createLink(action:'show', controller:"SUser", id:params.id, 'userGroupWebaddress':params.webaddress))

		} else {
			flash.message = "${message(code: 'edit.denied.message')}";
			redirect (url:uGroup.createLink(action:'show', controller:"SUser", id:params.id, 'userGroupWebaddress':params.webaddress))
		}
	}

	private String getUserIcon(String icon) {
		if(!icon) return;

		def resource = null;
		def rootDir = grailsApplication.config.speciesPortal.users.rootDir

		File iconFile = new File(rootDir , icon);
		if(!iconFile.exists()) {
			log.error "COULD NOT locate icon file ${iconFile.getAbsolutePath()}";
		}

		resource = iconFile.absolutePath.replace(rootDir, "");
		return resource;
	}

	private processResult(List  recommendationVoteList, Long uniqueVotes, Map params) {
		try { 
			if (recommendationVoteList.size() > 0) {
				def result = [];
				recommendationVoteList.each { recoVote ->
					def map = recoVote.recommendation.getRecommendationDetails(recoVote.observation);
					//map.put("noOfVotes", map.);
					def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
					def image = recoVote.observation.mainImage()
					def imagePath = image.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, config.speciesPortal.resources.images.thumbnail.suffix)
					def imageLink = config.speciesPortal.observations.serverURL +  imagePath
					map.put('observationImage', imageLink);
					map.put("obvId", recoVote.observation.id);
					map.put('observationInstance', recoVote.observation);
					map.put("maxVotedSpeciesName", recoVote.observation.maxVotedReco?.id == recoVote.recommendation.id)
					result.add(map);
				}
				//def noOfVotes = observationService.getAllRecommendationsOfUser(userInstance);
				def model = ['hideAgree':true, 'result':result, 'totalVotes':result.size(), 'uniqueVotes':uniqueVotes, 'userGroupWebaddress':params.webaddress];
				def html = g.render(template:"/common/observation/showObservationRecosTemplate", model:model);
				def r = [
							status : 'success',
							uniqueVotes:model.uniqueVotes,
							recoHtml:html,
							msg:params.recoVoteMsg]
				render r as JSON
				return
			} else {
				//response.setStatus(500);
				def message = "";
				if(params.offset > 0) {
					message = g.message(code: 'user.recommendations.nomore.message', default:'No more recommendations made.');
				} else {
					message = g.message(code: 'user.recommendations.zero.message', default:'No recommendations made.');
				}
                def r = ['status':'info', 'msg':message];
				render r as JSON
				return
			}
		} catch(e){
			e.printStackTrace();
			//response.setStatus(500);
			def message = ['status':'error', 'msg':g.message(code: 'error', default:'Error while processing the request.')];
			render message as JSON
		}
	}

	private void addInterestedSpeciesGroups(userInstance, speciesGroups) {
		log.debug "Adding species group interests ${speciesGroups}"
		userInstance.speciesGroups = []

		speciesGroups.each {key, value ->
			userInstance.addToSpeciesGroups(SpeciesGroup.read(value.toLong()));
		}
	}

	private void addInterestedHabitats(userInstance, habitats) {
		log.debug "Adding habitat interests ${habitats}"
		userInstance.habitats = []
		habitats.each { key, value ->
			userInstance.addToHabitats(Habitat.read(value.toLong()));
		}
	}

	@Secured(['ROLE_USER'])
    def myprofile = {
		def user = springSecurityService.currentUser
		redirect (url:uGroup.createLink(action:'show', controller:"user", id:user.id, 'userGroupWebaddress':params.webaddress))
    }

}



class ResetPasswordCommand {
	String username
	String currentPassword
	String password
	String password2
	def springSecurityService
	def saltSource

	static constraints = {
		username nullable: false
		currentPassword nullable: false, blank:false, validator: { value, command ->
			def currentUser = command.springSecurityService.currentUser;
			String salt = command.saltSource instanceof NullSaltSource ? null : params.username
			if (!currentUser.password.equals(command.springSecurityService.encodePassword(value, salt))) { 
				println "doesn't match"
				return 'spring.security.ui.resetPassword.currentPassword.doesnt.match' 
			}
		} 
		password blank: false, nullable: false, validator: grails.plugins.springsecurity.ui.RegisterController.passwordValidator
		password2 validator: RegisterController.password2Validator
	}
}
