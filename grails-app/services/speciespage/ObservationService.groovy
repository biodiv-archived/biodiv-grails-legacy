package speciespage

import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine

import org.grails.taggable.TagLink;

import java.util.Date;
import java.util.Map;

import species.Resource;
import species.Habitat;
import species.Language;
import species.utils.Utils;
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

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

class ObservationService {

	static transactional = false

	def recommendationService;
	def observationsSearchService;
	def grailsApplication;
	def dataSource;
	def springSecurityService;
	def curationService;
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
		observation.resource?.clear();
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
		def query = "from Observation as obv where obv." + property + " = :propertyValue and obv.id != :parentObvId and obv.isDeleted = :isDeleted order by obv.createdOn desc"
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
	String getSpeciesNames(obvId){
		return Observation.read(obvId).maxVotedSpeciesName;
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
		def count = Observation.countByMaxVotedRecoAndIsDeleted(parentObv.maxVotedReco, false) - 1;
		def c = Observation.createCriteria();
		def observations = c.list (max: limit, offset: offset) {
			and {
				eq("maxVotedReco", parentObv.maxVotedReco)
				eq("isDeleted", false)
				ne("id", obvId)
			}
			order("lastRevised", "desc")
		}
		
		def result = [];
		observations.each {
			result.add(['observation':it, 'title':it.maxVotedSpeciesName]);
		}
		return ["observations":result, "count":count]
	}

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

	private Map getRecommendation(params){
		def recoName = params.recoName;
		def canName = params.canName;
		def commonName = params.commonName;
		def languageId = Language.getLanguage(params.languageName).id;
		def obv = params.observation?:Observation.get(params.obvId);
		
		Recommendation commonNameReco = findReco(commonName, false, languageId, null);
		Recommendation scientificNameReco = getRecoForScientificName(recoName, canName, commonNameReco);
		
		curationService.add(scientificNameReco, commonNameReco, obv, springSecurityService.currentUser);
		
		//giving priority to scientific name if its available. same will be used in determining species call
		return [mainReco : (scientificNameReco ?:commonNameReco), commonNameReco:commonNameReco];
	}
		
	private Recommendation getRecoForScientificName(String recoName, String canonicalName, Recommendation commonNameReco){
		def reco, taxonConcept;
		
		//first searching by canonical name. this name is present if user select from auto suggest
		if(canonicalName && (canonicalName.trim() != "")){
			return findReco(canonicalName, true, null, taxonConcept);
		}
		
		//searching on whatever user typed in scientific name text box
		if(recoName) {
			return findReco(recoName, true, null, taxonConcept);
		}
		
		//it may possible certain common name may point to species id in that case getting the SN for it
		if(commonNameReco && commonNameReco.taxonConcept){
			TaxonomyDefinition taxOnConcept = commonNameReco.taxonConcept
			return findReco(taxOnConcept.canonicalForm, true, null, taxOnConcept)
		}
		
		return null;
	}
	
	private  Recommendation findReco(name, isScientificName, languageId, taxonConceptForNewReco){
		if(name){
			name = Utils.cleanName(name);
			def c = Recommendation.createCriteria();
			def result = c.list {
				ilike('name', name);
				eq('isScientificName', isScientificName);
			    (languageId) ? eq('languageId', languageId) : isNull('languageId');
			}
			def reco = result?result[0]:null;
			if(!reco) {
				reco = new Recommendation(name:name, taxonConcept:taxonConceptForNewReco, isScientificName:isScientificName, languageId:languageId);
				if(!recommendationService.save(reco)) {
					reco = null;
				}
			}
			return reco;
		}
		return null;
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
	
	Map getRelatedTagsFromObservation(obv){
		int tagsLimit = 30;
		def tagNames = obv.tags
		LinkedHashMap tags = [:]
		if(tagNames.isEmpty()){
			return tags
		}
		
		Sql sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where t.name in " +  getSqlInCluase(tagNames) + " and tl.tag_ref = obv.id and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;
		
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
		String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, observation obv where tl.tag_ref in " + getSqlInCluase(obvIds)  + " and tl.tag_ref = obv.id and obv.is_deleted = false and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;

		sql.rows(query, obvIds).each{
			tags[it.getProperty("name")] = it.getProperty("obv_count");
		};
		return tags;
	}

	Map getFilteredTags(params){
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
	 */
	Map getFilteredObservations(params, max, offset, isMapView) {
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
			filterQuery +=  " and obv.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "

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
			filterQuery += " and obv.maxVotedSpeciesName = :speciesName "
			queryParams["speciesName"] = params.speciesName
			activeFilters["speciesName"] = params.speciesName
		}

		if(params.isFlagged && params.isFlagged.toBoolean()){
			filterQuery += " and obv.flagCount > 0 "
			activeFilters["isFlagged"] = params.isFlagged.toBoolean()
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

		def orderByClause = " order by obv." + (params.sort ? params.sort : "lastRevised") +  " desc, obv.id asc"

		if(isMapView) {
			query = mapViewQuery + filterQuery + orderByClause
		} else {
			query += filterQuery + orderByClause
			activeFilters["max"] = max
			queryParams["max"] = max
			activeFilters["offset"] = offset
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
	
	long getAllObservationsOfUser(SUser user) {
		return (long)Observation.countByAuthorAndIsDeleted(user, false);
	}

	long getAllRecommendationsOfUser(SUser user) {
		def result = RecommendationVote.executeQuery("select count(recoVote) from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted", [userId:user.id, isDeleted:false]);
		return (long)result[0];
	}
	
	List getRecommendationsOfUser(SUser user, int max, long offset) {
		if(max == -1) {
			def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted order by recoVote.votedOn desc", [userId:user.id, isDeleted:false]);
			return recommendationVotesList;
		} else {
			def recommendationVotesList = RecommendationVote.executeQuery("select recoVote from RecommendationVote recoVote where recoVote.author.id = :userId and recoVote.observation.isDeleted = :isDeleted order by recoVote.votedOn desc", [userId:user.id, isDeleted:false], [max:max, offset:offset]);
			return recommendationVotesList;
		}
	}
	
	Map getIdentificationEmailInfo(m, requestObj, unsubscribeUrl){
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
			
			default:
				log.debug "invalid source type"
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
	   def max = Math.min(params.max ? params.int('max') : 9, 100)
	   def offset = params.offset ? params.long('offset') : 0

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
		def queryParams = [isDeleted : false]
		
		def activeFilters = [:]
		
		
		NamedList paramsList = new NamedList();
		
		//params.userName = springSecurityService.currentUser.username;
		
		queryParams["query"] = params.query
		activeFilters["query"] = params.query
		params.query = params.query ?: "";
			
		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		//options
		paramsList.add('start', offset);
		paramsList.add('rows', max);
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase(); 
		if(sort.indexOf(' desc') == -1) {
			sort += " desc";
			
		}
		paramsList.add('sort', sort);
		
		paramsList.add('fl', params['fl']?:"id");
		
		//Facets
		params["facet.field"] = params["facet.field"] ?: searchFieldsConfig.TAG;
		paramsList.add('facet.field', params["facet.field"]);
		paramsList.add('facet', "true");
		params["facet.limit"] = params["facet.limit"] ?: 50;
		paramsList.add('facet.limit', params["facet.limit"]);
		params["facet.offset"] = params["facet.offset"] ?: 0;
		paramsList.add('facet.offset', params["facet.offset"]);
		paramsList.add('facet.mincount', "1");
		
		//Filters
		if(params.sGroup){
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
		if(params.speciesName && (params.speciesName != grailsApplication.config.speciesPortal.group.ALL)) {
			paramsList.add('fq', searchFieldsConfig.MAX_VOTED_SPECIES_NAME+":"+params.speciesName);
			queryParams["speciesName"] = params.speciesName
			activeFilters["speciesName"] = params.speciesName
		}
		if(params.isFlagged && params.isFlagged.toBoolean()){
			paramsList.add('fq', searchFieldsConfig.ISFLAGGED+":"+params.isFlagged.toBoolean());
			activeFilters["isFlagged"] = params.isFlagged.toBoolean()
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
		if(paramsList.get('q')) {
			def queryResponse = observationsSearchService.search(paramsList);
			
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				def instance = Observation.read(Long.parseLong(doc.getFieldValue("id")+""));
				if(instance) {
					totalObservationIdList.add(Long.parseLong(doc.getFieldValue("id")+""));
					instanceList.add(instance);
				}
			}
			
			List facets = queryResponse.getFacetField(params["facet.field"]).getValues()
			
			facets.each {
				facetResults.put(it.getName(),it.getCount());
			}
			
			responseHeader = queryResponse?.responseHeader;
			noOfResults = queryResponse.getResults().getNumFound()
		}
		/*if(responseHeader?.params?.q == "*:*") {
			responseHeader.params.remove('q');
		}*/
		
		
		return [responseHeader:responseHeader, observationInstanceList:instanceList, observationInstanceTotal:noOfResults, queryParams:queryParams, activeFilters:activeFilters, tags:facetResults, totalObservationIdList:totalObservationIdList]
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
}
