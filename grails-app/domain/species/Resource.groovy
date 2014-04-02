package species

import species.participation.Observation;
import species.utils.ImageUtils;
import species.utils.ImageType;
import species.utils.Utils;
import org.grails.rateable.*

class Resource extends Sourcedata implements Rateable {
	
	public enum ResourceType {
		ICON("Icon"),
		IMAGE("Image"),
		AUDIO("Audio"),
		VIDEO("Video");
		
		private String value;
		
		ResourceType(String value) {
			this.value = value;
		}
		
		public String value() {
			return this.value;
		}

        public iconClass() {
            switch(this) {
                case ICON : return 'icon-picture'
                case IMAGE : return 'icon-picture'
                case VIDEO : return 'icon-film'
                case AUDIO : return 'icon-music'
            }
        }
	}

    public enum ResourceContext {
        OBSERVATION("OBSERVATION"),
        SPECIES("SPECIES"),
        DOCUMENT("DOCUMENT");
        
        private String value;

        ResourceContext(String value) {
            this.value = value;
        }

        String value() {
            return this.value;
        }

        static def toList() {
            return [ OBSERVATION,SPECIES,DOCUMENT ]
        }

        public String toString() {
            return this.value();
        }
    }
	
	ResourceType type;
	String url; //TODO validate as url
	String fileName;
	String description;
	String mimeType; //TODO:validate
    int rating = 0;
	String baseUrl; 
	ResourceContext context;
    def grailsApplication

	static hasMany = [contributors:Contributor, attributors:Contributor, speciesFields:SpeciesField, observation:Observation, licenses:License];
	static belongsTo = [SpeciesField, Observation];
	
	static mapping = {
		description type:'text';
		sort "id"
	}
	
    static constraints = {
		fileName(blank:false);
		url(nullable:true);
		description(nullable:true);
		mimeType(nullable:true);
		licenses  validator : { val, obj -> val && val.size() > 0 }
        rating(nullable:false, min:0, max:5);
        context(nullable:true);
    }
	
	static transients = ['baseUrl']
	
	String thumbnailUrl(String newBaseUrl=null, String defaultFileType=null, ImageType imageType = ImageType.NORMAL) {
		String thumbnailUrl = '';
		newBaseUrl = newBaseUrl?:(this.baseUrl?:grailsApplication.config.speciesPortal.observations.serverURL)
		switch(type) {
			case  ResourceType.IMAGE :
				thumbnailUrl = newBaseUrl + "/" + ImageUtils.getFileName(this.fileName, imageType, defaultFileType)
				//thumbnailUrl = baseUrl + "/" + this.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix);
				break;
			case ResourceType.VIDEO :				
				String videoId = Utils.getYouTubeVideoId(this.url);
				thumbnailUrl = "http://img.youtube.com/vi/${videoId}/default.jpg"
				break;
			default :
				log.error "Not a valid type"
		}		 
		return thumbnailUrl;
	}
	
	String absPath(){
		//println "in abs path -----------------------------------------------------";
		String path = '';
		switch(this.type){
			case ResourceType.IMAGE :
				if(this.observation != null){
					path = grailsApplication.config.speciesPortal.observations.rootDir + "/" + this.fileName;
					return path;
				}
				else if(this.speciesFields != null){
					path = grailsApplication.config.speciesPortal.resources.rootDir + "/" + this.fileName;
					return path;
				}
			case ResourceType.VIDEO :
				log.error "Type Video : Not Supported for now"
			default :
				log.error "Not a valid type"
		}
	}
	
	String getUrl() {
		if(this.type == ResourceType.IMAGE) {
            if(Utils.isAbsoluteURL(this.fileName)) {
    			return this.fileName;
            } else {
		        //def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
                //return g.createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL, this.fileName);
            }
		}
		return this.@url;
	}
	
	void setUrl(String url) {
		if(!url) return;
		if(Utils.isURL(url)) {
			this.url = url;
		}
	}
}
