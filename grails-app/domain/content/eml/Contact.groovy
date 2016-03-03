package content.eml

import species.auth.SUser;
import species.ResponsibleParty;

class Contact extends ResponsibleParty {
    public enum ContactType {
        TECHNICAL_POINT_OF_CONTACT,
        ADMINISTRATIVE_POINT_OF_CONTACT,
        POINT_OF_CONTACT,
        ORIGINATOR,
        METADATA_AUTHOR,
        PRINCIPAL_INVESTIGATOR,
        AUTHOR,
        CONTENT_PROVIDER,
        CUSTODIAN_STEWARD,
        DISTRIBUTOR,
        EDITOR,
        OWNER,
        PROCESSOR,
        PUBLISHER,
        USER,
        PROGRAMMER,
        DATA_ADMINISTRATOR,
        SYSTEM_ADMINISTRATOR,
        HEAD_OF_DELEGATION,
        TEMPORARY_HEAD_OF_DELEGATION,
        ADDITIONAL_DELEGATE,
        TEMPORARY_DELEGATE,
        REGIONAL_NODE_REPRESENTATIVE,
        NODE_MANAGER,
        NODE_STAFF
    }
        
    ContactType role;
    String firstName;
    String lastName;
    String description;
    String deliveryPoint;
    String city;
    String state;
    String country = 'IN';
    String postalCode;

    List<String> phone;
    List<String> email;
    List<String> position; 

    String organization;

    SUser user;

}
