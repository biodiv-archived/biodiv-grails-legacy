package species;

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import species.groups.UserGroup;
import species.Resource.ResourceType;
import species.participation.Featured;
import species.auth.SUser;
import species.participation.Checklists;
import species.sourcehandler.XMLConverter;
import species.participation.Observation;
import species.Species;

import org.apache.commons.logging.LogFactory;

class AbstractObjectService {
    
	def grailsApplication;
	def dataSource;
	def springSecurityService;
	def sessionFactory;

	private static final log = LogFactory.getLog(this);

    /**
    */
    protected static List createUrlList2(observations){
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		String iconBasePath = config.speciesPortal.observations.serverURL
		def urlList = createUrlList2(observations, iconBasePath)
//		urlList.each {
//			it.imageLink = it.imageLink.replaceFirst(/\.[a-zA-Z]{3,4}$/, config.speciesPortal.resources.images.thumbnail.suffix)
//		}
		return urlList
	}

	protected static List createUrlList2(observations, String iconBasePath){
		List urlList = []
		for(param in observations){
			def item = [:];
            def controller = getTargetController(param['observation']);
            item.id = param['observation'].id
			item.url = "/" + controller + "/show/" + param['observation'].id
			item.title = param['title']
            item.type = controller
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			Resource image = param['observation'].mainImage()
            def sGroup = param['observation'].fetchSpeciesGroup()
            if(sGroup)
			    item.sGroup = sGroup.name
            if(param['observation'].habitat)
			    item.habitat = param['observation'].habitat?.name
			if(image){
				if(image.type == ResourceType.IMAGE) {
                    boolean isChecklist = param['observation'].hasProperty("isChecklist")?param['observation'].isChecklist:false ;
					item.imageLink = image.thumbnailUrl(isChecklist ? null: iconBasePath, isChecklist ? '.png' :null)//thumbnailUrl(iconBasePath)
				} else if(image.type == ResourceType.VIDEO) {
					item.imageLink = image.thumbnailUrl()
				}
			}else{
				item.imageLink =  config.speciesPortal.resources.serverURL + "/" + "no-image.jpg"
			} 			
			if(param.inGroup) {
				item.inGroup = param.inGroup;
			} 
			
            item.notes = param['observation'].notes()
  			item.summary = param['observation'].summary();				
            
            if(param['featuredNotes']) {
                item.featuredNotes = param['featuredNotes']
            }
           
            if(param['featuredOn']) {
                item.featuredOn = param['featuredOn'].getTime();
            }
            def obj = param['observation'];
            if(obj.hasProperty('latitude') && obj.latitude) item.lat = obj.latitude
            if(obj.hasProperty('longitude') && obj.longitude) item.lng = obj.longitude
            if(obj.hasProperty('isChecklist') && obj.isChecklist) item.isChecklist = obj.isChecklist
            if(obj.hasProperty('fromDate') && obj.fromDate) item.observedOn = obj.fromDate.getTime();
			if(obj.hasProperty('geoPrivacy') && obj.geoPrivacy){
				item.geoPrivacy = obj.geoPrivacy
				item.geoPrivacyAdjust = obj.fetchGeoPrivacyAdjustment()
			}
			urlList << item;
		}
		return urlList
	}

    //XXX for new checklists doamin object and controller name is not same as grails convention so using this method 
	// to resolve controller name
	protected static getTargetController(domainObj){
		if(domainObj.instanceOf(Checklists)){
			return "checklist"
		}else if(domainObj.instanceOf(SUser)){
			return "user"
		}else{
			return domainObj.class.getSimpleName().toLowerCase()
		}
	}
	
    /**
    */
    protected String getIconBasePath(String controller) {
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
        String iconBasePath = '';
        switch(controller) {
            case "observation": 
		        iconBasePath = config.speciesPortal.observations.serverURL
                break;
            case "species":
                iconBasePath = config.speciesPortal.resources.serverURL
                break;
            case "content":
		        iconBasePath = config.speciesPortal.observations.serverURL
                break;
            default:
                log.warn "Invalid controller type for iconbasepath"
        }
        return iconBasePath;
    }


    /**
    */
    protected Map getFeaturedObject(Long ugId, int limit, long offset, String controller){
        log.debug "Getting featured objects for ${controller} in usergroup ${ugId?ugId:'IBP'}. limit:${limit} offset:${offset}"
        String type = ""
        String type1 = ""
        //TODO:change hardcoded string to class definitions
        if (controller == "observation") {
            type = "species.participation.Observation";
            type1 = "species.participation.Checklists";
        }
        else if (controller == "species") {
            type = "species.Species";
        }
        else if (controller == "document") {
            type = "content.eml.Document";
        }
        else {    
        }

        def featured = []
        def count = 0;
        def queryParams = [:];
        def countQuery,query;
        if(type) {
            queryParams["type"] = type
            queryParams["type1"] = type1

            countQuery = "select count(*) from Featured feat where (feat.objectType = :type or feat.objectType = :type1) "
        
            query = "from Featured feat where (feat.objectType = :type or feat.objectType = :type1) "
        } else {
            countQuery = "select count(*) from Featured feat "
            query = "from Featured feat "
        }

        if(ugId) {
            queryParams["ugId"] = ugId
            countQuery += ' and feat.userGroup.id = :ugId'
            query +=  ' and feat.userGroup.id = :ugId'
        }
        
        log.debug "CountQuery:"+ countQuery + " params: "+queryParams
        count = Featured.executeQuery(countQuery, queryParams)

        queryParams["max"] = limit
        queryParams["offset"] = offset

        def orderByClause = " order by feat.createdOn desc"
        query += orderByClause

        log.debug "FeaturedQuery:"+ query + " params: "+queryParams
        featured = Featured.executeQuery(query, queryParams);
        def observations = [:]
        featured.each {
            def observation = activityFeedService.getDomainObject(it.objectType,it.objectId)
            def featuredNotes = [];
            if(observations.containsKey(observation)) {
                featuredNotes = observations.get(observation);
            } else {
                observations.put(observation, featuredNotes);
            }
            //JSON marsheller is registered in Bootstrap
            featuredNotes << it
        }
		
        def result = []
        def i = 0;
        observations.each {key,value ->
            result.add([ 'observation':key, 'title': key.fetchSpeciesCall(), 'featuredNotes':value, 'controller':getTargetController(key)]);
        }
		
        return['observations':result,'count':count[0], 'controller':controller?:'abstractObject']
                		
    }


     /**
     * 
     */
    private def createResourcesXML(params) {
        NodeBuilder builder = NodeBuilder.newInstance();
        XMLConverter converter = new XMLConverter();
        def resources = builder.createNode("resources");
        Node images = new Node(resources, "images");
        Node videos = new Node(resources, "videos");
        
        String uploadDir = ""
        if( params.resourceListType == "ofSpecies" ){
            uploadDir = grailsApplication.config.speciesPortal.resources.rootDir
        }
        else{
            uploadDir =  grailsApplication.config.speciesPortal.observations.rootDir;
        }
        List files = [];
        List titles = [];
        List licenses = [];
        List type = [];
        List url = []
        List source = [];
        List ratings = [];
        List contributor = [];
        //List resContext = [];
        params.each { key, val ->
            int index = -1;
            if(key.startsWith('file_')) {
                index = Integer.parseInt(key.substring(key.lastIndexOf('_')+1));

            }
            if(index != -1) {
                files.add(val);
                titles.add(params.get('title_'+index));
                licenses.add(params.get('license_'+index));
                type.add(params.get('type_'+index));
                url.add(params.get('url_'+index));
                source.add(params.get('source_'+index));
                ratings.add(params.get('rating_'+index));
                //resContext.add(params.get('resContext_'+index));
                if( params.speciesId != null ){
                    contributor.add(params.get('contributor_'+index));
                }
            }
        }
        files.eachWithIndex { file, key ->
            Node image;
            if(file) {
                if(type.getAt(key).equalsIgnoreCase(ResourceType.IMAGE.value())) {
                    image = new Node(images, "image");
                    File f = new File(uploadDir, file);
                    new Node(image, "fileName", f.absolutePath);
                } else if(type.getAt(key).equalsIgnoreCase(ResourceType.VIDEO.value())) {
                    image = new Node(videos, "video");
                    new Node(image, "fileName", file);
                    new Node(image, "source", url.getAt(key));
                }				
                new Node(image, "caption", titles.getAt(key));
                new Node(image, "license", licenses.getAt(key));
                new Node(image, "rating", ratings.getAt(key));
                new Node(image, "user", springSecurityService.currentUser?.id);
                //new Node(image, "resContext", resContext.getAt(key));
                if( params.resourceListType == "ofObv" ){
                    new Node(image, "contributor", params.author.username); 
                }
                else{
                    new Node(image, "contributor", contributor.getAt(key));
                    if(type.getAt(key).equalsIgnoreCase(ResourceType.IMAGE.value())) {
                        new Node(image, "source", source.getAt(key));
                    }
                }
            } else {
                log.warn("No reference key for image : "+key);
            } 
        }

        return resources;
    }

    private List<Resource> saveResources(instance, resourcesXML) {
        XMLConverter converter = new XMLConverter();
        def rootDir
        switch(instance.class.name) {
            case [Observation.class.name, Checklists.class.name]:
            rootDir = grailsApplication.config.speciesPortal.observations.rootDir
            break;
            
            case Species.class.name:
            rootDir = grailsApplication.config.speciesPortal.resources.rootDir
            break;
        }
        converter.setResourcesRootDir(rootDir);
        def relImagesContext = resourcesXML.images.image?.getAt(0)?.fileName?.getAt(0)?.text()?.replace(rootDir.toString(), "")?:""
        relImagesContext = new File(relImagesContext).getParent();
        println "+++++++++++++++++++++"
        println resourcesXML
        return converter.createMedia(resourcesXML, relImagesContext);
    }

}
