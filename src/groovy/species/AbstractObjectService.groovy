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
			item.url = "/" + controller + "/show/" + param['observation'].id
			item.imageTitle = param['title']
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
/*			if(param['observation'].notes()) {
				item.notes = param['observation'].notes()
			} else {
*/  			item.notes = param['observation'].summary();				
//			}
            
            if(param['featuredNotes'] ==  null) {
            }
            else {
                String n = item.notes;
                item.notes = param['featuredNotes']
                if(n)
                    item.notes += "<p><small>"+ n +"</small></p>" 
            }
           
            if(param['featuredOn']) {
                item.featuredOn = param['featuredOn'].getTime();
            }
            def obj = param['observation'];
            if(obj.hasProperty('latitude') && obj.latitude) item.lat = obj.latitude
            if(obj.hasProperty('longitude') && obj.longitude) item.lng = obj.longitude
            if(obj.hasProperty('geoPrivacy') && obj.geoPrivacy) item.geoPrivacy = obj.geoPrivacy
            if(obj.hasProperty('isChecklist') && obj.isChecklist) item.isChecklist = obj.isChecklist
            if(obj.hasProperty('fromDate') && obj.fromDate) item.observedOn = obj.fromDate.getTime();
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
        //TODO:change hardcoded string to class definitions
        if (controller == "observation") {
            type = "species.participation.Observation";
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
        def count;
        if(ugId == null) {
            def queryParams = ["type": type]
            def countQuery = "select count(*) from Featured feat where feat.userGroup.id is null and feat.objectType = :type "
            log.debug "CountQuery:"+ countQuery + "params: "+queryParams
            count = Featured.executeQuery(countQuery, queryParams)
            queryParams["max"] = limit
            queryParams["offset"] = offset
            def query = "from Featured feat where feat.userGroup.id is null and feat.objectType = :type "
            def orderByClause = "order by feat.createdOn desc"
            query += orderByClause
            log.debug "FeaturedQuery:"+ query + "params: "+queryParams
            featured = Featured.executeQuery(query, queryParams);
        }
        else{
            def queryParams = ["ugId": ugId]
            queryParams["type"] = type
            def countQuery = "select count(*) from Featured feat where feat.userGroup.id = :ugId and feat.objectType = :type "
            log.debug "CountQuery:"+ countQuery + "params: "+queryParams
            count = Featured.executeQuery(countQuery, queryParams)
            queryParams["max"] = limit
            queryParams["offset"] = offset
            def query = "from Featured feat where feat.userGroup.id = :ugId and feat.objectType = :type "
            def orderByClause = "order by feat.createdOn desc"
            query += orderByClause
            
            log.debug "FeaturedQuery:"+ query + "params: "+queryParams

            featured = Featured.executeQuery(query, queryParams);
            
        }

        def observations = []
        featured.each {
            observations.add(activityFeedService.getDomainObject(it.objectType,it.objectId))
        }
        def result = []
        def i = 0;
        observations.each {
            if(featured.notes[i] == null){
                result.add([ 'observation':it, 'title': it.fetchSpeciesCall(),  'featuredOn':featured.createdOn[i] ]);
            }
            else{
                result.add([ 'observation':it, 'title': it.fetchSpeciesCall(), 'featuredOn':featured.createdOn[i], 'featuredNotes': featured.notes[i] ]);
            }
            i = i + 1;
        }
        return['observations':result,'count':count[0]]
                		
    }



}
