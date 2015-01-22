package species

import grails.util.Environment;

import species.utils.Utils;
import species.groups.UserGroup;
import species.auth.AppKey;

import grails.converters.JSON;
import grails.converters.XML;
import java.util.concurrent.atomic.AtomicLong
import org.springframework.context.i18n.LocaleContextHolder as LCH
import org.springframework.web.servlet.support.RequestContextUtils as RCU; 
import javax.servlet.http.HttpServletResponse;
import org.codehaus.groovy.grails.commons.ApplicationHolder
import static org.springframework.http.HttpStatus.*;

class SecurityFilters {

    def grailsApplication;
    def springSecurityService;
    def utilsService;

    private static final AtomicLong REQUEST_NUMBER_COUNTER = new AtomicLong()
    private static final String START_TIME_ATTRIBUTE = 'Controller__START_TIME__'
    private static final String REQUEST_NUMBER_ATTRIBUTE = 'Controller__REQUEST_NUMBER__'

    def filters = {
        all(controller:'*', action:'*') {

            before = {
                log.info "^Request ${request.getRequestURL()} with params: ${params}"

                grailsApplication.config.speciesPortal.domain = Utils.getDomain(request);
                //println "Setting domain to : "+grailsApplication.config.speciesPortal.domain;
                def appectedLanguage = false;    
                for ( localeLanguage in grailsApplication.config.speciesPortal.localeLanguages ) {                    
                    if(localeLanguage.twoletter.equals(LCH.getLocale().toString())){
                        appectedLanguage = true;
                    }
                }

                if(grailsApplication.config.speciesPortal.hideLanguages || !appectedLanguage){
                    Locale locale = new Locale("en");
                    LCH.setLocale(locale);                        
                    def localeResolver = RCU.getLocaleResolver(request); // returns a SessionLocaleResolver                   
                    localeResolver.setLocale(request, response, locale); // set the locale, bound to the HttpSession                    
                }    

                def appName = grailsApplication.metadata['app.name']
                
                //HACK
                params.action = actionName;

                //verify appkey if present
                String appKeyHeader = request.getHeader('X-AppKey');
                println "------------------------------------------------)"
                println appKeyHeader
                println request.requestURI
                println request.forwardURI
                println "------------------------------------------------)"
                if(request.forwardURI.startsWith("/${appName}/api/")) {
                    if (!actionName) actionName = 'index'
                        println "MATCHED--------${params.controller}------${params.action}---${controllerName}--${actionName}------------)"
                        for( cc in ApplicationHolder.application.controllerClasses) {
                            for (m in cc.clazz.methods) {
                                def ann = m.getAnnotation(grails.plugin.springsecurity.annotation.Secured)
                                if (ann) {
                                    String con = cc.logicalPropertyName
                                    String act = m.name
                                    if(controllerName.equalsIgnoreCase(con) && actionName.equalsIgnoreCase(act)) {
                                        boolean isUnauthorized = false;
                                        if(appKeyHeader) {
                                            println "Verifying app key ${appKeyHeader}"
                                            AppKey appKey = AppKey.findByKey(appKeyHeader);
                                            if(!appKey) println "App key not found";
                                            if(!appKey.email.equalsIgnoreCase(springSecurityService.currentUser?.email))
                                                println "Appkey is not of the logged in user ${springSecurityService.currentUser.email}" 
                                            if(appKey && appKey.email.equalsIgnoreCase(springSecurityService.currentUser?.email)) {
                                                println "Found valid appkey. Continuing"
                                            } else isUnauthorized = true
                                        } else {
                                            isUnauthorized = true;
                                        }
                                        if(isUnauthorized) {
                                            //sending 401 status
                                            def model = utilsService.getErrorModel('Invalid app key in the header', null, UNAUTHORIZED.value())
                                            render model as JSON;
                                            return false;
                                        }

                                    }
                                }
                            }
                        }
                }
            }

            after = { model ->
                //setting user group and permission for view
                if(model!=null){
                    def userGroupInstance = model.userGroupInstance
                    def userGroup = model.userGroup
                    if(!userGroupInstance) {
                        if(userGroup) {
                            userGroupInstance = userGroup
                        } else if(params.userGroup) {
                            if(params.userGroup instanceof UserGroup) {
                                userGroupInstance = params.userGroup
                            } else {
                                userGroupInstance = UserGroup.get(params.long('userGroup'));
                            }
                        } else if(params.webaddress) {
                            userGroupInstance = UserGroup.findByWebaddress(params.webaddress);
                        } else if(params.userGroupWebaddress) {
                            userGroupInstance =UserGroup.findByWebaddress(params.userGroupWebaddress);
                        }
                    }

                    if(userGroupInstance && userGroupInstance.id){
                        model.userGroupInstance = userGroupInstance
                        def secTagLib = grailsApplication.mainContext.getBean('species.CustomSecurityAclTagLib');
                        model.canEditUserGroup = secTagLib.hasPermission(['permission':org.springframework.security.acls.domain.BasePermission.WRITE, 'object':userGroupInstance], 'permitted')
                        def user = springSecurityService.getCurrentUser();
                        model.isExpertOrFounder = (user && (userGroupInstance.isExpert(user) || userGroupInstance.isFounder(user)))
                    }
                    //passing locale Languages
                    model.localeLanguages = grailsApplication.config.speciesPortal.localeLanguages
                    model.hideLanguages = grailsApplication.config.speciesPortal.hideLanguages

                    // 
                    def appectedLanguage = false;    
                    for ( localeLanguage in grailsApplication.config.speciesPortal.localeLanguages ) {
                        if(localeLanguage.twoletter.equals(LCH.getLocale().toString())){
                            appectedLanguage = true;
                        }
                    }                   
                    if(grailsApplication.config.speciesPortal.hideLanguages || !appectedLanguage){
                        Locale locale = new Locale("en");
                        LCH.setLocale(locale)                        
                        def localeResolver = RCU.getLocaleResolver(request); // returns a SessionLocaleResolver                        
                        localeResolver.setLocale(request, response, locale); // set the locale, bound to the HttpSession                        
                    }    


                }
                log.debug "after rendering"
            }

            afterView = {
                log.debug "after view"
            }

        }

        logFilter(controller: '*', action: '*') {

            before = {
                long start = System.currentTimeMillis()
                long currentRequestNumber = REQUEST_NUMBER_COUNTER.incrementAndGet()

                request[START_TIME_ATTRIBUTE] = start
                request[REQUEST_NUMBER_ATTRIBUTE] = currentRequestNumber
                return true
            }

            after = { Map model ->

                long start = request[START_TIME_ATTRIBUTE]
                long end = System.currentTimeMillis()
                long currentRequestNumber = request[REQUEST_NUMBER_ATTRIBUTE]

                def msg = "^^Request #$currentRequestNumber : " +
                "'$request.forwardURI', " +
                " 'at ${new Date()}', 'Ajax: $request.xhr', 'controller: $controllerName', " +
                "'action: $actionName', 'params: ${params}', " +
                "from '$request.remoteHost ($request.remoteAddr)', '"+ request.getHeader('User-Agent')+"',"+
                "'${end - start}ms'"

                if (log.traceEnabled) {
                    log.trace msg + "; model: $model"
                }
                else {
                    log.info msg
                }
            }

            afterView = { Exception e ->

                long start = request[START_TIME_ATTRIBUTE]
                long end = System.currentTimeMillis()
                long requestNumber = request[REQUEST_NUMBER_ATTRIBUTE]

                def msg = "afterCompletion request #$requestNumber: " +
                "end ${new Date()}, total time ${end - start}ms"
                if (e) {
                    log.error "$msg \n\texception: $e.message", e
                }
                else {
                    log.info msg
                }
            }

        }
    }

}
