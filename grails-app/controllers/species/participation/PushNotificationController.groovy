package species.participation
import species.auth.SUser

import grails.converters.JSON
import species.auth.PushNotificationToken


class PushNotificationController {

	def messageSource;
	def utilsService;

	static allowedMethods = [ list:'GET', save: "POST",pushTokensave:"POST"]

    def save(){

    	def model = [];
	    def msg	 
    	if(request.method == 'POST') {	    		    	
	    	if(params.title && params.description && params.int('userId')){
	    		println "Passed 1"
	    		def pushNotification = new PushNotification();    	
	    		pushNotification.title = params.title
	    		pushNotification.description = params.description
	    		pushNotification.userId=params.int('userId');
	    		if(pushNotification.save(flush:true)){
	    			println "passed 2"
	    			msg ="Inserted success"	    			
	    			model = utilsService.getSuccessModel(msg, pushNotification);
	    		}else{
	    			println "passed 3"
	    			msg = messageSource.getMessage("Object Not Saved", null)
	    			model = utilsService.getErrorModel(msg, null);
	    		}
	    	}else{
	    		println "passed 4"
	    		msg = "Required Field Missing"
           		model = utilsService.getErrorModel(msg, null);
	    	}
    	}else{
    		msg = "Method Not Allowed"
            model = utilsService.getErrorModel(msg, null);
    	}
    	println "================="+model;
    	render model as JSON
    }

    def list(){
    	
    	def max = Math.min(params.max ? params.int('max') : 12, 100)
		def offset = params.offset ? params.int('offset') : 0 
		def pushNotificationList = PushNotification.list(order:"desc",sort:'id',offset:offset, max:max); 

    	render pushNotificationList as JSON;
    }


    def pushTokensave(){

    	def model = [];
	    def msg	 
    	if(request.method == 'POST') {	    		    	
	    	if(params.deviceToken){
	    		println "Passed 1"
	    		def pushNotificationTokenIsExist = PushNotificationToken.findByDeviceToken(params.deviceToken);
	    		if(!pushNotificationTokenIsExist){
		    		def pushNotificationToken = new PushNotificationToken();    	
		    		pushNotificationToken.deviceToken = params.deviceToken
		    		if(pushNotificationToken.save(flush:true)){
		    			println "passed 2"
		    			msg ="Inserted success"	    			
		    			model = utilsService.getSuccessModel(msg, pushNotificationToken);
		    		}else{
		    			println "passed 3"
		    			msg = messageSource.getMessage("Object Not Saved", null)
		    			model = utilsService.getErrorModel(msg, null);
		    		}
		    	}else{
		    		println "passed 2"
		    		msg ="Getting from table"	    			
		    		model = utilsService.getSuccessModel(msg, pushNotificationTokenIsExist);
		    	}
	    	}else{
	    		println "passed 4"
	    		msg = "Required Field Missing"
           		model = utilsService.getErrorModel(msg, null);
	    	}
    	}else{
    		msg = "Method Not Allowed"
            model = utilsService.getErrorModel(msg, null);
    	}
    	println "================="+model;
    	render model as JSON

    }

    def pushTokenlist(){
    	def query = "select deviceToken from PushNotificationToken"
    	def pushNotificationTokenList = PushNotificationToken.executeQuery(query)
    	render pushNotificationTokenList as JSON
    }

    def getappVersion(){
    	def version,msg,model;
    	if(grailsApplication.config.speciesPortal.mobileAppVersion){
    		version = grailsApplication.config.speciesPortal.mobileAppVersion;
    		msg ="Getting success"	    			
	    	model = utilsService.getSuccessModel(msg, version);
    	}else{
    		msg = messageSource.getMessage("Object Not Found", null)
	    	model = utilsService.getErrorModel(msg, null);
    	}
    	render  model as JSON;
    }
}

