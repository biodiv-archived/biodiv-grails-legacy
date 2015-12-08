package species.dataset;

import species.DataSourceMetadata;
import content.eml.Contact;
import content.eml.Organization;

class DataSource extends DataSourceMetadata {

    Contact contributor;
    Organization organization;
    int numPublishedDatasets;
   
    static constraints = {
    }

    static mapping = {
    }

    static hasMany = [dataSets : DataSet];
}
