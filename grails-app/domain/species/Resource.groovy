package species

import species.participation.Observation;
import species.participation.Checklists;
import species.auth.SUser;

import species.utils.ImageUtils;
import species.utils.ImageType;
import species.utils.Utils;
import org.grails.rateable.*
import content.eml.Document;
import species.participation.UsersResource;
import grails.converters.JSON

class Resource extends Sourcedata implements Rateable {
	def utilsService
	
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

		static def toList() {
			return [
				ICON,
				IMAGE,
				AUDIO,
				VIDEO
            ]
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
        DOCUMENT("DOCUMENT"),
        SPECIES_FIELD("SPECIES_FIELD"),
		CHECKLIST("CHECKLIST"),
        USER("USER"),
		DISCUSSION("DISCUSSION")
		
        private String value;

        ResourceContext(String value) {
            this.value = value;
        }

        String value() {
            return this.value;
        }

        static def toList() {
            return [ OBSERVATION,SPECIES,DOCUMENT, SPECIES_FIELD, CHECKLIST, USER, DISCUSSION]
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
    Language language;
    String annotations;
    String accessRights;
    License license;

	static hasMany = [contributors:Contributor, attributors:Contributor, speciesFields:SpeciesField, observation:Observation];
	static belongsTo = [SpeciesField, Observation];
	
	static mapping = {
		description type:'text';
		annotations type:'text';
		sort "id"
	}
	
    static constraints = {
    	language(nullable:false);
		fileName(blank:false);
		url(nullable:true);
		description(nullable:true);
		mimeType(nullable:true);
		//licenses  validator : { val, obj -> val && val.size() > 0 }
        rating(nullable:false, min:0, max:5);
        context(nullable:true);
        annotations(nullable:true);
        accessRights(nullable:true);
    }
	
	static transients = ['baseUrl']
	
	String thumbnailUrl(String newBaseUrl=null, String defaultFileType=null, ImageType imageType = ImageType.NORMAL) {
        String thumbnailUrl = '';
        def basePath = '';
        if(this?.context?.value() == Resource.ResourceContext.OBSERVATION.toString()) {
            basePath = grailsApplication.config.speciesPortal.observations.serverURL
        } else if(this?.context?.value() == Resource.ResourceContext.SPECIES.toString() || this?.context?.value() == Resource.ResourceContext.SPECIES_FIELD.toString()) {
            basePath = grailsApplication.config.speciesPortal.resources.serverURL
        } else if(this?.context?.value() == Resource.ResourceContext.DOCUMENT.toString() || this?.context?.value() == Resource.ResourceContext.DISCUSSION.toString()) {
            basePath = grailsApplication.config.speciesPortal.content.serverURL
        } else {
            basePath = this.baseUrl;
        }

		newBaseUrl = (newBaseUrl)?:(basePath?:grailsApplication.config.speciesPortal.observations.serverURL)

		switch(type) {
			case  ResourceType.IMAGE :
                if(url && url.endsWith('no-image.jpg')) {
                    return ;
                } else if(url && fileName.length() == 1) {
                    thumbnailUrl = url;
                } else {
				    thumbnailUrl = newBaseUrl + "/" + ImageUtils.getFileName(this.fileName, imageType, defaultFileType)
                }
				break;
			case ResourceType.VIDEO :				
                if( imageType == ImageType.ORIGINAL) {
                    return this.url
                }
				String videoId = Utils.getYouTubeVideoId(this.url);
				thumbnailUrl = "http://img.youtube.com/vi/${videoId}/default.jpg"
				break;
			case ResourceType.AUDIO :								
                if( imageType == ImageType.ORIGINAL) {
                    return grailsApplication.config.grails.serverURL+this.fileName
                }
				thumbnailUrl = grailsApplication.config.grails.serverURL+"/assets/all/audioicon.png"
				break;	
            case ResourceType.ICON :
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
				if(this.observation != null) { 
                    if(url) return url;
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
	
	void saveResourceContext(objInstance){
		//saving only if context is null earlier
		//if(this.context) return;
		
		switch(objInstance.class.name){
			case Species.class.name:
				this.context = ResourceContext.SPECIES
				break
			case Observation.class.name:
				this.context = ResourceContext.OBSERVATION
				break
			case Document.class.name:
				this.context = ResourceContext.DOCUMENT
				break
			case SpeciesField.class.name:
				this.context = ResourceContext.SPECIES_FIELD
				break
			case Checklists.class.name:
				this.context = ResourceContext.CHECKLIST
				break
            case SUser.class.name:
                this.context = ResourceContext.USER
                break

			default:
				break
		}

		if(!this.save(flush:true)){
			this.errors.allErrors.each { log.error it }
		} else {
            //utilsService.evictInCache('resources', this.context.toString().toLowerCase()+"-"+this.id);
        }
		
	}

    def fetchAnnotations() {
        def m = [:];
        if(this.annotations) {
            JSON.parse(this.annotations).each {
                if(it.value) {
                    m[it.key] = ['value':it.value]
                }
            }
        }
        return m
            
    }
}
