package species.dataset;

import species.DatasourceMetadata;
import species.DataObject;
import species.Language;
import content.eml.GeospatialCoverage;
import content.eml.TemporalCoverage;
import content.eml.TaxonomicCoverage;
import species.dataset.Datasource;
import species.ResponsibleParty;
import content.eml.Contact;
import content.eml.UFile;
import species.License;
import species.Metadata;
import org.grails.taggable.Taggable;
import org.grails.rateable.*
import species.participation.Observation;
import grails.converters.JSON;
import grails.converters.XML;
import species.groups.UserGroup;

class Dataset extends DatasourceMetadata  implements Taggable, Rateable,  Serializable{

    public enum DatasetType {
        SPECIES("Species"),
        OBSERVATIONS("Observations"),
        DOCUMENTS("Documents")

        private String value;

        DatasetType(String value) {
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

    DatasetType type
    Datasource datasource;
    Date publicationDate = new Date();

    List<String> alternateIdentifiers;
    Contact originalAuthor;
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

	UFile uFile   //covers physical file formats
    
    String attribution;

    boolean isDeleted = false;   

    static hasMany = [userGroups:UserGroup];
	static belongsTo = [UserGroup]


    // EML specific properties which are not persisted on the dataset table!
    //List<Citation> bibliographicCitations = Lists.newArrayList();
       //Project project;
    //SamplingDescription samplingDescription;
    //Set<Country> countryCoverage = Sets.newHashSet();
    //List<Collection> collections = Lists.newArrayList();
    //List<DataDescription> dataDescriptions = Lists.newArrayList();
    Language dataLanguage;

    static constraints = {
        ///HACK to comment as dataset is no longer used importFrom Metadata, include : ['language', 'license', 'externalId', 'externalUrl', 'viaId', 'viaCode'];
        //HACK to comment as dataset is no longer used importFrom DatasourceMetadata, include : ['title', 'description']
 
        datasource nullable:false;
        originalAuthor nullable:true;

        alternateIdentifiers nullable:true;

        metadataProvider nullable:true;
        associatedParty nullable:true;

        keywords nullable:true;

        additionalInfo nullable:true;
        rights nullable:true;
        purpose nullable:true;

        geographicDescription nullable:true;
        geographicCoverages nullable:true;
        temporalCoverages nullable:true;
        taxonomicCoverages nullable:true;
        uFile nullable:true;
        publicationDate nullable:false;
        attribution nullable:false, blank:false;
    }
	
	static mapping = {
        rights type:'text'
        additionalInfo type:'text'
        purpose type:'text'
        id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "dataset_id_seq"] 
	}

//	static belongsTo = [DataSource]

    def fetchColumnNames() {
        def obv = Observation.findByDataset(this);
        def annots = JSON.parse(obv.checklistAnnotations);
        return annots.names();
    }

    String getSciNameColumn() {
        return "scientificName";
    }
}
