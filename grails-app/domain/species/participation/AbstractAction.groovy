package species.participation

import species.auth.SUser
import species.participation.Observation

abstract class AbstractAction {
    
    SUser author
    long objectId
    String objectType
    Date createdOn = new Date();

    static belongsTo = [author:SUser];
 
    static mapping = {
        version : false;
        tablePerHierarchy false
    }

    static constraints = {
        createdOn nullable:true
    }
}
