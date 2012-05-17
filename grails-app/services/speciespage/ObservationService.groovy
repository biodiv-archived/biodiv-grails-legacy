package speciespage

import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine

import org.grails.taggable.Tag;
import org.grails.taggable.TagLink;

import java.util.Date;
import java.util.Map;

import species.Resource;
import species.Habitat;
import species.utils.Utils;
import species.Resource.ResourceType;
import species.TaxonomyDefinition;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import species.participation.ObservationFlag.FlagType
import species.participation.RecommendationVote.ConfidenceType;
import species.sourcehandler.XMLConverter;
import species.utils.Utils;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

class ObservationService {

	static transactional = false

	def recommendationService;
	def grailsApplication;
	def dataSource;

	/**
	 * 
	 * @param params
	 * @return
	 */
	Observation createObservation(params) {
		log.info "Creating observations from params : "+params
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
		if(params.author)  {
			observation.author = params.author;
		}

		if(params.url) {
			observation.url = params.url;
		}
		observation.group = SpeciesGroup.get(params.group_id);
		observation.notes = params.notes;
		observation.observedOn = parseDate(params.observedOn);
		observation.placeName = params.place_name;
		observation.reverseGeocodedName = params.reverse_geocoded_name;
		observation.location = 'POINT(' + params.longitude + ' ' + params.latitude + ')'
		observation.latitude = Float.parseFloat(params.latitude);
		observation.longitude = Float.parseFloat(params.longitude);
		observation.locationAccuracy = params.location_accuracy;
		observation.geoPrivacy = false;
		observation.habitat = Habitat.get(params.habitat_id);

		def resourcesXML = createResourcesXML(params);
		def resources = saveResources(observation, resourcesXML);

		resources.each { resource ->
			observation.addToResource(resource);
		}
	}
	
	/**
	*
	* @param params
	* @return
	*/
	Map getRelatedObservations(params) {
	   log.debug params;
	   def max = Math.min(params.limit ? params.limit.toInteger() : 9, 100)

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
	   }else{
		   relatedObv = getRelatedObservation(params.filterProperty, params.id.toLong(), max, offset)
	   }
	   return [relatedObv:relatedObv, max:max]
   }

	
	
	List getRelatedObservation(String property, long obvId, int limit, long offset){
		def propertyValue = Observation.read(obvId)[property]
		def query = "from Observation as obv where obv." + property + " like :propertyValuee and obv.id != :parentObvId and obv.isDeleted = :isDeleted order by obv.createdOn desc"
		def obvs = Observation.findAll(query, [propertyValue:propertyValue, parentObvId:obvId, max:limit, offset:offset, isDeleted:false])
		def result = [];
		obvs.each {
			result.add(['observation':it, 'title':it.maxVotedSpeciesName]);
		}
		return result
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	Map getRelatedObservationBySpeciesName(long obvId, int limit, long offset){
		List<String> speciesNames = getSpeciesNames(obvId)
		log.debug speciesNames
		log.debug speciesNames.getClass();
		return getRelatedObservationBySpeciesNames(speciesNames, obvId, limit, offset)
	}
	/**
	 * 
	 * @param params
	 * @return
	 */
	Map getRelatedObservationByUser(long userId, int limit, long offset, String sort){
		//getting count
		def queryParams = [isDeleted:false]
		def countQuery = "select count(*) from Observation obv where obv.author.id = :userId and obv.isDeleted = :isDeleted "
		queryParams["userId"] = userId
		queryParams["isDeleted"] = false;
		def count = Observation.executeQuery(countQuery, queryParams)


		//getting observations
		def query = "from Observation obv where obv.author.id = :userId and obv.isDeleted = :isDeleted "
		def orderByClause = "order by obv." + (sort ? sort : "createdOn") +  " desc"
		query += orderByClause

		queryParams["max"] = limit
		queryParams["offset"] = offset

		def observations = Observation.findAll(query, queryParams);
		def result = [];
		observations.each {
			result.add(['observation':it, 'title':it.maxVotedSpeciesName]);
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
			result.add(['observation':it, 'title':it.maxVotedSpeciesName]);
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
	List<String> getSpeciesNames(obvId){
		return Observation.read(obvId).getSpecies();
	}

	/**
	 * 
	 * @param speciesName
	 * @param params
	 * @return
	 */
	Map getRelatedObservationBySpeciesNames(List<String> speciesNames, long obvId, int limit, long offset){
		if(!speciesNames) {
			return ["observations":[], "count":0];
		}
		def recIds = Recommendation.executeQuery("select rec.id from Recommendation as rec where rec.name in (:speciesNames)", [speciesNames:speciesNames]);
		def countQuery = "select count(*) from RecommendationVote recVote where recVote.recommendation.id in (:recIds) and recVote.observation.id != :parentObv and recVote.observation.isDeleted = :isDeleted"
		def countParams = [parentObv:obvId, recIds:recIds, isDeleted:false]
		def count = RecommendationVote.executeQuery(countQuery, countParams)
		def query = "select recVote.observation as observation from RecommendationVote recVote where recVote.recommendation.id in (:recIds) and recVote.observation.id != :parentObv and recVote.observation.isDeleted = :isDeleted order by recVote.votedOn desc "
		def m = [parentObv:obvId, recIds:recIds, max:limit, offset:offset, isDeleted:false]
		def observations = RecommendationVote.executeQuery(query, m);
		def result = [];
		observations.each {
			result.add(['observation':it, 'title':it.maxVotedSpeciesName]);
		}
		return ["observations":result, "count":count[0]]
	}

	/**
	 * 
	 * @return
	 */
	//	static List createUrlList(observations){
	//		List urlList = []
	//		for(obv in observations){
	//			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
	//			def image = obv.mainImage()
	//			def imagePath = image.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, config.speciesPortal.resources.images.galleryThumbnail.suffix)
	//			def imageLink = config.speciesPortal.observations.serverURL + "/" +  imagePath
	//			urlList.add(["obvId":obv.id, "imageLink":imageLink, "imageTitle": image.fileName])
	//		}
	//		return urlList
	//	}

	static List createUrlList2(observations){
		List urlList = []
		for(param in observations){
			def obv = param['observation']
			def title = param['title']
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			def image = obv.mainImage()
			def imagePath = image.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, config.speciesPortal.resources.images.thumbnail.suffix)
			def imageLink = config.speciesPortal.observations.serverURL +  imagePath
			urlList.add(["obvId":obv.id, "imageLink":imageLink, "imageTitle": title])
		}
		return urlList
	}
	
	private Recommendation getRecommendation(recoName, canName) {
		def reco, taxonConcept;
		if(canName) {
			//findBy returns first...assuming taxon concepts wont hv same canonical name and different rank
			canName = Utils.cleanName(canName);
			reco = Recommendation.findByNameIlike(canName);
			log.debug "Found taxonConcept : "+taxonConcept;
			log.debug "Found reco : "+reco;
			if(!reco) {
				taxonConcept = TaxonomyDefinition.findByCanonicalFormIlike(canName);
				if(taxonConcept) {
					log.debug "Resolving recoName to canName : "+taxonConcept.canonicalForm
					reco = new Recommendation(name:taxonConcept.canonicalForm, taxonConcept:taxonConcept);
					if(!recommendationService.save(reco)) {
						reco = null;
					}
					return reco;
				} else {
					log.error "Given taxonomy canonical name is invalid"
				}
			}
		}

		if(recoName) {
			recoName = Utils.cleanName(recoName);
			def c = Recommendation.createCriteria();
			def result = c.list {
				ilike('name', recoName);
				//(taxonConcept) ? eq('taxonConcept', taxonConcept) : isNull('taxonConcept');
			}
			reco = result?result[0]:null;
		}

		if(!reco) {
			reco = new Recommendation(name:recoName, taxonConcept:taxonConcept);
			if(!recommendationService.save(reco)) {
				reco = null;
			}
		}

		return reco;
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
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();
		def resources = builder.createNode("resources");
		Node images = new Node(resources, "images");
		String uploadDir =  grailsApplication.config.speciesPortal.observations.rootDir;
		List files = [];
		List titles = [];
		List licenses = [];
		params.each { key, val ->
			int index = -1;
			if(key.startsWith('file_')) {
				index = Integer.parseInt(key.substring(key.lastIndexOf('_')+1));

			}
			if(index != -1) {
				files.add(val);
				titles.add(params.get('title_'+index));
				licenses.add(params.get('license_'+index));
			}
		}
		files.eachWithIndex { file, key ->
			Node image = new Node(images, "image");
			if(file) {
				File f = new File(uploadDir, file);
				new Node(image, "fileName", f.absolutePath);
				//new Node(image, "source", imageData.get("source"));
				new Node(image, "caption", titles.getAt(key));
				new Node(image, "contributor", params.author.username);
				new Node(image, "license", licenses.getAt(key));
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
		def rows = sql.rows("select count(*) as count from observation_locations as g1, observation_locations as g2 where ROUND(ST_Distance_Sphere(g1.st_geomfromtext, g2.st_geomfromtext)/1000) < :maxRadius and g2.is_deleted = false and g1.id = :observationId and g1.id <> g2.id", [observationId: Long.parseLong(observationId), maxRadius:maxRadius]);
		def totalResultCount = Math.min(rows[0].getProperty("count"), maxObvs);
		limit = Math.min(limit, maxObvs - offset); 
		def resultSet = sql.rows("select g2.id,  ROUND(ST_Distance_Sphere(g1.st_geomfromtext, g2.st_geomfromtext)/1000) as distance from observation_locations as g1, observation_locations as g2 where  ROUND(ST_Distance_Sphere(g1.st_geomfromtext, g2.st_geomfromtext)/1000) < :maxRadius and g2.is_deleted = false and g1.id = :observationId and g1.id <> g2.id order by ST_Distance(g1.st_geomfromtext, g2.st_geomfromtext) limit :max offset :offset", [observationId: Long.parseLong(observationId), maxRadius:maxRadius, max:limit, offset:offset])

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
	
	protected Map getTagsFromObservation(obvIds){
		int tagsLimit = 30;
		LinkedHashMap tags = [:]
		if(!obvIds){
			return tags
		}
		
		def sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where tl.tag_ref in " + getIdList(obvIds)  + " and tl.tag_ref = obv.id and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;
		
		sql.rows(query).each{
			tags[it.getProperty("name")] = it.getProperty("obv_count");
		};
		return tags;
	}
	
	Map getFilteredTags(params){
		return getTagsFromObservation(getFilteredObservations(params, -1, -1, true).observationInstanceList.collect{it[0]});
	}
	
	/**
	 * Filter observations by group, habitat, tag, user, species
	 * max: limit results to max: if max = -1 return all results
	 * offset: offset results: if offset = -1 its not passed to the 
	 * executing query
	 */
	Map getFilteredObservations(params, max, offset, isMapView){
		params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = params.habitat.toLong()
		//params.userName = springSecurityService.currentUser.username;

		def query = "select obv from Observation obv where obv.isDeleted = :isDeleted "
		def mapViewQuery = "select obv.id, obv.latitude, obv.longitude from Observation obv where obv.isDeleted = :isDeleted "
		def queryParams = [isDeleted : false]
		def filterQuery = ""
		def activeFilters = [:]

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

		if(params.tag){
			query = "select obv from Observation obv,  TagLink tagLink where obv.isDeleted = :isDeleted "
			mapViewQuery = "select obv.id, obv.latitude, obv.longitude from Observation obv, TagLink tagLink where obv.isDeleted = :isDeleted " 
			filterQuery +=  " and obv.id = tagLink.tagRef and tagLink.type like :tagType and tagLink.tag.name like :tag "

			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'observation'
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
			filterQuery += " and obv.maxVotedSpeciesName like :speciesName "
			queryParams["speciesName"] = params.speciesName
			activeFilters["speciesName"] = params.speciesName
		}
		
		if(params.isFlagged && params.isFlagged.toBoolean()){
			filterQuery += " and obv.flagCount > 0 "
		}
		if(params.bounds){
			def bounds = params.bounds.split(",")

			def swLat = bounds[0]
			def swLon = bounds[1]
			def neLat = bounds[2]
			def neLon = bounds[3]

			filterQuery += " and obv.latitude > " + swLat + " and  obv.latitude < " + neLat + " and obv.longitude > " + swLon + " and obv.longitude < " + neLon
			activeFilters["bounds"] = params.bounds
		}

		def orderByClause = " order by obv." + (params.sort ? params.sort : "lastRevised") +  " desc"
		
		if(isMapView){
			query = mapViewQuery + filterQuery + orderByClause
		}else{
			query += filterQuery + orderByClause
			queryParams["max"] = max
			queryParams["offset"] = offset
		}
		
		
		def observationInstanceList = Observation.executeQuery(query, queryParams)
		return [observationInstanceList:observationInstanceList, queryParams:queryParams, activeFilters:activeFilters]
	}
	
	private Date parseDate(date){
		try {
			return date? Date.parse("dd/MM/yyyy", date):new Date();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	} 

	private String getIdList(l){
		return l.toString().replace("[", "(").replace("]", ")")
	}
	
	
	long getAllObservationsOfUser(SUser user) {
		return (long)Observation.countByAuthorAndIsDeleted(user, false);
	}
	
	long getAllRecommendationsOfUser(SUser user) {
		def result = RecommendationVote.executeQuery("select count(recoVote) from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted", [userId:user.id, isDeleted:false]);
		return (long)result[0];
	}
	
	
	
	Map getIdentificationEmailInfo(m){
		def obv = m.observationInstance;
		def domain = m.domain;
		
		def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
		def obvUrl =  g.createLink(controller: 'observation', action: 'show', absolute: 'true', params:["id": obv.id])
		def templateMap = [obvUrl:obvUrl, domain:domain]
		
		def conf = SpringSecurityUtils.securityConfig
		def mailSubject = conf.ui.askIdentification.emailSubject
		def body = conf.ui.askIdentification.emailBody
		if (body.contains('$')) {
			body = evaluate(body, templateMap)
		}
		return [mailSubject:mailSubject, mailBody:body, observationInstance:obv]
	}
	
	private String evaluate(s, binding) {
		new SimpleTemplateEngine().createTemplate(s).make(binding)
	}
	
	
}
