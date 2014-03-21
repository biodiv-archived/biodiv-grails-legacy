package species

import species.auth.SUser

abstract class NamesSorucedata extends Sourcedata {

	static hasMany = [contributors: SUser]
	
    static constraints = {
    
	}
	
	def beforeInsert(){
		super.beforeInsert()
		if(!contributors){
			this.addToContributors(uploader)
		}
	}
	
	def beforeUpdate(){
		//overwriting base class method
	}
	
	def updateContributors(List<SUser> users){
		if(!users) return
		
		users.each { u ->
			this.addToContributors(u)
		}
		
		if(!save(flush:true)){
			this.errors.allErrors.each { log.error it }
		}
	}

    boolean isContributor(SUser user) {
        if(!user) user = springSecurityService.currentUser;
        boolean success = false;
        this.contributors.each { c->
           if(c.email == springSecurityService.currentUser.email) {
               success = true;
               return
           }
       }
        return success;
    }
}
