package content.eml

import species.ResponsibleParty;
import content.eml.Contact;

class Organization extends ResponsibleParty {

    String name;
    List<Contact> contactPerson;    
    URI logoUrl;

}
