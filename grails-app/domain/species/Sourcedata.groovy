package species

import species.auth.SUser

abstract class Sourcedata {
	SUser uploader
	Date uploadTime
	
	def springSecurityService
	
    static constraints = {
		// uploader and uploadTime should not be null by default
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
