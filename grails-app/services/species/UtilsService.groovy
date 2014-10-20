package species;

import species.groups.UserGroupController;
import species.utils.Utils;
import org.codehaus.groovy.grails.web.util.WebUtils;
import species.groups.UserGroup;
import species.auth.SUser;
import species.participation.ActivityFeed;
import species.participation.Comment;
import speciespage.ObvUtilService
import species.participation.ActivityFeedService
import grails.plugin.springsecurity.SpringSecurityUtils;
import species.participation.Checklists;
import species.participation.Observation;
import grails.util.Environment;
import species.utils.ImageType;
import species.groups.SpeciesGroup;
import species.CommonNames;
import org.springframework.web.servlet.support.RequestContextUtils as RCU; 
import java.beans.Introspector;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder as LCH;

class UtilsService {

    def grailsApplication;
    def grailsLinkGenerator;
    def sessionFactory
    def mailService;
    def springSecurityService
    def messageSource;
    //def observationService
 //   def activityFeedService

    static final String OBSERVATION_ADDED = "observationAdded";
    static final String SPECIES_RECOMMENDED = "speciesRecommended";
    static final String SPECIES_AGREED_ON = "speciesAgreedOn";
    static final String SPECIES_NEW_COMMENT = "speciesNewComment";
    static final String SPECIES_REMOVE_COMMENT = "speciesRemoveComment";
    static final String OBSERVATION_FLAGGED = "observationFlagged";
    static final String OBSERVATION_DELETED = "observationDeleted";
    static final String CHECKLIST_DELETED= "checklistDeleted";
    static final String DOWNLOAD_REQUEST = "downloadRequest";
    //static final int MAX_EXPORT_SIZE = -1;
    static final String REMOVE_USERS_RESOURCE = "deleteUsersResource";
    static final String NEW_SPECIES_PERMISSION = "New permission on species"

    static final String SPECIES_CONTRIBUTOR = "speciesContributor";
    static final String SPECIES_CURATORS = "speciesCurators"

    static final String DIGEST_MAIL = "digestMail";
    static final String DIGEST_PRIZE_MAIL = "digestPrizeMail";

    private void cleanUpGorm() {
        cleanUpGorm(true)
    }

    private void cleanUpGorm(boolean clearSession) {

        def hibSession = sessionFactory?.getCurrentSession();

        if(hibSession) {
            log.debug "Flushing and clearing session"
            try {
                hibSession.flush()
            } catch(Exception e) {
                e.printStackTrace()
            }
            if(clearSession){
                hibSession.clear()
            }
        }
    }

    public String generateLink( String controller, String action, linkParams, request=null) {
        request = (request) ?:(WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest())
        return userGroupBasedLink(base: Utils.getDomainServerUrl(request),
            controller:controller, action: action,
            params: linkParams)
    }

    public createHardLink(controller, action, id){
        return "" + Utils.getIBPServerDomain() + "/" + controller + "/" + action + "/" + id 
    }

    def userGroupBasedLink(attrs) {
        def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        String url = "";

        if(attrs.userGroupInstance) attrs.userGroup = attrs.userGroupInstance
            if(attrs.userGroup && attrs.userGroup.id) {
                attrs.webaddress = attrs.userGroup.webaddress
                String base = attrs.remove('base')
                String controller = attrs.remove('controller')
                String action = attrs.remove('action');
                String mappingName = attrs.remove('mapping')?:'userGroupModule';
                def userGroup = attrs.remove('userGroup');
                attrs.remove('userGroupWebaddress');
                boolean absolute = attrs.remove('absolute');
                if(attrs.params) {
                    attrs.putAll(attrs.params);
                    attrs.remove('params');
                }
                if(base) {
                    url = grailsLinkGenerator.link(mapping:mappingName, 'controller':controller, 'action':action, 'base':base, absolute:absolute, params:attrs);
                    String onlyGroupUrl = grailsLinkGenerator.link(mapping:'onlyUserGroup', params:['webaddress':attrs.webaddress]).replace("/"+grailsApplication.metadata['app.name']+'/','/')
                    url = url.replace(onlyGroupUrl, "");
                } else {

                    if((userGroup?.domainName)) { 
                        url = grailsLinkGenerator.link(mapping:mappingName, 'controller':controller, base:userGroup.domainName, 'action':action, absolute:absolute, params:attrs);
                        String onlyGroupUrl = grailsLinkGenerator.link(mapping:'onlyUserGroup', params:['webaddress':attrs.webaddress]).replace("/"+grailsApplication.metadata['app.name']+'/','/')
                        url = url.replace(onlyGroupUrl, "");
                    } else {
                        url = grailsLinkGenerator.link(mapping:mappingName, 'controller':controller, base:Utils.getIBPServerDomain(), 'action':action, absolute:absolute, params:attrs);
                    }
                }

            } else if(attrs.userGroupWebaddress) {
                attrs.webaddress = attrs.userGroupWebaddress
                String base = attrs.remove('base')
                String controller = attrs.remove('controller')
                String action = attrs.remove('action');
                String mappingName = attrs.remove('mapping')?:'userGroupModule';
                def userGroup = attrs.remove('userGroup');
                String userGroupWebaddress = attrs.remove('userGroupWebaddress');
                boolean absolute = attrs.remove('absolute');
                def userGroupController = new UserGroupController();
                userGroup = userGroupController.findInstance(null, userGroupWebaddress, false);
                if(attrs.params) {
                    attrs.putAll(attrs.params);
                    attrs.remove('params');
                }
                if(base) {
                    url = grailsLinkGenerator.link(mapping:mappingName, 'controller':controller, 'action':action, 'base':base, absolute:absolute, params:attrs)
                    String onlyGroupUrl = grailsLinkGenerator.link(mapping:'onlyUserGroup', params:['webaddress':attrs.webaddress]).replace("/"+grailsApplication.metadata['app.name']+'/','/')
                    url = url.replace(onlyGroupUrl, "");
                } else {

                    if((userGroup?.domainName)) { 
                        url = grailsLinkGenerator.link(mapping:mappingName, 'controller':controller, base:userGroup.domainName, 'action':action, absolute:absolute, params:attrs)
                        String onlyGroupUrl = grailsLinkGenerator.link(mapping:'onlyUserGroup', params:['webaddress':attrs.webaddress]).replace("/"+grailsApplication.metadata['app.name']+"/",'/')
                        url = url.replace(onlyGroupUrl, "");
                    } else {
                        url = grailsLinkGenerator.link(mapping:mappingName, 'controller':controller, base:Utils.getIBPServerDomain(), 'action':action, absolute:absolute, params:attrs)
                    }
                }

            } else {
                String base = attrs.remove('base')
                String controller = attrs.remove('controller')
                String action = attrs.remove('action');
                attrs.remove('userGroup');
                attrs.remove('userGroupWebaddress');
                String mappingName = attrs.remove('mapping');
                boolean absolute = attrs.remove('absolute');
                if(attrs.params) {
                    attrs.putAll(attrs.params);
                    attrs.remove('params');
                }
                if(base) {
                    url = grailsLinkGenerator.link(mapping:mappingName, 'base':base, 'controller':controller, 'action':action, absolute:absolute, params:attrs).replace("/"+grailsApplication.metadata['app.name']+'/','/')
                } else {
                    url = grailsLinkGenerator.link(mapping:mappingName, 'controller':controller, 'action':action, absolute:absolute, params:attrs).replace("/"+grailsApplication.metadata['app.name']+'/','/')
                }
            }
        return url;
    }


    File getUniqueFile(File root, String fileName){
        File imageFile = new File(root, fileName);

        if(!imageFile.exists()) {
            return imageFile
        }

        int i = 0;
        int duplicateFileLimit = 20
        while(++i < duplicateFileLimit){
            def newFileName = "" + i + "_" + fileName
            File newImageFile = new File(root, newFileName);

            if(!newImageFile.exists()){
                return newImageFile
            }

        }
        log.error "Too many duplicate files $fileName"
        return imageFile
    }


    //Create file with given filename
    def File createFile(String fileName, String uploadDir, String contentRootDir) {
        File uploaded
        if (uploadDir) {
            File fileDir = new File(contentRootDir + "/"+ uploadDir)
            if(!fileDir.exists())
                fileDir.mkdirs()
                uploaded = getUniqueFile(fileDir, Utils.generateSafeFileName(fileName));

        } else {

            File fileDir = new File(contentRootDir)
            if(!fileDir.exists())
                fileDir.mkdirs()
                uploaded = getUniqueFile(fileDir, Utils.generateSafeFileName(fileName));
            //uploaded = File.createTempFile('grails', 'ajaxupload')
        }

        log.debug "New file created : "+ uploaded.getPath()
        return uploaded
    }

    def getUserGroup(params) {
        if(params.userGroup && params.userGroup instanceof UserGroup) {
            return params.userGroup
        }

        if(params.webaddress || (params.userGroup && (params.userGroup instanceof String || params.userGroup instanceof Long ))) {
            def userGroupController = new UserGroupController();
            return userGroupController.findInstance(params.userGroup, params.webaddress);
        }

        return null;
    }
    Language getCurrentLanguage(request){
       // println "====================================="+request
        String langStr = RCU.getLocale(request)
        def (langtwo, lang1) = langStr.tokenize( '_' );
        def languageInstance = Language.findByTwoLetterCode(langtwo);
        return languageInstance?languageInstance:Language.getLanguage(Language.DEFAULT_LANGUAGE);
    }

    /**
     */
    public sendNotificationMail(String notificationType, def obv, request, String userGroupWebaddress, ActivityFeed feedInstance=null, otherParams = null) {
        def conf = SpringSecurityUtils.securityConfig
        log.debug "Sending email"
        try {

            def targetController =  getTargetController(obv)//obv.getClass().getCanonicalName().split('\\.')[-1]
            def obvUrl, domain, baseUrl

            try {
                request = (request) ?:(WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest())
            } catch(IllegalStateException e) {
                log.error e.getMessage();
            }

            def userLanguage=getCurrentLanguage(request)
            if(request) {
                obvUrl = generateLink(targetController, "show", ["id": obv.id], request)
                domain = Utils.getDomainName(request)
                baseUrl = Utils.getDomainServerUrl(request)
            }

            def templateMap = [obvUrl:obvUrl, domain:domain, baseUrl:baseUrl]
            //println "testing obs====================="+userLanguage;
            templateMap["currentUser"] = springSecurityService.currentUser
            templateMap["action"] = notificationType;
            templateMap["siteName"] = grailsApplication.config.speciesPortal.app.siteName;
            def mailSubject = ""
            def bodyContent = ""
            String htmlContent = ""
            String bodyView = '';
            def replyTo = conf.ui.notification.emailReplyTo;
            Set toUsers = []
            //Set bcc = ["xyz@xyz.com"];
            //def activityModel = ['feedInstance':feedInstance, 'feedType':ActivityFeedService.GENERIC, 'feedPermission':ActivityFeedService.READ_ONLY, feedHomeObject:null]

            switch ( notificationType ) {
                case [OBSERVATION_ADDED, ActivityFeedService.OBSERVATION_UPDATED]:
                if( notificationType == OBSERVATION_ADDED ) {
                    mailSubject = messageSource.getMessage("mail.obs.added", null, LCH.getLocale())
                    templateMap["message"] = messageSource.getMessage("mail.add.obs", null, LCH.getLocale())
                } else {
                    mailSubject = messageSource.getMessage("mail.obs.added", null, LCH.getLocale())
                    mailSubject = messageSource.getMessage("mail.obs.updated", null, LCH.getLocale())
                    templateMap["message"] = messageSource.getMessage("mail.following.obs", null, LCH.getLocale())
                }
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.add(getOwner(obv))
                break

                case [ActivityFeedService.CHECKLIST_CREATED, ActivityFeedService.CHECKLIST_UPDATED]:
                if( notificationType == ActivityFeedService.CHECKLIST_CREATED ) {
                    mailSubject = messageSource.getMessage("mail.list.added", null, LCH.getLocale())
                    def messagesourcearg = new Object[1];
                    messagesourcearg[0] = templateMap['domain'];
                    templateMap["message"] = messageSource.getMessage("mail.upload.list", messagesourcearg, LCH.getLocale())+ "<a href=\"${templateMap['obvUrl']}\">"+messageSource.getMessage("msg.here", null, LCH.getLocale())+"</a>"
                } else {
                    mailSubject = messageSource.getMessage("mail.list.updated", null, LCH.getLocale())
                    def messagesourcearg = new Object[1];
                    messagesourcearg[0] = templateMap['domain'];
                    templateMap["message"] = messageSource.getMessage("mail.update.list", messagesourcearg, LCH.getLocale())+ "<a href=\"${templateMap['obvUrl']}\">"+messageSource.getMessage("msg.here", null, LCH.getLocale())+"</a>"
                }
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                templateMap["actionObject"] = "checklist"
                toUsers.add(getOwner(obv))
                break

                case SPECIES_CURATORS:
                mailSubject = "Request to curate species"
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/speciesCurators"
                templateMap["link"] = otherParams["link"]
                templateMap["curator"] = otherParams["curator"]
                //templateMap["link"] = URLDecoder.decode(templateMap["link"])
                //println "========THE URL  =============" + templateMap["link"]
                populateTemplate(obv, templateMap,userGroupWebaddress, feedInstance, request )
                toUsers = otherParams["usersMailList"]
                break

                case SPECIES_CONTRIBUTOR:
                mailSubject = "Species uploaded"
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/speciesContributor"
                templateMap["link"] = otherParams["link"]
                def user = springSecurityService.currentUser;                
                templateMap["contributor"] = user.name
                templateMap["speciesCreated"] = otherParams["speciesCreated"]
                templateMap["speciesUpdated"] = otherParams["speciesUpdated"]
                templateMap["stubsCreated"] = otherParams["stubsCreated"]
                templateMap["uploadCount"] = otherParams["uploadCount"]
                populateTemplate(obv, templateMap,userGroupWebaddress, feedInstance, request )
                toUsers.add(user)
                break

                case OBSERVATION_FLAGGED :
                mailSubject = getResType(obv).capitalize() + " flagged"
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                toUsers.add(getOwner(obv))
                if(obv?.getClass() == Observation) {
                    templateMap["actionObject"] = 'obvSnippet'
                }
                else {
                    templateMap["actionObject"] = 'usergroup'
                }
                templateMap["message"] = " flagged your " + getResType(obv)
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                break

                case OBSERVATION_DELETED :
                mailSubject = messageSource.getMessage("grails.plugin.springsecurity.ui.observationDeleted.emailSubject", null, LCH.getLocale())
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                templateMap["message"] = messageSource.getMessage("mail.delete.obs", null, LCH.getLocale())
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.add(getOwner(obv))
                break

                case CHECKLIST_DELETED :
                mailSubject = messageSource.getMessage("grails.plugin.springsecurity.ui.checklistDeleted.emailSubject", null, LCH.getLocale())
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                templateMap["actionObject"] = messageSource.getMessage("default.checklist.label", null, LCH.getLocale())
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = templateMap['obvUrl'];
                templateMap["message"] = messageSource.getMessage("mail.delete.list", messagesourcearg, LCH.getLocale())
                toUsers.add(getOwner(obv))
                break


                case SPECIES_RECOMMENDED :
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                mailSubject = messageSource.getMessage("mail.name.suggest", null, LCH.getLocale())
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(getParticipants(obv))
                break

                case SPECIES_AGREED_ON:
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                mailSubject = messageSource.getMessage("mail.name.suggest", null, LCH.getLocale())
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(getParticipants(obv))
                break

                case ActivityFeedService.RECOMMENDATION_REMOVED:
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                mailSubject = messageSource.getMessage("grails.plugin.springsecurity.ui.removeRecommendationVote.emailSubject", null, LCH.getLocale())
                toUsers.addAll(getParticipants(obv))
                break

                case [ActivityFeedService.RESOURCE_POSTED_ON_GROUP,  ActivityFeedService.RESOURCE_REMOVED_FROM_GROUP]:
                mailSubject = feedInstance.activityDescrption
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                templateMap["actionObject"] = obv.class.simpleName.toLowerCase()
                //templateMap['message'] = ActivityFeedService.getContextInfo(feedInstance, [:])
                templateMap["groupNameWithlink"] = ActivityFeedService.getUserGroupHyperLink(getDomainObject(feedInstance.activityHolderType, feedInstance.activityHolderId));
                toUsers.addAll(getParticipants(obv))
                break

                case ActivityFeedService.COMMENT_ADDED:				
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                templateMap["userGroupWebaddress"] = userGroupWebaddress
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = templateMap['domainObjectType'];

                mailSubject = messageSource.getMessage("mail.new.comment", messagesourcearg, LCH.getLocale())
                templateMap['message'] = messageSource.getMessage("mail.added.comment", null, LCH.getLocale())
                templateMap['discussionUrl'] = generateLink('activityFeed', 'list', [], request)
                toUsers.addAll(getParticipants(obv))
                break;

                case "COMMENT_ADD_USER_TAG":
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                templateMap["userGroupWebaddress"] = userGroupWebaddress
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = templateMap['domainObjectType'];
                mailSubject = messageSource.getMessage("mail.tagged.comment", messagesourcearg, LCH.getLocale())
                templateMap['message'] = messageSource.getMessage("mail.tag.info", null, LCH.getLocale())
                templateMap['discussionUrl'] =  generateLink('activityFeed', 'list', [], request)
                toUsers.addAll(otherParams["taggedUsers"])
                break;

                case SPECIES_REMOVE_COMMENT:
                mailSubject = messageSource.getMessage("mail.comment.remove", null, LCH.getLocale())
                //bodyView = "/emailtemplates/addObservation"
                //populateTemplateMap(obv, templateMap)
               def messagesourcearg = new Object[4];
                messagesourcearg[0] = username;
                messagesourcearg[1] = domain;
                messagesourcearg[2] = obvUrl;
                messagesourcearg[3] = userProfileUrl;

                bodyContent = messageSource.getMessage("grails.plugin.springsecurity.ui.removeComment.emailBody", messagesourcearg, LCH.getLocale())
                toUsers.add(getOwner(obv))
                break;

                case DOWNLOAD_REQUEST:
                mailSubject = messageSource.getMessage("grails.plugin.springsecurity.ui.downloadRequest.emailSubject", null, LCH.getLocale())
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                toUsers.add(getOwner(obv))
                templateMap['userProfileUrl'] = createHardLink('user', 'show', obv.author.id)
                templateMap['message'] = messageSource.getMessage("grails.plugin.springsecurity.ui.downloadRequest.message", null, LCH.getLocale())
                break;

                case ActivityFeedService.DOCUMENT_CREATED:
                mailSubject = messageSource.getMessage("grails.plugin.springsecurity.ui.addDocument.emailSubject", null, LCH.getLocale())
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                def messagesourcearg = new Object[1];
                messagesourcearg[0] = domain;
                templateMap["message"] = messageSource.getMessage("mail.upload.doc", messagesourcearg, LCH.getLocale())
                toUsers.add(getOwner(obv))
                break

                case [ActivityFeedService.FEATURED, ActivityFeedService.UNFEATURED]:
                boolean a
                if(notificationType == ActivityFeedService.FEATURED) {
                    a = true
                }
                else { 
                    a = false
                }
                mailSubject = ActivityFeedService.getDescriptionForFeature(obv, null , a)
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                def ug = getDomainObject(feedInstance.activityHolderType, feedInstance.activityHolderId)
                def groupName
                if(obv == ug){
                    groupName = grailsApplication.config.speciesPortal.app.siteName 
                }
                else{
                    groupName = getUserGroupHyperLink(ug)
                }
                //templateMap["groupNameWithlink"] = groupName
                templateMap["message"] = getDescriptionForFeature(obv, null, a) + (a ? " in : " : " from : ") + groupName

                if(obv?.getClass() == Observation) {
                    templateMap["actionObject"] = 'obvSnippet'
                }
                else {
                    templateMap["actionObject"] = 'usergroup'
                }
                toUsers.addAll(getParticipants(obv))
                break

                case DIGEST_MAIL:
                templateMap["serverURL"] =  grailsApplication.config.grails.serverURL
                templateMap["siteName"] = grailsApplication.config.speciesPortal.app.siteName
                templateMap["resourcesServerURL"] = grailsApplication.config.speciesPortal.resources.serverURL
                templateMap["grailsApplication"] = grailsApplication
                mailSubject = "Activity digest on " + otherParams["userGroup"].name
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/digest"
                templateMap["digestContent"] = otherParams["digestContent"]
                templateMap["userGroup"] = otherParams["userGroup"]
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(otherParams["usersEmailList"]);
                //toUsers.addAll(SUser.get(4136L));
                break

                case DIGEST_PRIZE_MAIL:
                mailSubject = "Neighborhood Trees Campaign extended till tonight"
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/digestPrizeEmail"
                templateMap["userGroup"] = otherParams["userGroup"]
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(otherParams["usersEmailList"]);
                break
                
                case [ActivityFeedService.SPECIES_CREATED, ActivityFeedService.SPECIES_UPDATED]:
                mailSubject = notificationType;
                    if(otherParams['resURLs']){
                        templateMap['resURLs'] = otherParams['resURLs']
                    }
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                if(notificationType == ActivityFeedService.SPECIES_CREATED){
                    templateMap["message"] = messageSource.getMessage("mail.added.species", null, LCH.getLocale())
                } else {
                    //templateMap['domainObjectType'] = 'species'
                    templateMap['obvUrl'] = generateLink("species", "show", ["id": otherParams['spId']], request)
                    templateMap['obvId'] = otherParams['spId']
                    templateMap["message"] = messageSource.getMessage("mail.updated.species", null, LCH.getLocale())
                }
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(getParticipants(obv))
                break

                case REMOVE_USERS_RESOURCE:
                mailSubject = messageSource.getMessage("mail.info.message", null, LCH.getLocale())
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/deleteUsersResource"
                templateMap["uploadedDate"] = otherParams["uploadedDate"]
                templateMap["toDeleteDate"] = otherParams["toDeleteDate"]
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(otherParams["usersList"])
                break


                case [ActivityFeedService.SPECIES_FIELD_CREATED, ActivityFeedService.SPECIES_SYNONYM_CREATED, ActivityFeedService.SPECIES_COMMONNAME_CREATED, ActivityFeedService.SPECIES_HIERARCHY_CREATED] :
                mailSubject = notificationType;
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                templateMap["message"] = Introspector.decapitalize(otherParams['info']);
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(getParticipants(obv))
                break


                case [ActivityFeedService.SPECIES_FIELD_UPDATED, ActivityFeedService.SPECIES_SYNONYM_UPDATED, ActivityFeedService.SPECIES_COMMONNAME_UPDATED, ActivityFeedService.SPECIES_HIERARCHY_UPDATED] :
                mailSubject = notificationType;
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                templateMap["message"] = Introspector.decapitalize(otherParams['info']);
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(getParticipants(obv))
                break

                case [ActivityFeedService.SPECIES_FIELD_DELETED, ActivityFeedService.SPECIES_SYNONYM_DELETED, ActivityFeedService.SPECIES_COMMONNAME_DELETED, ActivityFeedService.SPECIES_HIERARCHY_DELETED] :
                mailSubject = notificationType;
                bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/addObservation"
                templateMap["message"] = Introspector.decapitalize(otherParams['info']);
                populateTemplate(obv, templateMap, userGroupWebaddress, feedInstance, request)
                toUsers.addAll(getParticipants(obv))
                break

                case NEW_SPECIES_PERMISSION : 
                mailSubject = notificationType
                    bodyView = "/emailtemplates/"+userLanguage.threeLetterCode+"/grantedPermission"
                    def user = otherParams['user'];
                templateMap.putAll(otherParams);
                toUsers.add(user)
                break


                default:
                log.debug "invalid notification type"
            }

            toUsers.eachWithIndex { toUser, index ->
                if(toUser) {
                    if(!toUser.enabled || toUser.accountLocked){
                        log.error "Account not enabled or locked - so skipping sending email to ${toUser}"
                        return
                    }
                    templateMap['username'] = toUser.name.capitalize();
                    templateMap['tousername'] = toUser.username;
                    if(request){
                        templateMap['userProfileUrl'] = generateLink("SUser", "show", ["id": toUser.id], request)
                    }
                    if(notificationType == DIGEST_MAIL){
                        templateMap['userID'] = toUser.id
                    }

                    log.debug "Sending email to ${toUser}"
                    try{
                        mailService.sendMail {
                            to toUser.email
                            if(index == 0 && (Environment.getCurrent().getName().equalsIgnoreCase("kk")) ) {
                                bcc grailsApplication.config.speciesPortal.app.notifiers_bcc.toArray()
                            }
                            from grailsApplication.config.grails.mail.default.from
                            //replyTo replyTo
                            subject mailSubject
                            if(bodyView) {
                                body (view:bodyView, model:templateMap)
                            }
                            else if(htmlContent) {
                                htmlContent = Utils.getPremailer(grailsApplication.config.grails.serverURL, htmlContent)
                                html htmlContent
                            } else if(bodyContent) {
                                if (bodyContent.contains('$')) {
                                    bodyContent = evaluate(bodyContent, templateMap)
                                }
                                html bodyContent
                            }
                        }
                    } catch(Exception e) {
                        log.error "Error sending message ${e.getMessage()} toUser : ${toUser} "
                        e.printStackTrace();
                    }
                }
            }

        } catch (e) {
            log.error "Error sending email $e.message"
            e.printStackTrace();
        }
    }

    private  void  populateTemplate(def obv, def templateMap, String userGroupWebaddress="", def feed=null, def request=null)  {
        if(obv?.getClass() == Observation)  {
            def values = obv?.fetchExportableValue();
            templateMap["obvOwner"] = values[ObvUtilService.AUTHOR_NAME];
            templateMap["obvOwnUrl"] = values[ObvUtilService.AUTHOR_URL];
            templateMap["obvSName"] =  values[ObvUtilService.SN]
            templateMap["obvCName"] =  values[ObvUtilService.CN]
            templateMap["obvPlace"] = values[ObvUtilService.LOCATION]
            templateMap["obvDate"] = values[ObvUtilService.OBSERVED_ON]
            templateMap["obvImage"] = obv.mainImage().thumbnailUrl()
            //get All the UserGroups an observation is part of
            templateMap["groups"] = obv.userGroups
        }
        if(obv.instanceOf(Species) && obv.id) {
            templateMap["obvSName"] = obv.taxonConcept.name  
            templateMap["obvCName"] = CommonNames.findByTaxonConceptAndLanguage(obv.taxonConcept, Language.findByThreeLetterCode('eng'))?.name    
            def imagePath = ''; 
            def speciesGroupIcon =  obv.fetchSpeciesGroup().icon(ImageType.ORIGINAL) 
            def mainImage = obv.mainImage(); 
            if(mainImage?.fileName == speciesGroupIcon.fileName) {  
                imagePath = mainImage.thumbnailUrl(null, '.png'); 
            } else 
                imagePath = mainImage?mainImage.thumbnailUrl():null; 

            templateMap["obvImage"] = imagePath.replaceAll(' ','%20'); 
            //get All the UserGroups a species is part of 
            templateMap["groups"] = obv.userGroups 
        }   

        if(feed) {
            templateMap['actor'] = feed.author;
            templateMap["actorProfileUrl"] = generateLink("SUser", "show", ["id": feed.author.id], request)
            templateMap["actorIconUrl"] = feed.author.profilePicture(ImageType.SMALL)
            templateMap["actorName"] = feed.author.name
            templateMap["activity"] = feed.contextInfo([webaddress:userGroupWebaddress])
            def domainObject = getDomainObject(feed.rootHolderType, feed.rootHolderId);
            templateMap['domainObjectTitle'] = getTitle(domainObject);
            templateMap['domainObjectType'] = feed.rootHolderType.split('\\.')[-1].toLowerCase()
            def isCommentThread = (feed.subRootHolderType == Comment.class.getCanonicalName() && feed.rootHolderType == UserGroup.class.getCanonicalName()) 
            if(isCommentThread) {
                templateMap['feedInstance'] = feed.fetchMainCommentFeed(); 
                templateMap["feedActorProfileUrl"] = generateLink("SUser", "show", ["id": feed.author.id], request)
                templateMap['commentInstance'] = getDomainObject(feed.subRootHolderType, feed.subRootHolderId)
                templateMap['group'] = domainObject;
            }
        }
    }

    private List getParticipants(observation) {
        List participants = [];
        if (Environment.getCurrent().getName().equalsIgnoreCase("kk")) {
            def result = getUserForEmail(observation) //Follow.getFollowers(observation)
            result.each { user ->
                if(user.sendNotification && !participants.contains(user)){
                    participants << user
                }
            }
        } else {
            participants << springSecurityService.currentUser;
        }
        return participants;
    }

    private List getUserForEmail(observation){
        if(!observation.instanceOf(UserGroup)){
            return Follow.getFollowers(observation)
        }else{
            //XXX for user only sending founders and current user as list members list is too large have to decide on this
            List userList = observation.getFounders(100, 0)
            userList.addAll(observation.getExperts(100, 0)) 
            def currUser = springSecurityService.currentUser
            if(!userList.contains(currUser)){
                userList << currUser
            }
            return userList
        }
    }

    private SUser getOwner(observation) {
        def author = null;
        if (Environment.getCurrent().getName().equalsIgnoreCase("kk") ) {
            if(observation.metaClass.hasProperty(observation, 'author') || observation.metaClass.hasProperty(observation, 'contributors')) {
                author = observation.author;
                if(!author.sendNotification) {
                    author = null;
                }
            }
        } else {
            author = springSecurityService.currentUser;
        }
        return author;
    } 

    private String getTitle(observation) {
        if(observation.metaClass.hasProperty(observation, 'title')) {
            return observation.title
        } else if(observation.metaClass.hasProperty(observation, 'name')) {
            return observation.name
        } else
            return null
    }

    def getResType(r) {
        def desc = ""
        switch(r.class.canonicalName){
            case Checklists.class.canonicalName:
            desc += "checklist"
            break
            default:
            desc += r.class.simpleName.toLowerCase()
            break
        }
        return desc
    }

    def getDomainObject(className, id){
        def retObj = null
        if(!className || className.trim() == ""){
            return retObj
        }

        id = id.toLong()
        switch (className) {
            case [ActivityFeedService.SPECIES_SYNONYMS, ActivityFeedService.SPECIES_COMMON_NAMES, ActivityFeedService.SPECIES_MAPS, ActivityFeedService.SPECIES_TAXON_RECORD_NAME]:
            retObj = [objectType:className, id:id]
            break
            default:
            retObj = grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
            break
        }
        return retObj
    }

    def getUserGroupHyperLink(userGroup){
        if(!userGroup){
            return ""
        }
        String sb = '<a href="' + userGroupBasedLink([controller:"userGroup", action:"show", mapping:"userGroup", userGroup:userGroup, userGroupWebaddress:userGroup?.webaddress]) + '">' + "<i>$userGroup.name</i>" + "</a>"
        return sb;
        //return sb.replaceAll('"|\'','\\\\"')
    }

    def getDescriptionForFeature(r, ug, isFeature)  {
        def desc = isFeature ? "Featured " : "Removed featured "
        String temp = getResType(r)
        desc+= temp
        if(ug == null) {
            return desc
        }
        desc +=  isFeature ? " to group" : " from group"
        return desc


    }

    //XXX for new checklists doamin object and controller name is not same as grails convention so using this method 
    // to resolve controller name
    static getTargetController(domainObj){
        if(domainObj.instanceOf(Checklists)){
            return "checklist"
        }else if(domainObj.instanceOf(SUser)){
            return "user"
        }else{
            return domainObj.class.getSimpleName().toLowerCase()
        }
    }

    /**
     * 
     * @param groupId
     * @return
     */
    Object getSpeciesGroupIds(groupId){
        def groupName = SpeciesGroup.read(groupId)?.name
        //if filter group is all
        if(!groupName || (groupName == grailsApplication.config.speciesPortal.group.ALL)){
            return null
        }
        return groupId
    }

    def benchmark(String blockName, Closure closure) {
        def start = System.currentTimeMillis()  
        closure.call()  
        def now = System.currentTimeMillis()  
        log.debug "%%%%%%%%%%%% execution time for ${blockName} took ${now- start} ms"  
    }  
}

