package species.dataset;

import species.DataSourceMetadata;
import species.DataObject;
import species.Language;
import content.eml.GeospatialCoverage;
import content.eml.TemporalCoverage;
import content.eml.TaxonomicCoverage;
import species.dataset.DataSource;
import species.ResponsibleParty;
import content.eml.Contact;
import species.License;
import species.Metadata;

class DataSet extends DataSourceMetadata {

    public enum DataSetType {
        SPECIES("Species"),
        OBSERVATIONS("Observations"),
        DOCUMENTS("Documents")

        private String value;

        DataSetType(String value) {
            this.value = value;
        }

        static list() {
            [
            SPECIES,
            OBSERVATIONS,
            DOCUMENTS
            ]
        }

        String value() {
            return this.value;
        }

        String toString() {
            return this.value();
        }
    }

    DataSetType type
    DataSource publisher;
    List<DataObject> dataObjects;

    List<String> alternateIdentifiers;
    Contact creator;
    List<Contact> metadataProvider;
    List<ResponsibleParty> associatedParty;
    
    Language language;
    License license;
    List<String> keywords;

    String additionalInfo;
    String rights;
    
    String purpose;
 
    String geographicDescription;
    List<GeospatialCoverage> geographicCoverages;
    List<TemporalCoverage> temporalCoverages;

    List<TaxonomicCoverage> taxonomicCoverages;
    
    //List<Method> method;

    String externalId
    String externalUrl
    String viaId
    String viaCode

   
    // EML specific properties which are not persisted on the dataset table!
    //List<Citation> bibliographicCitations = Lists.newArrayList();
       //Project project;
    //SamplingDescription samplingDescription;
    //Set<Country> countryCoverage = Sets.newHashSet();
    //List<Collection> collections = Lists.newArrayList();
    //List<DataDescription> dataDescriptions = Lists.newArrayList();
    Language dataLanguage;

    static constraints = {
        importFrom Metadata, include : ['language', 'license', 'externalId', 'externalUrl', 'viaId', 'viaCode'];
    }
	
	static mapping = {
	}

    //static hasMany = [dataObjects:DataObject];
	static belongsTo = [DataSource]
 
}
