package species.participation

import species.auth.SUser
import species.participation.Observation

class Property {
    
    SUser author
    long objectId
    String objectType
    Date createdOn = new Date();
	String notes;

    static belongsTo = [author:SUser];
 
    static mapping = {
        tablePerHierarchy true
    }

    static constraints = {
        notes nullable:true, blank: true
        notes (size:0..400)

    }
}
