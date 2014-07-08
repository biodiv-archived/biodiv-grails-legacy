package species

import species.auth.SUser;

class SpeciesPermission {

    SUser author;
    TaxonomyDefinition taxonConcept;
    Date createdOn = new Date();

    public enum PermissionType {
        ROLE_CURATOR("ROLE_CURATOR"),
        ROLE_CONTRIBUTOR("ROLE_CONTRIBUTOR"),

        private String value;

        PermissionType(String value) {
            this.value = value;
        }

        String value() {
            return this.value;
        }

        static def toList() {
            return [ ROLE_CURATOR, ROLE_CONTRIBUTOR ]
        }

        public String toString() {
            return this.value();
        }

    }
    
    String permissionType;

    static constraints = {
        author(unique: ['taxonConcept', 'permissionType'])
        author nullable: false
        taxonConcept nullable: false
        permissionType nullable: false
    }


}
