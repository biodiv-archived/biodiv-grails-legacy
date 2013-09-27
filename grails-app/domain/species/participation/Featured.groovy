package species.participation

import species.groups.UserGroup
import species.auth.SUser
import species.participation.Observation

class Featured {
    
    SUser author
    long objectId
    String objectType
    Date createdOn = new Date();
	String notes;
    UserGroup userGroup;
    
    static belongsTo = [author:SUser];

    static constraints = {
        author(unique: ['objectId', 'objectType', 'userGroup'])
        notes nullable:true, blank: true
        notes (size:0..400)
    }

    static void deleteFeatureOnObv(object) {
        def f = Featured.findAllWhere(objectType: object.getCanonicalName(), objectId: object.id)
        f.delete(flush: true);
        if(!f.delete(flush:true)){
		        println "Error deleting the featured object when Observation got deleted"
                f.errors.allErrors.each { println it }
		        return null
            }

    }
}
