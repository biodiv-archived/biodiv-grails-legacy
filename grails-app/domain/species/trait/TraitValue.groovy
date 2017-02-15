package species.trait;

import species.TaxonomyDefinition;
import species.Field;
import species.UtilsService;
import species.Resource;
import species.Resource.ResourceType;
import species.utils.ImageType;
import species.utils.ImageUtils;
import species.Language;
import org.springframework.context.i18n.LocaleContextHolder;

class TraitValue {

    Trait trait;
    String value;
    String description;
    String icon;
    String source;
//    TaxonomyDefinition taxon;
	
    def grailsApplication;
    boolean isDeleted = false;
    
    static constraints = {
        trait nullable:false, blank:false, unique:['value']
        value nullable:false
		description nullable:true
        icon nullable:true
        source nullable:false
        //taxon nullable:false
    }

    static mapping = {
        description type:"text"
        id  generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence_name: "trait_value_id_seq"] 
    }
    static hasMany = [traitValueTranslations:TraitValueTranslation]

	Resource icon(ImageType type) {
		boolean iconPresent = (new File(grailsApplication.config.speciesPortal.traits.rootDir.toString()+'/'+this.icon)).exists()
		if(!iconPresent || !this.icon) {
            //log.warn "Couldn't find logo at "+grailsApplication.config.speciesPortal.traits.rootDir.toString()+'/'+this.icon
			return new Resource(fileName:grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg", type:ResourceType.ICON, title:"");
		}
		return new Resource(fileName:grailsApplication.config.speciesPortal.traits.serverURL+'/'+this.icon, type:ResourceType.ICON, title:this.value);
	}

	Resource mainImage() {
		return icon(ImageType.NORMAL);
	}

    String thumbnailUrl(String newBaseUrl=null, String defaultFileType=null, ImageType imageType = ImageType.NORMAL) {
        String thumbnailUrl = '',isFilePresent='';
        def basePath = '';
        if(!this.icon){
            thumbnailUrl = grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg";
            return thumbnailUrl;
        }
        newBaseUrl = (newBaseUrl)?:(basePath?:grailsApplication.config.speciesPortal.traits.serverURL);
        int lastIndex = this.icon.lastIndexOf('.');
        def originalFileExt = (this.icon && lastIndex != -1) ? this.icon.substring(lastIndex, this.icon.length()):null;

        if(!defaultFileType && originalFileExt) defaultFileType = originalFileExt;
        
        isFilePresent = ImageUtils.getFileName(this.icon, imageType, defaultFileType)
        boolean iconPresent = (new File(grailsApplication.config.speciesPortal.traits.rootDir.toString()+'/'+isFilePresent)).exists()
        if(!iconPresent) {
            //log.warn "Couldn't find logo at "+grailsApplication.config.speciesPortal.traits.rootDir.toString()+'/'+this.icon
            thumbnailUrl = grailsApplication.config.speciesPortal.resources.serverURL.toString()+"/no-image.jpg";
        }else{
            thumbnailUrl = newBaseUrl + "/" + isFilePresent;
        }
        return thumbnailUrl;
    }

    
}
