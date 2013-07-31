package speciespage

import grails.util.Environment;
import grails.util.GrailsNameUtils;
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine

import org.grails.taggable.TagLink;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

import species.Resource;
import species.Habitat;
import species.Language;
import species.utils.Utils;
import species.TaxonomyDefinition;
import species.Resource.ResourceType;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.participation.ActivityFeed;
import species.participation.Follow;
import species.participation.Observation;
import species.participation.Checklists;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import species.participation.ObservationFlag.FlagType
import species.participation.RecommendationVote.ConfidenceType;
import species.participation.Annotation
import species.sourcehandler.XMLConverter;
import species.utils.ImageType;
import species.utils.Utils;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.DateTools;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

import org.apache.solr.common.util.DateUtil;
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.codehaus.groovy.grails.web.util.WebUtils;

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import content.eml.Coverage;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import species.groups.UserGroupController;
import species.groups.UserGroup;

class ObservationService {

	static transactional = false

	def recommendationService;
	def observationsSearchService;
	def grailsApplication;
	def dataSource;
	def springSecurityService;
	def curationService;
	def commentService;
	def userGroupService;
	def activityFeedService;
	def mailService;
	def sessionFactory;
	def SUserService;
	
	static final String OBSERVATION_ADDED = "observationAdded";
	static final String SPECIES_RECOMMENDED = "speciesRecommended";
	static final String SPECIES_AGREED_ON = "speciesAgreedOn";
	static final String SPECIES_NEW_COMMENT = "speciesNewComment";
	static final String SPECIES_REMOVE_COMMENT = "speciesRemoveComment";
	static final String OBSERVATION_FLAGGED = "observationFlagged";
	static final String OBSERVATION_DELETED = "observationDeleted";
	static final String CHECKLIST_DELETED= "checklistDeleted";
	static final String DOWNLOAD_REQUEST = "downloadRequest";
	/**
	 * 
	 * @param params
	 * @return
	 */
	Observation createObservation(params) {
		//log.info "Creating observations from params : "+params
		Observation observation = new Observation();
		updateObservation(params, observation);
		return observation;
	}
	/**
	 * 
	 * @param params
	 * @param observation
	 */
	void updateObservation(params, observation){
        //log.debug "Updating obv with params ${params}"
		if(params.author)  {
			observation.author = params.author;
		}

		if(params.url) {
			observation.url = params.url;
		}
		observation.group = params.group?:SpeciesGroup.get(params.group_id);
		observation.notes = params.notes;
		observation.fromDate = parseDate(params.fromDate);
		observation.toDate = params.toDate ? parseDate(params.toDate) : observation.fromDate
		observation.placeName = params.placeName//?:observation.reverseGeocodedName;
		observation.reverseGeocodedName = params.reverse_geocoded_name?:observation.placeName
		
		//observation.location = 'POINT(' + params.longitude + ' ' + params.latitude + ')'
		observation.locationAccuracy = params.location_accuracy?:params.locationAccuracy;
		observation.geoPrivacy = false;

		observation.habitat = params.habitat?:Habitat.get(params.habitat_id);

		observation.agreeTerms = (params.agreeTerms?.equals('on'))?true:false;
		observation.sourceId = params.sourceId ?: null
		observation.checklistAnnotations = params.checklistAnnotations?:observation.checklistAnnotations
		
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
        if(params.latitude && params.longitude) {
            observation.topology = geometryFactory.createPoint(new Coordinate(params.longitude?.toFloat(), params.latitude?.toFloat()));
        } else if(params.areas) {
            WKTReader wkt = new WKTReader(geometryFactory);
            try {
                Geometry geom = wkt.read(params.areas);
                observation.topology = geom;
            } catch(ParseException e) {
                log.error "Error parsing polygon wkt : ${params.areas}"
            }
        }

		def resourcesXML = createResourcesXML(params);
		def resources = saveResources(observation, resourcesXML);
		
		observation.resource?.clear();
		resources.each { resource ->
			observation.addToResource(resource);
		}
	}
	
	Map saveObservation(params, sendMail=true){
		//TODO:edit also calls here...handle that wrt other domain objects
		params.author = springSecurityService.currentUser;
		def observationInstance, feedType, feedAuthor, mailType; 
		try {
			
			if(params.action == "save"){
				observationInstance = createObservation(params);
				feedType = activityFeedService.OBSERVATION_CREATED
				feedAuthor = observationInstance.author
				mailType = OBSERVATION_ADDED
			}else{
				observationInstance = Observation.get(params.id.toLong())
				updateObservation(params, observationInstance)
				feedType = activityFeedService.OBSERVATION_UPDATED
				feedAuthor = springSecurityService.currentUser
			}
			
			if(!observationInstance.hasErrors() && observationInstance.save(flush:true)) {
				//flash.message = "${message(code: 'default.created.message', args: [message(code: 'observation.label', default: 'Observation'), observationInstance.id])}"
				log.debug "Successfully created observation : "+observationInstance
				params.obvId = observationInstance.id
				activityFeedService.addActivityFeed(observationInstance, null, feedAuthor, feedType);
				
				saveObservationAssociation(params, observationInstance)
								
				if(sendMail)
					sendNotificationMail(mailType, observationInstance, null, params.webaddress);
					
				params["createNew"] = true
				return ['success' : true, observationInstance:observationInstance]
			} else {
				observationInstance.errors.allErrors.each { log.error it }
				return ['success' : false, observationInstance:observationInstance]
			}
		} catch(e) {
			e.printStackTrace();
			return ['success' : false, observationInstance:observationInstance]
		}
	}
	
	/**
	 * @param params
	 * @param observationInstance
	 * @return
	 * saving Groups, tags and resources
	 */
	def saveObservationAssociation(params, observationInstance){
		def tags = (params.tags != null) ? Arrays.asList(params.tags) : new ArrayList();
		observationInstance.setTags(tags);

		if(params.groupsWithSharingNotAllowed) {
			setUserGroups(observationInstance, [params.groupsWithSharingNotAllowed]);
		} else {
			if(params.userGroupsList) {
				def userGroups = (params.userGroupsList != null) ? params.userGroupsList.split(',').collect{k->k} : new ArrayList();
				setUserGroups(observationInstance, userGroups);
			}
		}
		
		log.debug "Saving ratings for the resources"
		observationInstance?.resource?.each { res ->
			if(res.rating) {
				res.rate(springSecurityService.currentUser, res.rating);
			}
		}
	}

	/**
	 *
	 * @param params
	 * @return
	 */
	Map getRelatedObservations(params) {
		log.debug params;
		def max = Math.min(params.limit ? params.limit.toInteger() : 12, 100)
		def offset = params.offset ? params.offset.toInteger() : 0

		def relatedObv
		if(params.filterProperty == "speciesName") {
			relatedObv = getRelatedObservationBySpeciesName(params.id.toLong(), max, offset)
		} else if(params.filterProperty == "speciesGroup"){
			relatedObv = getRelatedObservationBySpeciesGroup(params.filterPropertyValue.toLong(),  max, offset)
		}else if(params.filterProperty == "user"){
			relatedObv = getRelatedObservationByUser(params.filterPropertyValue.toLong(), max, offset, params.sort)
		}else if(params.filterProperty == "nearBy"){
			relatedObv = getNearbyObservations(params.id, max, offset)
		} else if(params.filterProperty == "taxonConcept") {
			relatedObv = getRelatedObservationByTaxonConcept(params.filterPropertyValue.toLong(), max, offset)
		} else{
			relatedObv = getRelatedObservation(params.filterProperty, params.id.toLong(), max, offset)
		}
		
		if(params.contextGroupWebaddress || params.webaddress){
			//def group = UserGroup.findByWebaddress(params.contextGroupWebaddress)
			//println group.webaddress
			relatedObv.observations.each { map ->
				boolean inGroup = map.observation.userGroups.find { it.webaddress == params.contextGroupWebaddress || it.webaddress == params.webaddress} != null
				map.inGroup = inGroup;
				//map.observation.inGroup = inGroup;
			}
		}
		
		return [relatedObv:relatedObv, max:max]
	}



	List getRelatedObservation(String property, long obvId, int limit, long offset){
		if(!property){
			return []
		}
		
		def propertyValue = Observation.read(obvId)[property]
		def query = "from Observation as obv where obv." + property + " = :propertyValue and obv.id != :parentObvId and obv.isDeleted = :isDeleted order by obv.createdOn desc"
		def obvs = Observation.findAll(query, [propertyValue:propertyValue, parentObvId:obvId, max:limit, offset:offset, isDeleted:false])
		def result = [];
		obvs.each {
			result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
		}
		return result
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	Map getRelatedObservationBySpeciesName(long obvId, int limit, long offset){
		//String speciesName = getSpeciesNames(obvId)
		//log.debug speciesName
		//log.debug speciesName.getClass();
		return getRelatedObservationBySpeciesNames(obvId, limit, offset)
	}
	/**
	 * 
	 * @param params
	 * @return
	 */
	Map getRelatedObservationByUser(long userId, int limit, long offset, String sort){
		//getting count
		def queryParams = [isDeleted:false]
		def countQuery = "select count(*) from Observation obv where obv.author.id = :userId and obv.isDeleted = :isDeleted and obv.isShowable = :isShowable "
		queryParams["userId"] = userId
		queryParams["isDeleted"] = false;
		queryParams["isShowable"] = true;
		def count = Observation.executeQuery(countQuery, queryParams)

		
		//getting observations
		def query = "from Observation obv where obv.author.id = :userId and obv.isDeleted = :isDeleted and obv.isShowable = :isShowable "
		def orderByClause = "order by obv." + (sort ? sort : "createdOn") +  " desc"
		query += orderByClause

		queryParams["max"] = limit
		queryParams["offset"] = offset

		def observations = Observation.findAll(query, queryParams);
		def result = [];
		observations.each {
			result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
		}

		return ["observations":result, "count":count[0]]
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	List getRelatedObservationBySpeciesGroup(long groupId, int limit, long offset){
		log.debug(groupId)

		def query = ""
		def obvs = null
		//if filter group is all
		def groupIds = getSpeciesGroupIds(groupId)
		if(!groupIds) {
			query = "from Observation as obv where obv.isDeleted = :isDeleted order by obv.createdOn desc "
			obvs = Observation.findAll(query, [max:limit, offset:offset, isDeleted:false])
		}else if(groupIds instanceof List){
			//if group is others
			query = "from Observation as obv where obv.isDeleted = :isDeleted and obv.group is null or obv.group.id in (:groupIds) order by obv.createdOn desc"
			obvs = Observation.findAll(query, [groupIds:groupIds, max:limit, offset:offset, isDeleted:false])
		}else{
			query = "from Observation as obv where obv.isDeleted = :isDeleted and obv.group.id = :groupId order by obv.createdOn desc"
			obvs = Observation.findAll(query, [groupId:groupIds, max:limit, offset:offset, isDeleted:false])
		}
		log.debug(" == obv size " + obvs.size())

		def result = [];
		obvs.each {
			result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
		}

		return result
	}

	/**
	 * 
	 * @param groupId
	 * @return
	 */
	Object getSpeciesGroupIds(groupId){
		def groupName = SpeciesGroup.read(groupId)?.name
		//if filter group is all
		if(!groupName || (groupName == grailsApplication.config.speciesPortal.group.ALL)){
			return null
		}
		return groupId
	}


	/**
	 * 	
	 * @param obvId
	 * @return
	 */
	String getSpeciesNames(obvId){
		return Observation.read(obvId).fetchSpeciesCall();
	}

	/**
	 * 
	 * @param speciesName
	 * @param params
	 * @return
	 */
	Map getRelatedObservationBySpeciesNames(long obvId, int limit, long offset){
		Observation parentObv = Observation.read(obvId);
		if(!parentObv.maxVotedReco) {
			return ["observations":[], "count":0];
		}
		return getRelatedObservationByReco(obvId, parentObv.maxVotedReco, limit, offset)
	}
	
	private Map getRelatedObservationByReco(long obvId, Recommendation maxVotedReco, int limit, long offset){
		def observations = Observation.withCriteria (max: limit, offset: offset) {
			projections {
				groupProperty('sourceId')
				groupProperty('isShowable')
			}
			and {
				eq("maxVotedReco", maxVotedReco)
				eq("isDeleted", false)
				if(obvId) ne("id", obvId)
			}
			order("isShowable", "desc")
		}
		def result = [];
		observations.each {
			def obv = Observation.get(it[0])
			result.add(['observation':obv, 'title':(obv.isChecklist)? obv.title : maxVotedReco.name]);
		}
		def count = Observation.createCriteria().count {
			projections {
				count(groupProperty('sourceId'))
			}
			and {
				eq("maxVotedReco", maxVotedReco)
				eq("isDeleted", false)
				if(obvId) ne("id", obvId)
			}
		}
		return ["observations":result, "count":count]
	}
	
	Map getRelatedObservationByTaxonConcept(long taxonConceptId, int limit, long offset){
		def taxonConcept = TaxonomyDefinition.read(taxonConceptId);
		if(!taxonConcept) return ['observations':[], 'count':0]
		
		List<Recommendation> scientificNameRecos = recommendationService.searchRecoByTaxonConcept(taxonConcept);
		if(scientificNameRecos) {
			def observations = Observation.withCriteria (max: limit, offset: offset) {
				and {
					'in'("maxVotedReco", scientificNameRecos)
					eq("isDeleted", false)
					eq("isShowable", true)
				}
				order("lastRevised", "desc")
			}
			def result = [];
			observations.each {
				result.add(['observation':it, 'title':it.fetchSpeciesCall()]);
			}
			def count = Observation.createCriteria().count {
				and {
					'in'("maxVotedReco", scientificNameRecos)
					eq("isDeleted", false)
					eq("isShowable", true)
				}
			}
			return ['observations':result, 'count':count]
		} else {
			return ['observations':[], 'count':0]
		}
	}
	
	static List createUrlList2(observations){
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
		String iconBasePath = config.speciesPortal.observations.serverURL
		def urlList = createUrlList2(observations, iconBasePath)
//		urlList.each {
//			it.imageLink = it.imageLink.replaceFirst(/\.[a-zA-Z]{3,4}$/, config.speciesPortal.resources.images.thumbnail.suffix)
//		}
		return urlList
	}
	static List createUrlList2(observations, String iconBasePath){
		List urlList = []
		for(param in observations){
			def item = [:];
            def controller = getTargetController(param['observation']);
			item.url = "/" + controller + "/show/" + param['observation'].id
			item.imageTitle = param['title']
            item.type = controller
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			Resource image = param['observation'].mainImage()
			if(image){
				if(image.type == ResourceType.IMAGE) {
					item.imageLink = image.thumbnailUrl(param['observation'].isChecklist ? null: iconBasePath, param['observation'].isChecklist ? '.png' :null)//thumbnailUrl(iconBasePath)
				} else if(image.type == ResourceType.VIDEO) {
					item.imageLink = image.thumbnailUrl()
				}
			}else{
				item.imageLink =  config.speciesPortal.resources.serverURL + "/" + "no-image.jpg"
			}			
			if(param.inGroup) {
				item.inGroup = param.inGroup;
			}
			if(param['observation'].notes) {
				item.notes = param['observation'].notes
			} else {
				String location = "Observed at '" + (param['observation'].placeName.trim()?:param['observation'].reverseGeocodedName) +"'"
				String desc = "- "+ location +" by "+param['observation'].author.name.capitalize()+" in species group "+param['observation'].group.name + " and habitat "+ param['observation'].habitat.name;
				item.notes = desc;				
			}
			urlList << item;
		}
		return urlList
	}

	//XXX for new checklists doamin object and controller name is not same as grails convention so using this method 
	// to resolve controller name
	private static getTargetController(domainObj){
		if(domainObj.instanceOf(Checklists)){
			return "checklist"
		}else if(domainObj.instanceOf(SUser)){
			return "user"
		}else{
			return domainObj.class.getSimpleName().toLowerCase()
		}
	}
	
	Map getRecommendation(params){
		def recoName = params.recoName;
		def canName = params.canName;
		def commonName = params.commonName;
		def languageId = Language.getLanguage(params.languageName).id;
//		def refObject = params.observation?:Observation.get(params.obvId);
		
		//if source of recommendation is other that observation (i.e Checklist)
//		refObject = refObject ?: params.refObject
		
		Recommendation commonNameReco = recommendationService.findReco(commonName, false, languageId, null);
		Recommendation scientificNameReco = recommendationService.getRecoForScientificName(recoName, canName, commonNameReco);
		
//		curationService.add(scientificNameReco, commonNameReco, refObject, springSecurityService.currentUser);
		
		//giving priority to scientific name if its available. same will be used in determining species call
		return [mainReco : (scientificNameReco ?:commonNameReco), commonNameReco:commonNameReco];
	}
	
	
	/**
	 * 
	 */
	private List<Resource> saveResources(Observation observation, resourcesXML) {
		XMLConverter converter = new XMLConverter();
		converter.setResourcesRootDir(grailsApplication.config.speciesPortal.observations.rootDir);
		def relImagesContext = resourcesXML.images.image?.getAt(0)?.fileName?.getAt(0)?.text()?.replace(grailsApplication.config.speciesPortal.observations.rootDir.toString(), "")?:""
		relImagesContext = new File(relImagesContext).getParent();
		return converter.createMedia(resourcesXML, relImagesContext);
	}

	/**
	 * 
	 * @param confidenceType
	 * @return
	 */
	ConfidenceType getConfidenceType(String confidenceType) {
		if(!confidenceType) return null;
		for(ConfidenceType type : ConfidenceType) {
			if(type.name().equals(confidenceType)) {
				return type;
			}
		}
		return null;
	}
	/**
	 * 
	 * @param flagType
	 * @return
	 */
	FlagType getObservationFlagType(String flagType){
		if(!flagType) return null;
		for(FlagType type : FlagType) {
			if(type.name().equals(flagType)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * 
	 */
	private def createResourcesXML(params) {
        println params
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();
		def resources = builder.createNode("resources");
		Node images = new Node(resources, "images");
		Node videos = new Node(resources, "videos");
		String uploadDir =  grailsApplication.config.speciesPortal.observations.rootDir;
		List files = [];
		List titles = [];
		List licenses = [];
		List type = [];
		List url = []
        List ratings = [];
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
				ratings.add(params.get('rating_'+index));
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
				new Node(image, "contributor", params.author.username);
				new Node(image, "license", licenses.getAt(key));
				new Node(image, "rating", ratings.getAt(key));
				new Node(image, "user", springSecurityService.currentUser?.id);
			} else {
				log.warn("No reference key for image : "+key);
			} 
		}

		return resources;
	}



	Map findAllTagsSortedByObservationCount(int max){
		def sql =  Sql.newInstance(dataSource);
		//query with observation delete handle
		String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where tl.tag_ref = obv.id and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + max ;

		//String query = "select t.name as name from tag_links as tl, tags as t where t.id = tl.tag_id group by t.name order by count(t.name) desc limit " + max ;
		LinkedHashMap tags = [:]
		sql.rows(query).each{
			tags[it.getProperty("name")] = it.getProperty("obv_count");
		};
		return tags;
	}

	def getNoOfTags() {
		def count = TagLink.executeQuery("select count(*) from TagLink group by tag_id");
		return count.size()
	}

	Map getNearbyObservations(String observationId, int limit, int offset){
		int maxRadius = 200;
		int maxObvs = 50;
		def sql =  Sql.newInstance(dataSource);
		def rows = sql.rows("select count(*) as count from observation as g1, observation as g2 where ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g2.is_showable = true and g1.id = :observationId and g1.id <> g2.id", [observationId: Long.parseLong(observationId), maxRadius:maxRadius]);
		def totalResultCount = Math.min(rows[0].getProperty("count"), maxObvs);
		limit = Math.min(limit, maxObvs - offset);
		def resultSet = sql.rows("select g2.id,  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) as distance from observation as g1, observation as g2 where  ROUND(ST_Distance_Sphere(ST_Centroid(g1.topology), ST_Centroid(g2.topology))/1000) < :maxRadius and g2.is_deleted = false and g2.is_showable = true and g1.id = :observationId and g1.id <> g2.id order by ST_Distance(g1.topology, g2.topology), g2.last_revised desc limit :max offset :offset", [observationId: Long.parseLong(observationId), maxRadius:maxRadius, max:limit, offset:offset])

		def nearbyObservations = []
		for (row in resultSet){
			nearbyObservations.add(["observation":Observation.findById(row.getProperty("id")), "title":"Found "+row.getProperty("distance")+" km away"])
		}
		return ["observations":nearbyObservations, "count":totalResultCount]
	}

	Map getAllTagsOfUser(userId){
		def sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name,  count(t.name) as obv_count from tag_links as tl, tags as t, observation as obv where obv.author_id = " + userId + " and tl.tag_ref = obv.id and t.id = tl.tag_id and obv.is_deleted = false group by t.name order by count(t.name) desc, t.name asc ";

		LinkedHashMap tags = [:]
		sql.rows(query).each{
			tags[it.getProperty("name")] = it.getProperty("obv_count");
		};
		return tags;
	}

	Map getAllRelatedObvTags(params){
		//XXX should handle in generic way
		params.limit = 100
		params.offset = 0
		def obvIds = getRelatedObservations(params).relatedObv.observations.observation.collect{it.id}
		obvIds.add(params.id.toLong())
		return getTagsFromObservation(obvIds)
	}
	
	Map getRelatedTagsFromObservation(obv){
		int tagsLimit = 30;
		//println "HACK to load obv at ObvService: ${obv.author}";
		def tagNames = obv.tags
		
		LinkedHashMap tags = [:]
		if(tagNames.isEmpty()){
			return tags
		}
		
		Sql sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where t.name in " +  getSqlInCluase(tagNames) + " and tl.tag_ref = obv.id and tl.type = '"+GrailsNameUtils.getPropertyName(obv.class).toLowerCase()+"' and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;
		
		sql.rows(query, tagNames).each{
			tags[it.getProperty("name")] = it.getProperty("obv_count");
		};
		return tags;
	}

	protected Map getTagsFromObservation(obvIds){
		int tagsLimit = 30;
		LinkedHashMap tags = [:]
		if(!obvIds){
			return tags
		}

		def sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where tl.tag_ref in " + getSqlInCluase(obvIds)  + " and tl.tag_ref = obv.id and tl.type = '"+GrailsNameUtils.getPropertyName(Observation.class).toLowerCase()+"' and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;

		sql.rows(query, obvIds).each{
			tags[it.getProperty("name")] = it.getProperty("obv_count");
		};
		return tags;
	}

	Map getFilteredTags(params){
        //TODO:FIXIT ... doesnt return all tags as the last param true is not handled
		return getTagsFromObservation(getFilteredObservations(params, -1, -1, true).observationInstanceList.collect{it[0]});
	}
	
	private String getSqlInCluase(list){
		return "(" + list.collect {'?'}.join(", ") + ")"
	}

	/**
	 * Filter observations by group, habitat, tag, user, species
	 * max: limit results to max: if max = -1 return all results
	 * offset: offset results: if offset = -1 its not passed to the 
	 * executing query
     Spatial Query format 
        //create your query 
        Query q = session.createQuery(< your hql > ); 
        Geometry geom = < your parameter value> 
        //now first create a custom type 
        Type geometryType = new CustomType(GeometryUserType.class, null); 
        q.setParameter(:geoExp0, geom, geometryType); 
	 */
	Map getFilteredObservations(def params, max, offset, isMapView = false) {

		def queryParts = getFilteredObservationsFilterQuery(params) 
		String query = queryParts.query;
		long checklistCount = 0, allObservationCount = 0;

        def boundGeometry = queryParts.queryParams.remove('boundGeometry'); 
/*        if(isMapView) {//To get id, topology of all markers
			query = queryParts.mapViewQuery + queryParts.filterQuery;// + queryParts.orderByClause
		} else {*/
			query += queryParts.filterQuery + queryParts.orderByClause
//		}
        
        log.debug "checklistCountQuery : "+queryParts.checklistCountQuery;
        log.debug "allObservationCountQuery : "+queryParts.allObservationCountQuery;

        log.debug query;
        log.debug queryParts.queryParams;
        
        def checklistCountQuery = sessionFactory.currentSession.createQuery(queryParts.checklistCountQuery)
        def allObservationCountQuery = sessionFactory.currentSession.createQuery(queryParts.allObservationCountQuery)
       
        def hqlQuery = sessionFactory.currentSession.createQuery(query)
        if(params.bounds && boundGeometry) {
            hqlQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(org.hibernatespatial.GeometryUserType, null))
            checklistCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(org.hibernatespatial.GeometryUserType, null))
            allObservationCountQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(org.hibernatespatial.GeometryUserType, null))
        } 

        if(max > -1){
            hqlQuery.setMaxResults(max);
			queryParts.queryParams["max"] = max
        }
        if(offset > -1) {
            hqlQuery.setFirstResult(offset);
			queryParts.queryParams["offset"] = offset
        }

        hqlQuery.setProperties(queryParts.queryParams);
		def observationInstanceList = hqlQuery.list();
		
        checklistCountQuery.setProperties(queryParts.queryParams)
		checklistCount = checklistCountQuery.list()[0]

        allObservationCountQuery.setProperties(queryParts.queryParams)
		allObservationCount = allObservationCountQuery.list()[0]

		if(params.daterangepicker_start){
			queryParts.queryParams["daterangepicker_start"] = params.daterangepicker_start
		}
		if(params.daterangepicker_end){
			queryParts.queryParams["daterangepicker_end"] =  params.daterangepicker_end
		}
		return [observationInstanceList:observationInstanceList, allObservationCount:allObservationCount, checklistCount:checklistCount, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
	}

    /**
    *
    **/
	def getFilteredObservationsFilterQuery(params) {
		params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = params.habitat.toLong()
		//params.userName = springSecurityService.currentUser.username;

		def queryParams = [isDeleted : false]
		def activeFilters = [:]

		def query = "select "
        if(params.fetchField) {
            query += " obv.id,"
            params.fetchField.split(",").each { fetchField ->
                if(!fetchField.equalsIgnoreCase('id'))
                    query += " obv."+fetchField+","
            }
            query = query [0..-2];
            queryParams['fetchField'] = params.fetchField
        } else {
            query += " obv "
        }
        query += " from Observation obv "
		//def mapViewQuery = "select obv.id, obv.topology, obv.isChecklist from Observation obv "
        def userGroupQuery = " ", tagQuery = '';
		def filterQuery = " where obv.isDeleted = :isDeleted and obv.isShowable = true "

		if(params.sGroup){
			params.sGroup = params.sGroup.toLong()
			def groupId = getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			}else{
				filterQuery += " and obv.group.id = :groupId "
				queryParams["groupId"] = groupId
				activeFilters["sGroup"] = groupId
			}
		}

        println "*************************"
        println params.webaddress;
        if(params.userGroup || params.webaddress) {
            if(!(params.userGroup instanceof UserGroup) && (params.userGroup instanceof String || params.userGroup instanceof Long || params.webaddress)) {
    			def userGroupController = new UserGroupController();
	    		params.userGroup = userGroupController.findInstance(params.userGroup, params.webaddress);
            }
            log.debug "Filtering from usergourp : ${params.usergroup}"
		    userGroupQuery = " join obv.userGroups userGroup "
            query += userGroupQuery
		    filterQuery += " and userGroup.id =:userGroupId "
		    queryParams['userGroupId'] = params.userGroup.id
		    queryParams['userGroup'] = params.userGroup
        }

		if(params.tag){
			tagQuery = ",  TagLink tagLink "
            query += tagQuery;
			//mapViewQuery = "select obv.topology from Observation obv, TagLink tagLink "
			filterQuery +=  " and obv.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "

			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Observation.class);
			activeFilters["tag"] = params.tag
		}


		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
			filterQuery += " and obv.habitat.id = :habitat "
			queryParams["habitat"] = params.habitat
			activeFilters["habitat"] = params.habitat
		}

		if(params.user){
			filterQuery += " and obv.author.id = :user "
			queryParams["user"] = params.user.toLong()
			activeFilters["user"] = params.user.toLong()
		}

		if(params.speciesName && (params.speciesName != grailsApplication.config.speciesPortal.group.ALL)){
			filterQuery += " and (obv.isChecklist = false and obv.maxVotedReco is null) "
			//queryParams["speciesName"] = params.speciesName
			activeFilters["speciesName"] = params.speciesName
		}

		if(params.isFlagged && params.isFlagged.toBoolean()){
			filterQuery += " and obv.flagCount > 0 "
			activeFilters["isFlagged"] = params.isFlagged.toBoolean()
		}
		
		if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()){
			filterQuery += " and obv.isChecklist = true "
			activeFilters["isChecklistOnly"] = params.isChecklistOnly.toBoolean()
		}

		
		if(params.daterangepicker_start && params.daterangepicker_end){
			def df = new SimpleDateFormat("dd/MM/yyyy")
			def startDate = df.parse(params.daterangepicker_start)
			def endDate = df.parse(params.daterangepicker_end)
			Calendar cal = Calendar.getInstance(); // locale-specific
			cal.setTime(endDate)
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.MINUTE, 59);
			endDate = new Date(cal.getTimeInMillis())
			
			filterQuery += " and ( created_on between :daterangepicker_start and :daterangepicker_end) "
			queryParams["daterangepicker_start"] =  startDate   
			queryParams["daterangepicker_end"] =  endDate
			
			activeFilters["daterangepicker_start"] = params.daterangepicker_start
			activeFilters["daterangepicker_end"] =  params.daterangepicker_end
		}
		
		if(params.bounds) {
			def bounds = params.bounds.split(",")
			
			def swLat = bounds[0].toFloat()
			def swLon = bounds[1].toFloat()
			def neLat = bounds[2].toFloat()
			def neLon = bounds[3].toFloat()

            def boundGeometry = getBoundGeometry(swLat, swLon, neLat, neLon)
            filterQuery += " and within (obv.topology, :boundGeometry) = true " //) ST_Contains( :boundGeomety,  obv.topology) "
			//filterQuery += " and 1=0 ";// and obv.latitude > " + swLat + " and  obv.latitude < " + neLat + " and obv.longitude > " + swLon + " and obv.longitude < " + neLon
            queryParams['boundGeometry'] = boundGeometry
			activeFilters["bounds"] = params.bounds
		} 
		
		def orderByClause = " order by obv." + (params.sort ? params.sort : "lastRevised") +  " desc, obv.id asc"
		
		def checklistCountQuery = "select count(*) from Observation obv " + userGroupQuery +" "+((params.tag)?tagQuery:'')+filterQuery + " and obv.isChecklist = true "
		def allObservationCountQuery = "select count(*) from Observation obv " + userGroupQuery +" "+((params.tag)?tagQuery:'')+filterQuery
	
		return [query:query, allObservationCountQuery:allObservationCountQuery, checklistCountQuery:checklistCountQuery, filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

	}

    /**
    *
    **/
    private getBoundGeometry(x1, y1, x2, y2){
        def p1 = new Coordinate(y1, x1)
        def p2 = new Coordinate(y1, x2)
        def p3 = new Coordinate(y2, x2)
        def p4 = new Coordinate(y2, x1)
        Coordinate[] arr = new Coordinate[5]
        arr[0] = p1
        arr[1] = p2
        arr[2] = p3
        arr[3] = p4
        arr[4] = p1
        def gf = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID)
        def lr = gf.createLinearRing(arr)
        def pl = gf.createPolygon(lr, null)
        return pl
    }

	Date parseDate(date){
		try {
			return date? Date.parse("dd/MM/yyyy", date):new Date();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

    /**
    * Gets all obvs from all groups
    **/
	long getAllObservationsOfUser(SUser user) {
        //TODO: filter on usergroup if required
		return (long) Observation.createCriteria().count {
			and {
				eq("author", user)
				eq("isDeleted", false)
				eq("isShowable", true)
			}
		}
		//return (long)Observation.countByAuthorAndIsDeleted(user, false);
	}

    /**
    * Gets all recommendations of user made in all groups
    **/
	long getAllRecommendationsOfUser(SUser user) {
        //TODO: filter on usergroup if required
		def result = RecommendationVote.executeQuery("select count(recoVote) from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable", [userId:user.id, isDeleted:false, isShowable:true]);
		return (long)result[0];
	}
	
	List getRecommendationsOfUser(SUser user, int max, long offset) {
		if(max == -1) {
			def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable order by recoVote.votedOn desc", [userId:user.id, isDeleted:false, isShowable:true]);
			return recommendationVotesList;
		} else {
			def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted and recoVote.observation.isShowable = :isShowable order by recoVote.votedOn desc", [userId:user.id, isDeleted:false, isShowable:true], [max:max, offset:offset]);
			return recommendationVotesList;
		}
	}
	
	Map getIdentificationEmailInfo(m, requestObj, unsubscribeUrl, controller="", action=""){
		def source = m.source;
		def mailSubject = ""
		def activitySource = ""
		
        switch (source) {
			case "observationShow":
				mailSubject = "Share Observation"
				activitySource = "observation"
				break
			
			case  "observationList" :
				mailSubject = "Share Observation List"
				activitySource = "observation list"
				break
			
			case "userProfileShow":
				mailSubject = "Share User Profile"
				activitySource = "user profile"
				break
				
			case "userGroupList":
				mailSubject = "Share Groups List"
				activitySource = "user groups list"
				break
			case "userGroupInvite":
				mailSubject = "Invitation to join group"
				activitySource = "user group"
				break
            case controller+action.capitalize():
                mailSubject = "Share "+controller
                activitySource = controller
                break;
			default:
				log.debug "invalid source type ${source}"
		}
		def currentUser = springSecurityService.currentUser?:""
		def templateMap = [currentUser:currentUser, activitySource:activitySource, domain:Utils.getDomainName(requestObj)]
		def conf = SpringSecurityUtils.securityConfig
		def staticMessage = conf.ui.askIdentification.staticMessage
		if (staticMessage.contains('$')) {
			staticMessage = evaluate(staticMessage, templateMap)
		} 
		
		templateMap["activitySourceUrl"] = m.sourcePageUrl?: ""
		templateMap["unsubscribeUrl"] = unsubscribeUrl ?: ""
		templateMap["userMessage"] = m.userMessage?: ""
		def body = conf.ui.askIdentification.emailBody
		if (body.contains('$')) {
			body = evaluate(body, templateMap)
		}
		return [mailSubject:mailSubject, mailBody:body, staticMessage:staticMessage, source:source]
	}

	private String evaluate(s, binding) {
		new SimpleTemplateEngine().createTemplate(s).make(binding)
	}

	/**
	*
	* @param params
	* @return
	*/
   def getObservationsFromSearch(params) {
	   def max = Math.min(params.max ? params.max.toInteger() : 12, 100)
	   def offset = params.offset ? params.offset.toLong() : 0

	   def model;
	   
	   try {
		   model = getFilteredObservationsFromSearch(params, max, offset, false);
	   } catch(SolrException e) {
		   e.printStackTrace();
		   //model = [params:params, observationInstanceTotal:0, observationInstanceList:[],  queryParams:[max:0], tags:[]];
	   }
	   return model;
   }
   
	/**
	 * Filter observations by group, habitat, tag, user, species
	 * max: limit results to max: if max = -1 return all results
	 * offset: offset results: if offset = -1 its not passed to the
	 * executing query
	 */
	Map getFilteredObservationsFromSearch(params, max, offset, isMapView){
		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
		params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = params.habitat.toLong()
		def queryParams = [isDeleted : false, isShowable:true]
		
		def activeFilters = [:]
		
		
		NamedList paramsList = new NamedList();
		
		//params.userName = springSecurityService.currentUser.username;
		queryParams["query"] = params.query
		activeFilters["query"] = params.query
		params.query = params.query ?: "";
		
		String aq = "";
		int i=0;
		if(params.aq instanceof GrailsParameterMap || params.aq instanceof Map) {
			
			params.aq.each { key, value ->
				queryParams["aq."+key] = value;
				activeFilters["aq."+key] = value;
				if(!(key ==~ /action|controller|sort|fl|start|rows|webaddress/) && value ) {
					if(i++ == 0) {
						aq = key + ': ('+value+')';
					} else {
						aq = aq + " AND " + key + ': ('+value+')';
					}
				}
			}
		}
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String lastRevisedStartDate = '';
		String lastRevisedEndDate = '';
		if(params.daterangepicker_start) {
			Date s = DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']);
			Calendar cal = Calendar.getInstance(); // locale-specific
			cal.setTime(s)
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MINUTE, 0);
			s = new Date(cal.getTimeInMillis())
			//StringWriter str1 = new StringWriter();
			lastRevisedStartDate = dateFormatter.format(s)
			//DateUtil.formatDate(s, cal, str1)
			//println str1
			//lastRevisedStartDate = str1;
			
		}
		
		if(params.daterangepicker_end) {
			Calendar cal = Calendar.getInstance(); // locale-specific
			Date e = DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']);
			cal.setTime(e)
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.MINUTE, 59);
			e = new Date(cal.getTimeInMillis())
//			StringWriter str2 = new StringWriter();
//			DateUtil.formatDate(e, cal, str2)
//			println str2
			lastRevisedEndDate = dateFormatter.format(e);
		}
		
		if(lastRevisedStartDate && lastRevisedEndDate) {
			if(i > 0) aq += " AND";
			aq += " lastrevised:["+lastRevisedStartDate+" TO "+lastRevisedEndDate+"]";
			queryParams['daterangepicker_start'] = params.daterangepicker_start;
			queryParams['daterangepicker_end'] = params.daterangepicker_end;
			activeFilters['daterangepicker_start'] = params.daterangepicker_start;
			activeFilters['daterangepicker_end'] = params.daterangepicker_end;
			
		} else if(lastRevisedStartDate) {
			if(i > 0) aq += " AND";
			//String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
			aq += " lastrevised:["+lastRevisedStartDate+" TO NOW]";
			queryParams['daterangepicker_start'] = params.daterangepicker_start;
			activeFilters['daterangepicker_start'] = params.daterangepicker_endparams.daterangepicker_end;
		} else if (lastRevisedEndDate) {
			if(i > 0) aq += " AND";
			//String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
			aq += " lastrevised:[ * "+lastRevisedEndDate+"]";
			queryParams['daterangepicker_end'] = params.daterangepicker_end;
			activeFilters['daterangepicker_end'] = params.daterangepicker_end;
		}
		
		if(params.query && aq) {
			params.query = params.query + " AND "+aq
		} else if (aq) {
			params.query = aq;
		}
		
	
		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		//options
		paramsList.add('start', offset);
		paramsList.add('rows', max);
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase(); 
		if(isValidSortParam(sort)) {
			if(sort.indexOf(' desc') == -1) {
				sort += " desc";
			}
			paramsList.add('sort', sort);
		}
		
		paramsList.add('fl', params['fl']?:"id");
		
		//Facets
		/*params["facet.field"] = params["facet.field"] ?: searchFieldsConfig.TAG;
		paramsList.add('facet.field', params["facet.field"]);
		paramsList.add('facet', "true");
		params["facet.limit"] = params["facet.limit"] ?: 50;
		paramsList.add('facet.limit', params["facet.limit"]);
		params["facet.offset"] = params["facet.offset"] ?: 0;
		paramsList.add('facet.offset', params["facet.offset"]);
		paramsList.add('facet.mincount', "1");
		*/

		//Filters
		if(params.sGroup) {
			params.sGroup = params.sGroup.toLong()
			def groupId = getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			} else{
				paramsList.add('fq', searchFieldsConfig.SGROUP+":"+groupId);
				queryParams["groupId"] = groupId
				activeFilters["sGroup"] = groupId
			}
		}
		
		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
			paramsList.add('fq', searchFieldsConfig.HABITAT+":"+params.habitat);
			queryParams["habitat"] = params.habitat
			activeFilters["habitat"] = params.habitat
		}
		if(params.tag) {
			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'observation'
			activeFilters["tag"] = params.tag
		}
		if(params.user){
			paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
			queryParams["user"] = params.user.toLong()
			activeFilters["user"] = params.user.toLong()
		}
		if(params.name && (params.name != grailsApplication.config.speciesPortal.group.ALL)) {
			paramsList.add('fq', searchFieldsConfig.MAX_VOTED_SPECIES_NAME+":"+params.name);
			queryParams["name"] = params.name
			activeFilters["name"] = params.name
		}
		if(params.isFlagged && params.isFlagged.toBoolean()){
			paramsList.add('fq', searchFieldsConfig.ISFLAGGED+":"+params.isFlagged.toBoolean());
			activeFilters["isFlagged"] = params.isFlagged.toBoolean()
		}

		if(params.isChecklistOnly && params.isChecklistOnly.toBoolean()){
			paramsList.add('fq', searchFieldsConfig.IS_CHECKLIST+":"+params.isChecklistOnly.toBoolean());
			activeFilters["isChecklistOnly"] = params.isChecklistOnly.toBoolean()
		}

		
		if(params.bounds){
			def bounds = params.bounds.split(",")
			 def swLat = bounds[0]
			 def swLon = bounds[1]
			 def neLat = bounds[2]
			 def neLon = bounds[3]
			 paramsList.add('fq', searchFieldsConfig.LATLONG+":["+swLat+","+swLon+" TO "+neLat+","+neLon+"]");
			 activeFilters["bounds"] = params.bounds
		}
		
		if(params.uGroup) {
			if(params.uGroup == "THIS_GROUP") {
				String uGroup = params.webaddress
				if(uGroup) {
					paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
				}
				queryParams["uGroup"] = params.uGroup
				activeFilters["uGroup"] = params.uGroup
			} else {
				queryParams["uGroup"] = "ALL"
				activeFilters["uGroup"] = "ALL"
			}
		}
		log.debug "Along with faceting params : "+paramsList;
		
		if(isMapView) {
			//query = mapViewQuery + filterQuery + orderByClause
		} else {
			//query += filterQuery + orderByClause
			queryParams["max"] = max
			queryParams["offset"] = offset
		}

		List<Observation> instanceList = new ArrayList<Observation>();
		def totalObservationIdList = [];
		def facetResults = [:], responseHeader
		long noOfResults = 0;
		long checklistCount = 0
		if(paramsList) {
			def queryResponse = observationsSearchService.search(paramsList);
			
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				def instance = Observation.read(Long.parseLong(doc.getFieldValue("id")+""));
				if(instance) {
					totalObservationIdList.add(Long.parseLong(doc.getFieldValue("id")+""));
					instanceList.add(instance);
					if(instance.isChecklist){
						checklistCount++ 
					}
				}
			}
			
			/*List facets = queryResponse.getFacetField(params["facet.field"]).getValues()
			
			facets.each {
				facetResults.put(it.getName(),it.getCount());
			}*/
			
			responseHeader = queryResponse?.responseHeader;
			noOfResults = queryResponse.getResults().getNumFound()
		}
		/*if(responseHeader?.params?.q == "*:*") {
			responseHeader.params.remove('q');
		}*/
		
		return [responseHeader:responseHeader, observationInstanceList:instanceList, resultType:'observation', instanceTotal:noOfResults, checklistCount:checklistCount, observationCount: noOfResults-checklistCount , queryParams:queryParams, activeFilters:activeFilters, tags:facetResults, totalObservationIdList:totalObservationIdList]
	}
	
	private boolean isValidSortParam(String sortParam) {
		if(sortParam.equalsIgnoreCase("score") || sortParam.equalsIgnoreCase("visitCount")  || sortParam.equalsIgnoreCase("createdOn") || sortParam.equalsIgnoreCase("lastRevised") )
			return true;
		return false;
	}
	
	def setUserGroups(Observation observationInstance, List userGroupIds) {
		if(!observationInstance) return
		
		def obvInUserGroups = observationInstance.userGroups.collect { it.id + ""}
		def toRemainInUserGroups =  obvInUserGroups.intersect(userGroupIds);
		if(userGroupIds.size() == 0) {
			userGroupService.removeObservationFromUserGroups(observationInstance, obvInUserGroups);
		} else {
			userGroupIds.removeAll(toRemainInUserGroups)
			userGroupService.postObservationtoUserGroups(observationInstance, userGroupIds);
			obvInUserGroups.removeAll(toRemainInUserGroups)
			userGroupService.removeObservationFromUserGroups(observationInstance, obvInUserGroups);
		}
	}
	
	File getUniqueFile(File root, String fileName){
		File imageFile = new File(root, fileName);
		
		if(!imageFile.exists()) {
			return imageFile
		}
		
		int i = 0;
		int duplicateFileLimit = 20
		while(++i < duplicateFileLimit){
			def newFileName = "" + i + "_" + fileName
			File newImageFile = new File(root, newFileName);
			
			if(!newImageFile.exists()){
				return newImageFile
			}
			
		}
		log.error "Too many duplicate files $fileName"
		return imageFile
	}
	
	def addRecoComment(commentHolder, rootHolder, recoComment){
		recoComment = (recoComment?.trim()?.length() > 0)? recoComment.trim():null;
		if(recoComment){
			def m = [author:springSecurityService.currentUser, commentBody:recoComment, commentHolderId:commentHolder.id, \
						commentHolderType:commentHolder.class.getCanonicalName(), rootHolderId:rootHolder.id, rootHolderType:rootHolder.class.getCanonicalName()]
			commentService.addComment(m);
		}
	}
	
	def nameTerms(params) {
		List result = new ArrayList();
		
		def queryResponse = observationsSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Observations"]);
		}
		return result;
	} 

	def delete(params){
		def messageCode, url, label = Utils.getTitleCase(params.controller)
		def messageArgs = [label, params.id]
		def observationInstance = Observation.get(params.id.toLong())
		if (observationInstance && SUserService.ifOwns(observationInstance.author)) {
			def mailType = observationInstance.instanceOf(Observation) ? OBSERVATION_DELETED : CHECKLIST_DELETED
			try {
				observationInstance.isDeleted = true;
				observationInstance.save(flush: true)
				sendNotificationMail(mailType, observationInstance, null, params.webaddress);
				observationsSearchService.delete(observationInstance.id);
				messageCode = 'default.deleted.message'
				url = generateLink(params.controller, 'list', [])
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				messageCode = 'default.not.deleted.message'
				url = generateLink(params.controller, 'show', [id: params.id])
			}
		}
		else {
			messageCode = 'default.not.found.message'
			url = generateLink(params.controller, 'list', [])
		}
		
		return [url:url, messageCode:messageCode, messageArgs: messageArgs]
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	public sendNotificationMail(String notificationType, def obv, request, String userGroupWebaddress, ActivityFeed feedInstance=null){
		def conf = SpringSecurityUtils.securityConfig
		log.debug "Sending email"
		try {
			
		def targetController =  getTargetController(obv)//obv.getClass().getCanonicalName().split('\\.')[-1]
		targetController = targetController.replaceFirst(targetController[0], targetController[0].toLowerCase());
		def obvUrl, domain
	
        try {
		    request = (request) ?:(WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest())
        } catch(IllegalStateException e) {
            log.error e.getMessage();
        }
		if(request){
			 obvUrl = generateLink(targetController, "show", ["id": obv.id], request)
			 domain = Utils.getDomainName(request)
		}

		def templateMap = [obvUrl:obvUrl, domain:domain]

		def mailSubject = ""
		def bodyContent = ""
		String htmlContent = ""
		String bodyView = '';
		def replyTo = conf.ui.notification.emailReplyTo;
		Set toUsers = []
		//Set bcc = ["xyz@xyz.com"];
		//def activityModel = ['feedInstance':feedInstance, 'feedType':ActivityFeedService.GENERIC, 'feedPermission':ActivityFeedService.READ_ONLY, feedHomeObject:null]
		
		switch ( notificationType ) {
			case OBSERVATION_ADDED:
				mailSubject = conf.ui.addObservation.emailSubject
				bodyContent = conf.ui.addObservation.emailBody
				toUsers.add(getOwner(obv))
				break

			case activityFeedService.CHECKLIST_CREATED:
				mailSubject = conf.ui.addChecklist.emailSubject
				bodyContent = conf.ui.addChecklist.emailBody
				toUsers.add(getOwner(obv))
				break

				
			case OBSERVATION_FLAGGED :
				mailSubject = "Observation flagged"
				bodyContent = conf.ui.observationFlagged.emailBody
				templateMap["currentUser"] = springSecurityService.currentUser
				//replyTo = templateMap["currentUser"].email
				toUsers.add(getOwner(obv))
				break

			case OBSERVATION_DELETED :
				mailSubject = conf.ui.observationDeleted.emailSubject
				bodyContent = conf.ui.observationDeleted.emailBody
				templateMap["currentUser"] = springSecurityService.currentUser
				//replyTo = templateMap["currentUser"].email
				toUsers.add(getOwner(obv))
				break
				
			case CHECKLIST_DELETED :
				mailSubject = conf.ui.checklistDeleted.emailSubject
				bodyContent = conf.ui.checklistDeleted.emailBody
				templateMap["currentUser"] = springSecurityService.currentUser
				//replyTo = templateMap["currentUser"].email
				toUsers.add(getOwner(obv))
				break


			case SPECIES_RECOMMENDED :
				bodyView = "/emailtemplates/addRecommendation"
				mailSubject = conf.ui.addRecommendationVote.emailSubject
				templateMap['actor'] = feedInstance.author;
				templateMap["actorProfileUrl"] = generateLink("SUser", "show", ["id": feedInstance.author.id], request)
				templateMap["actorIconUrl"] = feedInstance.author.profilePicture(ImageType.SMALL)
				templateMap["actorName"] = feedInstance.author.name
				templateMap["activity"] = activityFeedService.getContextInfo(feedInstance, [webaddress:userGroupWebaddress])
				templateMap["userGroupWebaddress"] = userGroupWebaddress
				//mailSubject = feedInstance.author.name +" : "+ templateMap["activity"].activityTitle.replaceAll(/<.*?>/, '')
				//replyTo = templateMap["currentUser"].email
				toUsers.addAll(getParticipants(obv))
				break

			case SPECIES_AGREED_ON:
				bodyView = "/emailtemplates/addRecommendation"
				mailSubject = conf.ui.addRecommendationVote.emailSubject
				templateMap['actor'] = feedInstance.author;
				templateMap["actorProfileUrl"] = generateLink("SUser", "show", ["id": feedInstance.author.id], request)
				templateMap["actorIconUrl"] = feedInstance.author.profilePicture(ImageType.SMALL)
				templateMap["actorName"] = feedInstance.author.name
				templateMap["userGroupWebaddress"] = userGroupWebaddress
				templateMap["activity"] = activityFeedService.getContextInfo(feedInstance, [webaddress:userGroupWebaddress])
				//mailSubject = feedInstance.author.name +" : "+ templateMap["activity"].activityTitle.replaceAll(/<.*?>/, '')
				//replyTo = templateMap["currentUser"].email
				toUsers.addAll(getParticipants(obv))
				break
				
			case activityFeedService.RECOMMENDATION_REMOVED:
				bodyView = "/emailtemplates/addRecommendation"
				mailSubject = conf.ui.removeRecommendationVote.emailSubject
				templateMap['actor'] = feedInstance.author;
				templateMap["userGroupWebaddress"] = userGroupWebaddress
				templateMap["activity"] = activityFeedService.getContextInfo(feedInstance, [webaddress:userGroupWebaddress])
				toUsers.addAll(getParticipants(obv))
				break
			
			case activityFeedService.OBSERVATION_POSTED_ON_GROUP:
				mailSubject = conf.ui.observationPostedToGroup.emailSubject
				bodyContent = conf.ui.observationPostedToGroup.emailBody
				templateMap["actionObject"] = 'observation'
				templateMap["actorProfileUrl"] = generateLink("SUser", "show", ["id": feedInstance.author.id], request)
				templateMap["actorName"] = feedInstance.author.name
				templateMap["groupNameWithlink"] = activityFeedService.getUserGroupHyperLink(activityFeedService.getDomainObject(feedInstance.activityHolderType, feedInstance.activityHolderId))
				toUsers.addAll(getParticipants(obv))
				break

			case activityFeedService.OBSERVATION_REMOVED_FROM_GROUP:
				mailSubject = conf.ui.observationRemovedFromGroup.emailSubject
				bodyContent = conf.ui.observationRemovedFromGroup.emailBody
				templateMap["actionObject"] = 'observation'
				templateMap["actorProfileUrl"] = generateLink("SUser", "show", ["id": feedInstance.author.id], request)
				templateMap["actorName"] = feedInstance.author.name
				templateMap["groupNameWithlink"] = activityFeedService.getUserGroupHyperLink(activityFeedService.getDomainObject(feedInstance.activityHolderType, feedInstance.activityHolderId))
				toUsers.addAll(getParticipants(obv))
				break

			case activityFeedService.CHECKLIST_POSTED_ON_GROUP:
				mailSubject = conf.ui.checklistPostedToGroup.emailSubject
				bodyContent = conf.ui.checklistPostedToGroup.emailBody
				templateMap["actionObject"] = 'checklist'
				templateMap["actorProfileUrl"] = generateLink("SUser", "show", ["id": feedInstance.author.id], request)
				templateMap["actorName"] = feedInstance.author.name
				templateMap["groupNameWithlink"] = activityFeedService.getUserGroupHyperLink(activityFeedService.getDomainObject(feedInstance.activityHolderType, feedInstance.activityHolderId))
				toUsers.addAll(getParticipants(obv))
				break

			case activityFeedService.CHECKLIST_REMOVED_FROM_GROUP:
				mailSubject = conf.ui.checklistRemovedFromGroup.emailSubject
				bodyContent = conf.ui.checklistRemovedFromGroup.emailBody
				templateMap["actionObject"] = 'checklist'
				templateMap["actorProfileUrl"] = generateLink("SUser", "show", ["id": feedInstance.author.id], request)
				templateMap["actorName"] = feedInstance.author.name
				templateMap["groupNameWithlink"] = activityFeedService.getUserGroupHyperLink(activityFeedService.getDomainObject(feedInstance.activityHolderType, feedInstance.activityHolderId))
				toUsers.addAll(getParticipants(obv))
				break


			case activityFeedService.COMMENT_ADDED:				
				bodyView = "/emailtemplates/addComment"
				templateMap['actor'] = feedInstance.author;
				templateMap["actorProfileUrl"] = generateLink("SUser", "show", ["id": feedInstance.author.id], request)
				templateMap["actorIconUrl"] = feedInstance.author.profilePicture(ImageType.SMALL)
				templateMap["actorName"] = feedInstance.author.name
				templateMap["userGroupWebaddress"] = userGroupWebaddress
				templateMap["activity"] = activityFeedService.getContextInfo(feedInstance, [webaddress:userGroupWebaddress])
				templateMap['domainObjectTitle'] = getTitle(activityFeedService.getDomainObject(feedInstance.rootHolderType, feedInstance.rootHolderId))
				templateMap['domainObjectType'] = feedInstance.rootHolderType.split('\\.')[-1].toLowerCase()
				mailSubject = "New comment in ${templateMap['domainObjectType']}"
				toUsers.addAll(getParticipants(obv))
				break;
				
			case SPECIES_REMOVE_COMMENT:
				mailSubject = conf.ui.removeComment.emailSubject
				bodyContent = conf.ui.removeComment.emailBody
				toUsers.add(getOwner(obv))
				break;
			
			case DOWNLOAD_REQUEST:
				mailSubject = conf.ui.downloadRequest.emailSubject
				bodyContent = conf.ui.downloadRequest.emailBody
				templateMap['domain'] = "India Biodiversity Portal"
				toUsers.add(getOwner(obv))
				templateMap['userProfileUrl'] = ObvUtilService.createHardLink('user', 'show', obv.author.id)
				break;
			
			case activityFeedService.DOCUMENT_CREATED:
				mailSubject = conf.ui.addDocument.emailSubject
				bodyContent = conf.ui.addDocument.emailBody
				toUsers.add(getOwner(obv))
				break
				
			default:
				log.debug "invalid notification type"
		}
		
		toUsers.eachWithIndex { toUser, index ->
			if(toUser) {
				templateMap['username'] = toUser.name.capitalize();
				if(request){
					templateMap['userProfileUrl'] = generateLink("SUser", "show", ["id": toUser.id], request)
				}
		        if ( Environment.getCurrent().getName().equalsIgnoreCase("pamba")) {
				//if ( Environment.getCurrent().getName().equalsIgnoreCase("development")) {
		            log.debug "Sending email to ${toUser}"
					mailService.sendMail {
						to toUser.email
						if(index == 0) {
							//bcc "prabha.prabhakar@gmail.com", "sravanthi@strandls.com", "thomas.vee@gmail.com", "sandeept@strandls.com"
                            bcc grailsApplication.config.speciesPortal.app.notifiers_bcc.toArray()
							//bcc "sravanthi@strandls.com"
						}
						from conf.ui.notification.emailFrom
						//replyTo replyTo
						subject mailSubject
						if(bodyView) {
							body (view:bodyView, model:templateMap)
						}
						else if(htmlContent) {
							htmlContent = Utils.getPremailer(grailsApplication.config.grails.serverURL, htmlContent)
							html htmlContent
						} else if(bodyContent) {
							if (bodyContent.contains('$')) {
								bodyContent = evaluate(bodyContent, templateMap)
							}
							html bodyContent
						}
					}
				}
			}
		}
		
		} catch (e) {
			log.error "Error sending email $e.message"
			e.printStackTrace();
		}
	}

	public String generateLink( String controller, String action, linkParams, request=null) {
		request = (request) ?:(WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest())
		userGroupService.userGroupBasedLink(base: Utils.getDomainServerUrl(request),
				controller:controller, action: action,
				params: linkParams)
	}
	
	private List getParticipants(observation) {
		List participants = [];
		//def result = ActivityFeed.findAllByRootHolderIdAndRootHolderType(observation.id, observation.class.getCanonicalName())*.author.unique()
		def result = Follow.getFollowers(observation)
		result.each { user ->
			if(user.sendNotification && !participants.contains(user)){
				participants << user
			}
		}
		return participants;
	}
	
	private SUser getOwner(observation) {
		def author = null;
		if(observation.metaClass.hasProperty(observation, 'author')) {
			author = observation.author;
			if(!author.sendNotification) {
				author = null;
			}
		}
		return author;
	}
	
	private String getTitle(observation) {
		if(observation.metaClass.hasProperty(observation, 'title')) {
			return observation.title
		} else if(observation.metaClass.hasProperty(observation, 'name')) {
			return observation.name
		} else
			return null
	}
	
	def addAnnotation(params, Observation obv){
		def ann = new Annotation(params)
			obv.addToAnnotations(ann);
		}

	def locations(params) {
		List result = new ArrayList();

        if(!params.term) return result;

		NamedList paramsList = new NamedList();
/*        paramsList.add('fl', 'location_exact');
        if(springSecurityService.currentUser){
            paramsList.add('q', "author_id:"+springSecurityService.currentUser.id.toLong())
        } else {
            paramsList.add('q', "*:*");
        }
        paramsList.add('facet', "true");
        paramsList.add('facet.field', "location_exact")
        paramsList.add('facet.mincount', "1")
        paramsList.add('facet.prefix', params.term?:'*');
        paramsList.add('facet.limit', 5);

        def facetResults = []
		if(paramsList) {
			def queryResponse = observationsSearchService.search(paramsList);
			List facets = queryResponse.getFacetField('location_exact').getValues()
			
			facets.each {
			    facetResults.add([value:it.getName(), label:it.getName()+'('+it.getCount()+')',  "category":"My Locations"]);
			}
			
		}
*/

		def searchFieldsConfig = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.speciesPortal.searchFields
        String query = ""
	    if(springSecurityService.currentUser){
            query += searchFieldsConfig.AUTHOR_ID+":"+springSecurityService.currentUser.id.toLong()+" AND "
        } else {
           //query += "*:*";
         }
        query += searchFieldsConfig.LOCATION_EXACT+":"+params.term+'*'?:'*';

        paramsList.add("q", query);
        paramsList.add("fl", searchFieldsConfig.LOCATION_EXACT+','+searchFieldsConfig.LATLONG+','+searchFieldsConfig.TOPOLOGY);
        paramsList.add("start", 0);
        paramsList.add("rows", 20);
        paramsList.add("sort", searchFieldsConfig.UPDATED_ON+' desc,'+searchFieldsConfig.SCORE + " desc ");

        def results = [];
        Map temp = [:]
        if(paramsList) {
			def queryResponse = observationsSearchService.search(paramsList);
	
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
                if(results.size() >= 5) break;
                if(!temp[doc.getFieldValue(searchFieldsConfig.LOCATION_EXACT)]) {
                    results.add(['location':doc.getFieldValue(searchFieldsConfig.LOCATION_EXACT), 'topology':doc.getFieldValue(searchFieldsConfig.TOPOLOGY), 'category':'My Locations']);
                    temp[doc.getFieldValue(searchFieldsConfig.LOCATION_EXACT)] = true;
                }
			}
	    }
		
		return results
	} 

}
