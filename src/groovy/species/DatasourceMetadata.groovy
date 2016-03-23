package species

import content.eml.Contact;
import species.auth.SUser;

abstract class DatasourceMetadata {

    //https://knb.ecoinformatics.org/#external//emlparser/docs/eml-2.1.1/./eml-dataset.html
    //http://ibif.gov.in:8080/ipt/eml.do?r=spider_namdapha
    String title;
    String description;

    SUser author;
    Date createdOn = new Date();
    Date lastRevised = new Date();


    List<Contact> contacts;
    //represents from where this datatable was parsed DWCA/EML/XML
    //List<Endpoint> endpoints;

    static constraints = {
        title nullable:false, blank:false;
        contacts nullable:true;
        description nullable:false, blank:false;
    }

    static mapping = {
        description type:'text'
        tablePerHierarchy false
        //        tablePerSubClass true
    }

    def beforeInsert(){
    }

    def beforeUpdate(){
    }

    String title() {
        return this.title;
    }

    String notes(Language userLanguage = null) {
        return this.description?:'';
    }

    String summary(Language userLanguage = null) {
        return this.notes?:'';
    }

    def getOwner() {
        return this.author;
    }


}
