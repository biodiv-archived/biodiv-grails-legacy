package species

import species.utils.Utils;

class SecurityFilters {

	def grailsApplication;
	
    def filters = {
        all(controller:'*', action:'*') {
            before = {
				grailsApplication.config.speciesPortal.domain = Utils.getDomain(request);
				println "Setting domain to : "+grailsApplication.config.speciesPortal.domain;
				
				def appName = grailsApplication.metadata['app.name']
				grailsApplication.config.grails.domainServerURL = "http://${grailsApplication.config.speciesPortal.domain}/${appName}"
				println "Setting server url to : "+grailsApplication.config.grails.domainServerURL
//				println params;
//				request.cookies.each{println it.name+" : "+it.value}
//				def enames = request.getHeaderNames();
//				   while (enames.hasMoreElements()) {
//					  String name = (String) enames.nextElement();
//					  String value = request.getHeader(name);
//					  println name+":"+value;
//				   }
				
            }
            after = {
				
            }
            afterView = {
                
            }
        }
    }
    
}
