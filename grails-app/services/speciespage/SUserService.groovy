package speciespage

import grails.plugin.springsecurity.ui.SpringSecurityUiService;
import grails.util.Environment;
import groovy.text.SimpleTemplateEngine;

import grails.plugin.springsecurity.SpringSecurityUtils;
import grails.plugin.springsecurity.ui.RegistrationCode;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

import species.auth.SUser;
import species.participation.Observation;
import species.utils.Utils;
import species.Language;
import species.utils.ImageType
import speciespage.search.SUserSearchService;
import species.SpeciesPermission;
import species.SpeciesPermission.PermissionType;
import org.springframework.context.MessageSource;
import au.com.bytecode.opencsv.CSVWriter
import species.participation.Observation;
import speciespage.ObvUtilService;
import org.springframework.context.i18n.LocaleContextHolder as LCH;

class SUserService extends SpringSecurityUiService implements ApplicationContextAware {

	def grailsApplication

	def springSecurityService
	def mailService
	def SUserSearchService
	def speciesPermissionService
    def messageSource;
    def request;
    def utilsService;
    def obvUtilService
    private ApplicationTagLib g
	ApplicationContext applicationContext

	public static final String NEW_USER = "newUser";
	public static final String USER_DELETED = "deleteUser";
	public static final String APP_KEY = "app_key";


	/**
	 * 
	 */
	SUser create(propsMap, Language userLanguage=null) {
		log.debug("Creating new User");
		propsMap = propsMap ?: [:];

        if(propsMap.containsKey('metaClass')) {
		    propsMap.remove('metaClass')
        }
        if(propsMap.containsKey('class')) {
            propsMap.remove('class')
        }

		String userDomainClassName = SpringSecurityUtils.securityConfig.userLookup.userDomainClassName

		Class<?> UserDomainClass = grailsApplication.getDomainClass(userDomainClassName).clazz
		if (!UserDomainClass) {
			log.error("Can't find user domain: $userDomainClassName")
			return null
		}

		def user = UserDomainClass.newInstance(propsMap);
        if(!userLanguage) userLanguage = utilsService.getCurrentLanguage();
        user.language = userLanguage;
		user.enabled = true;
		return user;
	}

	/**
	 * 
	 * @param user
	 * @param cleartextPassword
	 * @param salt
	 * @return
	 */
	SUser save(user) {
		log.debug "Saving $user"
		if (!user.save(flush:true, failOnError:true)) {
			warnErrors user, messageSource
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
			return null
		}
		SUserSearchService.publishSearchIndex(user, true);
		return user
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	RegistrationCode register(String username) {
		if(!username) return null;

		def registrationCode = new RegistrationCode(username: username)
		if (!registrationCode.save()) {
			warnErrors registrationCode, messageSource
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
		}

		registrationCode
	}

	/**
	 * 
	 * @param user
	 */
	void assignRoles(SUser user) {

		def securityConf = SpringSecurityUtils.securityConfig

		def defaultRoleNames = securityConf.ui.register.defaultRoleNames;

		Class<?> PersonRole = grailsApplication.getDomainClass(securityConf.userLookup.authorityJoinClassName).clazz
		Class<?> Authority = grailsApplication.getDomainClass(securityConf.authority.className).clazz

		PersonRole.withTransaction { status ->
			defaultRoleNames.each { String roleName ->
				String findByField = securityConf.authority.nameField[0].toUpperCase() + securityConf.authority.nameField.substring(1)
				def auth = Authority."findBy${findByField}"(roleName)
				if (auth) {
					log.debug "Assigning role $auth to user $user"
					PersonRole.create(user, auth)
				} else {
					log.error("Can't find authority for name '$roleName'")
				}
			}
		}
	}

    public void sendNotificationMail(String notificationType, SUser user, request, String userProfileUrl, Map otherParams=null){
		 
		def conf = SpringSecurityUtils.securityConfig
		g = applicationContext.getBean(ApplicationTagLib)

		//def userProfileUrl = generateLink("SUser", "show", ["id": user.id], request)

		def templateMap = [username: user.name.capitalize(), email:user.email, userProfileUrl:userProfileUrl, domain:Utils.getDomainName(request), grailsApplication:grailsApplication]

		def mailSubject = ""
		def bodyContent = ""
        String domain = Utils.getDomainName(request)

		def replyTo = conf.ui.notification.emailReplyTo;
		switch ( notificationType ) {
			case NEW_USER:
				def messagesourcearg = new Object[1];
                messagesourcearg[0] = domain;
				mailSubject = messageSource.getMessage("grails.plugin.springsecurity.ui.newuser.emailSubject", messagesourcearg, LCH.getLocale())
			//bodyContent = g.render(template:"/emailtemplates/welcomeEmail", model:templateMap)
				if (mailSubject.contains('$')) {
					mailSubject = evaluate(mailSubject, [domain: Utils.getDomainName(request)])
				}

					try {
						def userLanguage = utilsService.getCurrentLanguage();
						mailService.sendMail {
							to user.email
				            if (Environment.getCurrent().getName().equalsIgnoreCase("kk")) {
	                            bcc grailsApplication.config.speciesPortal.app.notifiers_bcc.toArray()
                            }
							//bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com","thomas.vee@gmail.com", "sandeept@strandls.com"
							from grailsApplication.config.grails.mail.default.from
							subject mailSubject
							body(view:"/emailtemplates/"+userLanguage.threeLetterCode+"/welcomeEmail", model:templateMap)
						}
						log.debug "Sent mail for notificationType ${notificationType} to ${user.email}"
					}catch(all)  {
					    log.error all.getMessage()
                        all.printStackTrace();
					}
				

				return;
			case USER_DELETED:
				def messagesourcearg = new Object[1];
                messagesourcearg[0] = domain;
				mailSubject = messageSource.getMessage("grails.plugin.springsecurity.ui.userdeleted.emailSubject", messagesourcearg, LCH.getLocale())
				def msgsourcearg = new Object[2];
                msgsourcearg[0] = user.email;
                msgsourcearg[1] = domain;
				bodyContent = messageSource.getMessage("grails.plugin.springsecurity.ui.userdeleted.emailBody", msgsourcearg, LCH.getLocale())
				if (bodyContent.contains('$')) {
					bodyContent = evaluate(bodyContent, templateMap)
				}

				if (mailSubject.contains('$')) {
					mailSubject = evaluate(mailSubject, [domain: Utils.getDomainName(request)])
				}
                
                try {
                    if(bodyContent && mailSubject) {
                        mailService.sendMail {

                            if (Environment.getCurrent().getName().equalsIgnoreCase("kk")) {
                                bcc grailsApplication.config.speciesPortal.app.notifiers_bcc.toArray()
                                    //bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com","thomas.vee@gmail.com","sandeept@strandls.com"
                            } else {
                                bcc 'sravanthi@strandls.com'                                
                            }
                            subject mailSubject
                            html bodyContent.toString()
                        }
                    }
                } catch(all)  {
                    log.error all.getMessage()
                    all.printStackTrace();
                }


				return;
			case APP_KEY:
				def messagesourcearg = new Object[1];
                messagesourcearg[0] = domain;
				mailSubject = messageSource.getMessage("app.key.emailSubject", messagesourcearg, LCH.getLocale())
				def msgsourcearg = new Object[3];
                msgsourcearg[0] = user.name;
                msgsourcearg[1] = domain;
                msgsourcearg[2] = otherParams.appKey;
				bodyContent = messageSource.getMessage("app.key.emailBody", msgsourcearg, LCH.getLocale())
				if (bodyContent.contains('$')) {
					bodyContent = evaluate(bodyContent, templateMap)
				}

				if (mailSubject.contains('$')) {
					mailSubject = evaluate(mailSubject, [domain: Utils.getDomainName(request)])
				}

				break

			default:
				log.debug "invalid notification type"
		}

        try {
            if(bodyContent && mailSubject) {
                mailService.sendMail {

					to user.email
                    if (Environment.getCurrent().getName().equalsIgnoreCase("kk")) {
                        bcc grailsApplication.config.speciesPortal.app.notifiers_bcc.toArray()
                            //bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com","thomas.vee@gmail.com","sandeept@strandls.com"
                    }
                        subject mailSubject
                        html bodyContent.toString()
                }
            }
        }catch(all)  {
            log.error all.getMessage()
            all.printStackTrace();
        }

	}

	protected String evaluate(s, binding) {
		new SimpleTemplateEngine().createTemplate(s).make(binding)
	}

	def nameTerms(params) {
		return getUserSuggestions(params);
	}

	def getUserSuggestions(params){
		def jsonData = []
		String username = params.term

        if(username) {
		String usernameFieldName = 'name';//SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		String userId = 'id';


		def results = grailsApplication.getDomainClass(SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz.executeQuery(
				"SELECT DISTINCT u.$usernameFieldName, u.$userId " +
				"FROM ${SpringSecurityUtils.securityConfig.userLookup.userDomainClassName} u " +
				"WHERE LOWER(u.$usernameFieldName) LIKE :name " +
				"ORDER BY u.$usernameFieldName",
				[name: "${username.toLowerCase()}%"],
				[max: params.max])

		for (result in results) {
			def profile_pic = SUser.read(result[1]).profilePicture(ImageType.SMALL);
			jsonData << [value: result[0], label:result[0] , userId:result[1] , "category":"Members", "user_pic" : profile_pic]
		}
        }
		return jsonData;
	}
	
	/**
	 * User Search 
	 **/

	 def getUsersFromSearch(params)  {
		def max = Math.min(params.max ? params.max.toInteger() : 12, 100)
		def offset = params.offset ? params.offset.toLong() : 0
		def model;
		try {
		    model = getFilteredUsersFromSearch(params, max, offset);
		} catch(SolrException e) {
		    e.printStackTrace();
		}
		return model;
	 }

         /**
	   * Filter observations by group, habitat, tag, user, species
	   * max: limit results to max: if max = -1 return all results
	   * offset: offset results: if offset = -1 its not passed to the
	   * executing query
	   */
	 Map getFilteredUsersFromSearch(params, max, offset){
	 	def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
		//Store all teh parameter list here
		NamedList paramsList = new NamedList();
		//Check for the parameters passed by user and convert them to solr queries
		def queryParams = [:]
		queryParams["query"] = params.query
		queryParams["max"]  = max

		params.query = params.query ?:""
		paramsList.add('q', Utils.cleanSearchQuery(params.query))
		paramsList.add('start', offset);
		paramsList.add('rows', max);
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase();
		if(isValidSortParam(sort)) {
		     if(sort.indexOf(' desc') == -1) {
		            sort += " desc";
		     }
		     paramsList.add('sort', sort);
		}
		
		List<SUser> userList = new ArrayList<SUser>();
		def totalUserList = []
		def responseHeader
		long noOfResults = 0
		if(paramsList)  {
			def queryResult = SUserSearchService.search(paramsList);

			Iterator it = queryResult.getResults().listIterator();

			while(it.hasNext()) {
				def doc = it.next()
				def user = SUser.read(Long.parseLong(doc.getFieldValue("id").tokenize("_")[1] + ""))

				if(user)  {
					totalUserList.add(Long.parseLong(doc.getFieldValue("id").tokenize("_")[1] + ""))
					userList.add(user)
				}
			}
			responseHeader = queryResult?.responseHeader
			noOfResults = queryResult?.getResults().getNumFound()
		}

		return [responseHeader:responseHeader, userInstanceList:userList, resultType:'user', instanceTotal:noOfResults, searchQuery:queryParams, totalUserIdList:totalUserList, searched:true, isSearch:true ] 
	}
        
	private boolean isValidSortParam(String sortParam) {
	    if(sortParam.equalsIgnoreCase("score") || sortParam.equalsIgnoreCase("name")  || sortParam.equalsIgnoreCase("lastLoginDate") )
	          return true;
	    return false;
	}
		def downloadUserDetails(){
                File csvFile = new File('/home/ifp/git/biodiv/app-conf/traitvalue.tsv', "upload_report.csv")
                CSVWriter writer = obvUtilService.getCSVWriter(csvFile.getParent(), csvFile.getName())
                writer.writeNext("Document Title:#user Detail:".split("#"))   
                writer.writeNext(("${documentInstance.title}#Success").split("#"))
                writer.writeNext("Total no of document uploaded successfully#${successCount}".split("#"))
                writer.writeNext("Total no of document failed to upload#${failedCount}".split("#"))
                writer.flush()
                writer.close()
}

}
