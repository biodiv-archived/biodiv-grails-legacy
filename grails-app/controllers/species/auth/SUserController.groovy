package species.auth

import groovy.sql.Sql;
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured;
import grails.plugin.springsecurity.ui.AbstractS2UiController;
import grails.plugin.springsecurity.ui.RegisterController;
import grails.plugin.springsecurity.ui.SpringSecurityUiService
import grails.plugin.springsecurity.ui.UserController;
import grails.util.GrailsNameUtils

import java.util.List
import java.util.Map

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.solr.common.util.NamedList;
import grails.plugin.springsecurity.authentication.dao.NullSaltSource
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartHttpServletRequest;

import species.BlockedMails;
import species.SpeciesPermission;
import species.participation.RecommendationVote;
import species.participation.Observation;

import species.participation.curation.UnCuratedVotes;
import species.utils.Utils;
import species.utils.ImageUtils;
import species.Habitat;
import species.groups.SpeciesGroup;
import species.participation.Follow;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import species.auth.AppKey;
import org.apache.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;

class SUserController extends UserController {

    def utilsService;
	def springSecurityService
	def namesIndexerService;
	def observationService;
	def SUserService;
	def userGroupService;
	def saltSource;
    def dataSource;
    def chartService;
    def SUserSearchService;
    def messageSource;
    def speciesPermissionService;
    
	static allowedMethods = [show:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"]]
    static defaultAction = "list"

	def isLoggedIn = { render springSecurityService.isLoggedIn() }
    
    def index () {
		redirect(action: "list", params: params)
	}

	def list() {
		params.max = Math.min(params.max ? params.int('max') : 24, 100)
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

        if(!params.loadMore?.toBoolean() && !!params.isGalleryUpdate?.toBoolean()) {
            model['resultType'] = 'user'
            model['obvListHtml'] =  g.render(template:"/common/suser/showUserListTemplate", model:model);
            model['obvFilterMsgHtml'] = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
        }

        model = utilsService.getSuccessModel("Success in executing ${actionName} of ${params.controller}", null, OK.value(), model) 
        withFormat {
            html { 
                if(params.loadMore?.toBoolean()){
                    render(template:"/common/suser/showUserListTemplate", model:model.model);
                    return;
                } else if(!params.isGalleryUpdate?.toBoolean()){
                    render (view:"list", model:model.model)
                    return;
                } else {
                    /*model['resultType'] = 'user'
                    def obvListHtml =  g.render(template:"/common/suser/showUserListTemplate", model:model);
                    def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);
                    def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]
                    render result as JSON
                    return;*/
                }
            }
            json {render model as JSON }
            xml { render model as XML }
        }
	}

	@Secured(['ROLE_ADMIN'])
	def create() {
		def user = lookupUserClass().newInstance(params)
		[user: user, authorityList: sortedRoles()]
	}

	@Secured(['ROLE_ADMIN'])
	def save() {
		def user = lookupUserClass().newInstance(params)

		if (params.password) {
			String salt = saltSource instanceof NullSaltSource ? null : params.username
			user.password = params.password; //SpringSecurityUiService.encodePassword(params.password, salt)
		}
		if (!user.save(flush: true)) {
            def model = utilsService.getErrorModel("Error in executing ${actionName} of ${params.controller}", user, UNPROCESSABLE_ENTITY.value())
            withFormat {
                html { 
                    render view: 'create', model: [user: user, authorityList: sortedRoles()]
                }
                json { render model as JSON }
                xml { render model as XML }
            }
			return
		}

		addRoles(user)
        log.debug "Publishing User on SOLR" + user
        SUserSearchService.publishSearchIndex(user, true)

        def model = utilsService.getSuccessModel("${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), user.id])}", user, CREATED.value())
        withFormat {
            html { 
                flash.message = model.msg
                redirect action: edit, id: user.id
            }
            json { render model as JSON }
            xml { render model as XML }
        }
	}

	def show() {
		def msg
		if(!params.id) {
			params.id = springSecurityService.currentUser?.id;
        }
        def userGroupInstance = null
        if(params.webaddress) {
            userGroupInstance = userGroupService.get(params['webaddress'])
        }
        def SUserInstance = SUser.get(params.long("id"))

        def userLanguage = utilsService.getCurrentLanguage(request);
        if (!SUserInstance) {
            def model = utilsService.getErrorModel("${message(code: 'default.not.found.message', args: [message(code: 'SUser.label', default: 'SUser'), params.id])}", SUserInstance, NOT_FOUND.value())
            withFormat {
                html {
                    flash.message = model.msg
                    redirect(action: "list")
                }
                json { render model as JSON }
                xml { render model as XML }
            }
        }
        else {
            def model = utilsService.getSuccessModel("", SUserInstance, OK.value())
            def result = [:];
            result['roles'] = [];
            def r = buildUserModel(SUserInstance)
            r.roleMap.each {role, granted ->
                if(granted) {
                    result.roles << role.authority
                }                    
            }
            result['stat'] = chartService.getUserStats(SUserInstance, userGroupInstance);
            result['userLanguage'] = userLanguage;
            model['model'] = result
            withFormat {
                html {
                    result = r
                    result.put('userGroupWebaddress', params.webaddress)
                    result.put('obvData', chartService.getUserStats(SUserInstance, userGroupInstance));
                    result['currentUser'] = springSecurityService.currentUser;
                    result['currentUserProfile'] = result['currentUser']?utilsService.generateLink("user", "show", ["id": result['currentUser'].id], request):'';
                    return result
                }
                json { render model as JSON }
                xml { render model as XML }

            }
        }
    }

	@Secured(['ROLE_USER', 'ROLE_ADMIN'])
	def edit() {
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		if((params.id && utilsService.ifOwns(params.long('id'))) || (params.email && utilsService.ifOwnsByEmail(params.email))) {
			def user = params.username ? lookupUserClass().findWhere((usernameFieldName): params.username) : null
			if (!user) user = findById();
            
            def model
			if (!user) model = [];
            else model = buildUserModel(user);
            withFormat {
                html {
                    return model
                }
                '*' { 
                    render model
                }
            }

            return model; 
        }

        withFormat {
            html {
                flash.message = "${message(code: 'edit.denied.message')}";
                redirect (url:uGroup.createLink(action:'show', controller:"user", id:params.id, 'userGroupWebaddress':params.webaddress))
            }
            '*' { 
                render status: UNAUTHORIZED 
            }
        }
    }

	@Secured(['ROLE_USER', 'ROLE_ADMIN'])
	def update() {
		String passwordFieldName = SpringSecurityUtils.securityConfig.userLookup.passwordPropertyName
		if((params.id && utilsService.ifOwns(params.long('id'))) || (params.email && utilsService.ifOwnsByEmail(params.email))) {
			def user = findById()

            if (!user) return;

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

			user.website = (params.website && params.website.trim() != "") ? params.website.trim().split(",").join(", ") : null

			user.language = utilsService.getCurrentLanguage(request);

			if (!user.save(flush: true)) {
                def model = utilsService.getErrorModel("Error in updating user instance", user, OK.value())
                withFormat {
                    html {
                        flash.message = model.msg
				        render view: 'edit', model: buildUserModel(user)
                        return
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
			}

			//if(params.allowIdentifactionMail?.equals('on'));

			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

			//only admin interface has roles information while editing user profile
			if(utilsService.isAdmin(user.id)) {
				lookupUserRoleClass().removeAll user
				addRoles user
			}

			userCache.removeUserFromCache user[usernameFieldName]
            log.debug "Publishing User on SOLR" + user
            SUserSearchService.publishSearchIndex(user, true)

            def model = utilsService.getSuccessModel("${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}", user, OK.value())
            withFormat {
                html { 
                    flash.message = model.msg
			        redirect (url:uGroup.createLink(action:'show', controller:"user", id:user.id, 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		} else {
            def model = utilsService.getErrorModel("${message(code: 'update.denied.message')}", null, OK.value())
            withFormat {
                html { 
                    flash.message = model.msg
                    redirect (url:uGroup.createLink(action:'show', controller:"user", id:user.id, 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}

	@Secured(['ROLE_ADMIN'])
	def delete() {
		def user = findById()
		if (!user) {
            def model = utilsService.getErrorModel("${message(code: 'info.id.not.found', args: [message(code: 'user.label', default: 'User'), params.id])}", null, NOT_FOUND.value())
            withFormat {
                html { 
                    flash.error = model.msg
                    redirect (url:uGroup.createLink(action:'list', controller:"user", id:params.id, 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
            return
        }

		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		try {
			List obvToUpdate = [];
			lookupUserClass().withTransaction { status ->

				user.recoVotes.each { vote ->
					if(vote.observation.author.id != user.id) {
						obvToUpdate.add(vote.observation);
					}
				}

				FacebookUser.removeAll user;
				lookupUserRoleClass().removeAll user
                Follow.deleteAll user;
                SpeciesPermission.removeAll user;

				user.delete(failOnError:true);
				SUserService.sendNotificationMail(SUserService.USER_DELETED, user, request, "");

			}
			//updating SpeciesName
			obvToUpdate.each { obv ->
				log.debug "Updating speciesname for ${obv}"
				obv.calculateMaxVotedSpeciesName();
			}
            userCache.removeUserFromCache user[usernameFieldName]
            def model = utilsService.getSuccessModel("${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), user.name])}", null, NO_CONTENT.value())

            withFormat {
                html { 
                    flash.message = model.msg
                    redirect (url:uGroup.createLink(action:'list', controller:"user", id:params.id, 'userGroupWebaddress':params.webaddress))
                    return;
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
		catch (DataIntegrityViolationException e) {
			e.printStackTrace();
            def model = utilsService.getErrorModel("${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), user.name])}", null, INTERNAL_SERVER_ERROR.value(),[msg:e.getMessage()])
            withFormat {
                html { 
                    flash.error = model.msg
                    redirect (url:uGroup.createLink(action:'edit', controller:"user", id:params.id, 'userGroupWebaddress':params.webaddress))

                }
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}

/*	def search () {
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields

		//def model = getUsersList(params);
		def model = SUserService.getUsersFromSearch(params);
		// add query params to model for paging
	
		//model['isSearch'] = true;
		actionName = 'search'
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
*/
	private def getUsersList(params) {
		boolean useOffset = params.containsKey('offset')
		setIfMissing 'max', 12, 100
		setIfMissing 'offset', 0
		int totalCount = 0;
		def results = [];
		def queryParams = [:]

		if(actionName != 'search' || (actionName == 'search' && params.query)) {
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
				def userName = params['query'] ? (params['query'].toLowerCase() + '%') : null
				results = chartService.getUserByRank(max, offset, userName)
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

		return ['userInstanceList': results, instanceTotal: totalCount, searchQuery:queryParams, searched: true]

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
/*	def advSearch () {
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
*/
	/**
	 * Ajax call used by autocomplete textfield.
	 */
/*	def nameTerms () {
		setIfMissing 'max', 5, 10

		def jsonData = []

		def namesLookupResults = namesIndexerService.suggest(params)
		jsonData.addAll(namesLookupResults);

		jsonData.addAll(SUserService.getUserSuggestions(params));
		withFormat {
            json { render jsonData as JSON }
            xml { render jsonData as XML }
        }
	}

	/**
	 *
	 */
	def terms () {
		setIfMissing 'max', 5, 10
		def model = SUserService.getUserSuggestions(params)
		withFormat {
            json { render model as JSON }
            xml { render model as XML }
        }
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
					result[candidateEmail] = utilsService.generateLink("observation", "unsubscribeToIdentificationMail", [email:candidateEmail], request) ;
				}
			} else {
				//its user id
				SUser user = SUser.get(candidateEmail.toLong());
				candidateEmail = user.email.trim();
				if(user.allowIdentifactionMail){
					result[candidateEmail] = utilsService.generateLink("observation", "unsubscribeToIdentificationMail", [email:candidateEmail, userId:user.id], request) ;
				}else{
					log.debug "User $user.id has unsubscribed for identification mail."
				}
			}
		}
		return result;
	}

	def loginTemplate() { 
        String s = g.render template:"/common/suser/userLoginBoxTemplate"  
        render s
    }

	def headerTemplate () {
		//TODO:HACK FOR NOW
		String domainUrl = Utils.getDomainServerUrl(request);

		if(params.webaddress) {
			def userGroupInstance = userGroupService.get(params['webaddress'])
			if(userGroupInstance) {
				render (template:"/domain/ibpHeaderTemplate", model:['userGroupInstance':userGroupInstance])
				return
			}
		}
		String s = g.render (template:"/domain/wikwioHeaderTemplate")
        render s;
	}

	def sidebarTemplate() { 
        String s = g.render template:"/common/userGroup/sidebarTemplate" 
        render s;
    }

	def footerTemplate() { 
        String s = g.render template:"/domain/wikwioFooterTemplate" 
        render s;
    }

	protected void addRoles(user) {
		String upperAuthorityFieldName = GrailsNameUtils.getClassName(
				SpringSecurityUtils.securityConfig.authority.nameField, null)

		for (String key in params.keySet()) {
			if (key.contains('ROLE') && 'on' == params.get(key)) {
                log.debug "Assigning role : ${key}" 
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
	def getRecommendationVotes () {
        params.max = params.limit ? params.int('limit') : 10
		params.offset = params.offset ? params.long('offset'): 0

		def userInstance = SUser.get(params.id?:params.filterPropertyValue)
        if (userInstance) {
            def userGroupInstance
            if(params.webaddress) {
                userGroupInstance = userGroupService.get(params['webaddress'])
            }
            def recommendationVoteList
            if(params.obvId){
                recommendationVoteList = RecommendationVote.findAllByAuthorAndObservation(springSecurityService.currentUser, Observation.read(params.long('obvId')));
            }else{
				recommendationVoteList = observationService.getRecommendationsOfUser(userInstance, params.max, params.offset, userGroupInstance);
			}
            def uniqueVotes = observationService.getAllRecommendationsOfUser(userInstance, userGroupInstance);

            def observations = recommendationVoteList.collect{it.observation};	
            def result = []
            observations.each {
                result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
            }
            def relatedObv = ['observations':result, count:uniqueVotes];
            if(relatedObv.observations) {
                    relatedObv.observations = observationService.createUrlList2(relatedObv.observations);
            } 
             
            def model = utilsService.getSuccessModel("", null, OK.value(), relatedObv)
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
		}
		else {
            def model = utilsService.getErrorModel(g.message(code: 'error', default:'Error while processing the request.'), null, INTERNAL_SERVER_ERROR.value())
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
		}
        return
	}

    def getRecommendationCount(SUser userInstance){
		Sql sql =  Sql.newInstance(dataSource);
		def result = sql.rows("select count(distinct(recoVote.recommendation_id)) from recommendation_vote as recoVote where recoVote.author_id = :userId", [userId:userInstance?.id])
		return result[0]["count"]
	}

	@Secured(['ROLE_USER'])
	def upload_resource() {
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
					log.debug "Saving user file ${f.originalFilename}"

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

						File file = utilsService.getUniqueFile(usersDir, Utils.generateSafeFileName(f.originalFilename));
						f.transferTo( file );
						ImageUtils.createScaledImages(file, usersDir);
						resourcesInfo.add([fileName:file.name, size:f.size]);
					}
				}
				log.debug resourcesInfo
				// render some XML markup to the response
				if(usersDir && resourcesInfo) {
                    withFormat {
                        json { 
                            def res = [];
                            for(r in resourcesInfo) {
                                res << ['fileName':r.fileName, 'size':r.size]
                            }
                            def model = utilsService.getSuccessModel("", null, OK.value(), [users:[dir:usersDir.absolutePath.replace(rootDir, ""), resources : res]])
                            render model as JSON
                        }
 
                        xml { 
                            render(contentType:'text/xml') {
                                response {
                                    success(true)
                                    status(OK.value())
                                    msg('Successfully uploaded the resource')
                                    model {
                                        dir(usersDir.absolutePath.replace(rootDir, ""))
                                        resources {
                                            for(r in resourcesInfo) {
                                                'image'('fileName':r.fileName, 'size':r.size){}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

				} else {
                    response.setStatus(500);
                    def model = utilsService.getErrorModel(message, null, INTERNAL_SERVER_ERROR.value())
                    withFormat {
                        json { render model as JSON }
                        xml { render model as XML }
                    }
				}
			} else {
                def model = utilsService.getErrorModel(g.message(code: 'no.file.attached', default:'No file is attached'), null, INTERNAL_SERVER_ERROR.value())
                withFormat {
                    json { render model as JSON }
                    xml { render model as XML }
                }
			}
		} catch(e) {
			e.printStackTrace();
            def model = utilsService.getErrorModel(g.message(code: 'file.upload.fail', default:'Error while processing the request.'), null, INTERNAL_SERVER_ERROR.value())
            withFormat {
                json { render model as JSON }
                xml { render model as XML }
            }
		}
	}

	@Secured(['ROLE_USER'])
	def resetPassword (ResetPasswordCommand command ) {
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
        String msg;
        boolean success = false;
		if((params.id && utilsService.ifOwns(params.long('id'))) || (params.email && utilsService.ifOwnsByEmail(params.email))) {
			def user = springSecurityService.currentUser
			def command2 = new ResetPasswordCommand(username:user.username?:email, currentPassword:command.currentPassword, password:command.password, password2:command.password2, springSecurityService:springSecurityService
,saltSource:saltSource);
			command2.validate()
			if (command2.hasErrors()) {
                msg = messageSource.getMessage("reset.password.fail", null, RCU.getLocale(request))
                def model = utilsService.getErrorModel(msg, command2, OK.value(), null);
                withFormat {
                    html {
                        //return [command:command2]
                    }
                    json { render model as JSON }
                    xml { render model as XML }
                }
                return [command:command2]
			}
			String salt = saltSource instanceof NullSaltSource ? null : registrationCode.username
			SUser.withTransaction { status ->
				//def user = lookupUserClass().findWhere((usernamePropertyName): command.username)
				user.password = command2.password
				if(!user.save()) {
					msg = msg = messageSource.getMessage("password.errors.save", null, RCU.getLocale(request))
				} else {
                    success = true;
					msg = messageSource.getMessage("password.update.success", null, RCU.getLocale(request))
                }
			}

            def model = utilsService.getSuccessModel(msg, null, OK.value(), null);
            withFormat {
                html {
                    flash.message = message(code: 'spring.security.ui.resetPassword.success')
                    redirect (url:uGroup.createLink(action:'show', controller:"user", id:params.id, 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
		} else {
			flash.message = "${message(code: 'edit.denied.message')}";
            def model = utilsService.getErrorModel(flash.message, null, OK.value(), null);
            withFormat {
                html {
			        redirect (url:uGroup.createLink(action:'show', controller:"user", id:params.id, 'userGroupWebaddress':params.webaddress))
                }
                json { render model as JSON }
                xml { render model as XML }
            }
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
    def myprofile() {
		def user = springSecurityService.currentUser
		redirect (url:uGroup.createLink(action:'show', controller:"user", id:user.id, 'userGroupWebaddress':params.webaddress))
    }

    @Secured(['ROLE_USER'])
    def myuploads(){
        def author = springSecurityService.currentUser;
		def filePickerSecurityCodes = utilsService.filePickerSecurityCodes();
		return ['springSecurityService':springSecurityService, 'userInstance':author, 'policy' : filePickerSecurityCodes.policy, 'signature': filePickerSecurityCodes.signature] 
    }

    @Secured(['ROLE_ADMIN'])
	def generateAppKey() {
        if(!params.id) {
            render ([success:false, msg:'Missing user id', 'errors':[]] as JSON)
            return;
        }

        def author = SUser.read(params.long('id'));
        if(!author) {
            render ([success:false, msg:"No user with id ${params.id}", 'errors':[]] as JSON)
            return;
        }


        AppKey appKey = AppKey.findByEmail(author.email);
        if(appKey) {
            appKey.key = UUID.randomUUID().toString()
        } else {
            appKey = new AppKey(key:UUID.randomUUID().toString(), email:author.email);
        }

        if(!appKey.save(flush:true)) {
            def errors = [];
		    appKey.errors?.allErrors.each { 

                def formattedMessage = messageSource.getMessage(it, null);
                errors << [field: it.field, message: formattedMessage]
                log.error it 
            }
            render ([success:false, msg:'Error in creating/updating app key', 'errors':errors] as JSON)
        }

		SUserService.sendNotificationMail(SUserService.APP_KEY, author, request, "",[appKey:appKey.key]);
        render ([success:true, 'appKey':appKey.key, 'msg':'This app key is also sent in an email to your account'] as JSON)

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
				return 'spring.security.ui.resetPassword.currentPassword.doesnt.match' 
			}
		} 
		password blank: false, nullable: false, validator: grails.plugin.springsecurity.ui.RegisterController.passwordValidator
		password2 validator: RegisterController.password2Validator
	}
}
