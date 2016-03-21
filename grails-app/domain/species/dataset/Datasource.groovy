package species.dataset;

import species.DatasourceMetadata;
import content.eml.Contact;
import content.eml.Organization;
import species.Language;
import species.Resource;
import species.Resource.ResourceType;
import species.utils.ImageType;
import species.utils.ImageUtils;
import org.grails.taggable.Taggable;
import org.grails.rateable.*
import species.participation.Observation;

class Datasource extends DatasourceMetadata implements Taggable, Rateable {

	String website;
    String icon;
    boolean isDeleted = false;
    Language language;

    def grailsApplication;

    static constraints = {
        importFrom DatasourceMetadata
		icon nullable:false
    }

    static mapping = {
        id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "datasource_id_seq"] 
    }

    static hasMany = [datasets : Dataset];

    Resource icon(ImageType type) {
		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.datasource.rootDir.toString()+this.icon)).exists()
		if(!iconPresent) {
            log.warn "Couldn't find logo at "+grailsApplication.config.speciesPortal.datasource.rootDir.toString()+this.icon
			return new Resource(fileName:grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg", type:ResourceType.ICON, title:"");
		}
		return new Resource(fileName:grailsApplication.config.speciesPortal.datasource.serverURL+this.icon, type:ResourceType.ICON, title:this.title);
	}

	Resource mainImage() {
		return icon(ImageType.NORMAL);
	}

    String title() {
        return this.title;
    }

    String notes(Language userLangauge = null) {
        return this.description;
    }

    String summary(Language userLangauge = null) {
        return this.description;
    }

    long noOfObservations() {
        def query = "select count(*) from Observation obv where obv.dataset.datasource.id = :datasourceId and obv.isDeleted = false "
        return Observation.executeQuery(query, ['datasourceId':this.id])[0]
    }
}
