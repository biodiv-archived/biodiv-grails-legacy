package species.dataset;

import species.auth.SUser;
import species.Contributor;

class Party {

    Long uploaderId;
    
    Long contributorId;

    String attributions;

    static constraints = {
        uploaderId nullable:false;
        attributions nullable:true;
        /* NOT WORKINGcontributorIds (validator : {val, obj ->
           def retval = true
           if (!obj?.contributorIds?.size()) {
           retval = 'register.contributor.validator.error'
           }
           return retval
           })*/
    }
    
    static mapping = {
        attributions type:'text'
    }

    SUser getContributor() {
        return SUser.get(contributorId);
    }
}


