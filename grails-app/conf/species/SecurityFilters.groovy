package species

import grails.util.Environment;

import species.utils.Utils;
import species.groups.UserGroup;

import grails.converters.JSON;
import java.util.concurrent.atomic.AtomicLong

class SecurityFilters {

    def grailsApplication;
    def springSecurityService;

    private static final AtomicLong REQUEST_NUMBER_COUNTER = new AtomicLong()
    private static final String START_TIME_ATTRIBUTE = 'Controller__START_TIME__'
    private static final String REQUEST_NUMBER_ATTRIBUTE = 'Controller__REQUEST_NUMBER__'

    def filters = {
        all(controller:'*', action:'*') {

            before = {
                //log.info "^Request ${request.getRequestURL()} with params: ${params}"

                grailsApplication.config.speciesPortal.domain = Utils.getDomain(request);
                //println "Setting domain to : "+grailsApplication.config.speciesPortal.domain;

                def appName = grailsApplication.metadata['app.name']
                /*                if(params.ajax_login_error == "1") {
                                  render ([status:401, error:'Please login to continue'] as JSON)
                                  return;
                } 
                 */ 
                //				println params;
                //				request.cookies.each{println it.name+" : "+it.value}
                //				def enames = request.getHeaderNames();
                //				   while (enames.hasMoreElements()) {
                //					  String name = (String) enames.nextElement();
                //					  String value = request.getHeader(name);
                //					  println name+":"+value;
                //				   }

            }

            after = { model ->
                //setting user group and permission for view
                if(model){
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
                        //log.info msg
                    }
            }

        }
    }

}
