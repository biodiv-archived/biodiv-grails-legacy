package species.participation

import species.auth.SUser;
import species.Resource;

class UsersResource {

    public enum UsersResourceStatus {
        NOT_USED("NOT_USED"),
        USED_IN_OBV("USED_IN_OBV"),
        USED_IN_SPECIES("USED_IN_SPECIES"),
        USED_IN_SPECIES_FIELD("USED_IN_SPECIES_FIELD"),
		USED_IN_CHECKLIST("USED_IN_CHECKLIST"),
        USED_IN_DOCUMENT("USED_IN_DOCUMENT")
		
        private String value;

        UsersResourceStatus(String value) {
            this.value = value;
        }

        String value() {
            return this.value;
        }

        static def toList() {
            return [ NOT_USED, USED_IN_OBV, USED_IN_SPECIES, USED_IN_SPECIES_FIELD, USED_IN_CHECKLIST, USED_IN_DOCUMENT]
        }

        public String toString() {
            return this.value();
        }
    }

    SUser user;
    Resource res;
    UsersResourceStatus status;

    static constraints = {
        user nullable:false
        res nullable:false
        status nullable:false
    }
}
