package species

import species.auth.SUser

abstract class Sourcedata {
	SUser uploader
	Date uploadTime
	
	def springSecurityService
	
    static constraints = {
		//XXX its null here. but setting appropriate value in before insert
		uploader nullable:true;
		uploadTime nullable:true;
    }

	static mapping = {
        tablePerHierarchy false
//        tablePerSubClass true
    }

	def beforeInsert(){
		updateSoruce()
	}
	
	def beforeUpdate(){
		updateSoruce()
	}

	private updateSoruce(){
		uploadTime = new Date()
		uploader = springSecurityService.currentUser ?: SUser.findByUsername('admin')
	}
}
