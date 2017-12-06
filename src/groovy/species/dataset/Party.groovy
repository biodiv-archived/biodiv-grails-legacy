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
    
    /*static mapping = {
        hasMany joinTable: [name: 'party_contributor',
        key: 'party_id',
        column: 'attribution',
        type: String]
    }*/
/*
    SUser getUploader() {
        return SUser.get(uploaderId);
    }

    void setUploader(SUser user) {
        this.uploaderId = user.id;
    }

    SUser getContributor() {
        return SUser.get(contributorId);
    }

    void setContributor(SUser user) {
        this.contributorId = user.id;
    }

    List<String> getAttributions() {
        return attributions;
    }

    void setAttributions(List<String> attributions) {
        def a = [];
        attributions.each {
            a << new Contributor(name:it);
        }
        this.attributions = a;
    }
*/


}


