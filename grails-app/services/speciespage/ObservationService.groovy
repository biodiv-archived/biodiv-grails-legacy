package speciespage

import groovy.sql.Sql 

import org.grails.taggable.Tag;
import org.grails.taggable.TagLink;

import species.participation.RecommendationVote.ConfidenceType;
import java.util.Date;
import species.Resource;
import species.Habitat;
import species.Resource.ResourceType;
import species.TaxonomyDefinition;
import species.auth.SUser;
import species.groups.SpeciesGroup;
import species.participation.Observation;
import species.participation.Recommendation;
import species.participation.RecommendationVote;
import species.participation.RecommendationVote.ConfidenceType;
import species.sourcehandler.XMLConverter;

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

		
		if(params.author)  {
			observation.author = params.author;
		}

		if(params.url) {
			observation.url = params.url;
		}
		
		observation.group = SpeciesGroup.get(params.group_id);
		observation.notes = params.notes;
		observation.observedOn = params.observedOn?:new Date();
        observation.placeName = params.place_name;
		observation.reverseGeocodedName = params.place_name;
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

		return observation;
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	RecommendationVote getRecommendationVote(params) {
		def observation = Observation.get(params.obvId);
		def reco = getRecommendation(params.recoName, params.canName);
		def author = params.author;
		ConfidenceType confidence = getConfidenceType(params.confidence?:ConfidenceType.CERTAIN.name());
		return getRecommendationVote(observation, reco, author, confidence);
	}

	/**
	 * 
	 * @param observation
	 * @param reco
	 * @param author
	 * @param confidence
	 * @return
	 */
	RecommendationVote getRecommendationVote(Observation observation, Recommendation reco, SUser author, ConfidenceType confidence) {
		return  RecommendationVote.findByAuthorAndRecommendationAndObservation(author, reco, observation);
	}

	/**
	 * 	
	 */
	RecommendationVote createRecommendationVote(params) {
		def observation = params.observation?:Observation.get(params.obvId);
		def reco;
		if(params.recoId) 
			reco = Recommendation.get(params.long('recoId'));
		else
			reco = getRecommendation(params.recoName, params.canName);
		def author = params.author;
		ConfidenceType confidence = getConfidenceType(params.confidence?:ConfidenceType.CERTAIN.name());
		log.debug params;
		return new RecommendationVote(observation:observation, recommendation:reco, author:author, confidence:confidence);
	}
	
	List getRelatedObservation(params){
		def obvId = params.id.toLong()
		def property = params.filterPropery
		def propertyValue = Observation.read(obvId)[property]
		def query = "from Observation as obv where obv." + property + " like :propertyValuee and obv.id != :parentObvId order by obv.createdOn desc"
		def obvs = Observation.findAll(query, [propertyValue:propertyValue, parentObvId:params.id.toLong(), max:params.limit.toInteger(), offset:params.offset.toInteger()])
		return createUrlList(obvs)
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	Map getRelatedObservationBySpeciesName(params){
		def obvId = params.id.toLong()
		List<String> speciesNames = getSpeciesNames(obvId)
		log.debug speciesNames
		log.debug speciesNames.getClass();
		return getRelatedObservationBySpeciesNames(speciesNames, params)
	}
	/**
	 * 
	 * @param params
	 * @return
	 */
	Map getRelatedObservationByUser(params){
		//getting count
		def queryParams = [:]
		def countQuery = "select count(*) from Observation obv where obv.author.username like :userName "
		queryParams["userName"] = SUser.read(params.filterPropertyValue.toInteger()).username
		def count = Observation.executeQuery(countQuery, queryParams)
		
		
		//getting observations
		def query = "from Observation obv where obv.author.username like :userName "
		def orderByClause = "order by obv." + (params.sort ? params.sort : "createdOn") +  " desc"
		query += orderByClause
		
		queryParams["max"] = params.limit.toInteger()
		queryParams["offset"] = params.offset.toInteger()
		
		return ["observations":createUrlList(Observation.findAll(query, queryParams)), "count":count]
	}
//	
//	List getRelatedObservationBySpeciesGroup(params){
//		def obvId = params.id.toLong()
//		def groupId = Observation.read(obvId).group.id
//		log.debug(groupId)
//		def query = "from Observation as obv where obv.group.id = :groupId and obv.id != :parentObvId order by obv.createdOn desc"
//		def obvs = Observation.findAll(query, [groupId:groupId, parentObvId:params.id.toLong(), max:params.limit.toInteger(), offset:params.offset.toInteger()])
//		return createUrlList(obvs)
//	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	List getRelatedObservationBySpeciesGroup(params){
		def groupId = params.filterPropertyValue.toLong()
		log.debug(groupId)
		
		def query = ""
		def obvs = null
		//if filter group is all 
		def groupIds = getSpeciesGroupIds(groupId)
		if(!groupIds) {
			query = "from Observation as obv order by obv.createdOn desc"
			obvs = Observation.findAll(query, [max:params.limit.toInteger(), offset:params.offset.toInteger()])
		}else if(groupIds instanceof List){
			//if group is others
			query = "from Observation as obv where obv.group is null or obv.group.id in (:groupIds) order by obv.createdOn desc"
			obvs = Observation.findAll(query, [groupIds:groupIds, max:params.limit.toInteger(), offset:params.offset.toInteger()])
		}else{
			query = "from Observation as obv where obv.group.id = :groupId order by obv.createdOn desc"
			obvs = Observation.findAll(query, [groupId:groupIds, max:params.limit.toInteger(), offset:params.offset.toInteger()])
		}
		log.debug(" == obv size " + obvs.size())
		return createUrlList(obvs)
	}
	
	/**
	 * 
	 * @param groupId
	 * @return
	 */
	Object getSpeciesGroupIds(groupId){
		def groupName = SpeciesGroup.read(groupId).name
		//if filter group is all
		if(groupName == grailsApplication.config.speciesPortal.group.ALL){
			return null
		}
		return groupId
//		}else if(groupName == grailsApplication.config.speciesPortal.group.OTHERS){
//			//if group is others
//			def groupNameList = ['Animals', 'Arachnids', 'Archaea', 'Bacteria', 'Chromista', 'Viruses', 'Kingdom Protozoa', 'Mullusks', 'Others']
//			def groupIds = SpeciesGroup.executeQuery("select distinct sg.id from SpeciesGroup sg where sg.name is null or sg.name in (:groupNameList)", [groupNameList:groupNameList])
//			return groupIds
//		}else{
//			return groupId
//		}
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
	Map getRelatedObservationBySpeciesNames(List<String> speciesNames, params){
		if(!speciesNames) {
			return [:];
		}
		//println "speciesName  ==== " + speciesName
		//def recId = Recommendation.findByName(speciesName).id
		def recIds = Recommendation.executeQuery("select rec.id from Recommendation as rec where rec.name in (:speciesNames)", [speciesNames:speciesNames]);
		def countQuery = "select count(*) from RecommendationVote recVote where recVote.recommendation.id in (:recIds) and recVote.observation.id != :parentObv"
		def countParams = [parentObv:params.id.toLong(), recIds:recIds]
		def count = RecommendationVote.executeQuery(countQuery, countParams)
		def query = "select recVote.observation from RecommendationVote recVote where recVote.recommendation.id in (:recIds) and recVote.observation.id != :parentObv  order by recVote.votedOn desc "
		def m = [parentObv:params.id.toLong(), recIds:recIds, max:params.limit.toInteger(), offset:params.offset.toInteger()]
		//return createUrlList(RecommendationVote.executeQuery(query, m).unique())
		return ["observations":createUrlList(RecommendationVote.executeQuery(query, m)), "count":count]
	}
	
	/**
	 * 
	 * @return
	 */
	private static List createUrlList(observations){
		List urlList = []
		for(obv in observations){
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			def image = obv.mainImage()
			def imagePath = image.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, config.speciesPortal.resources.images.galleryThumbnail.suffix)
			def imageLink = config.speciesPortal.observations.serverURL + "/" +  imagePath
			urlList.add(["obvId":obv.id, "imageLink":imageLink, "imageTitle": image.fileName])
		}
		return urlList
	}
	
	private static List createUrlList2(observations){
		List urlList = []
		for(param in observations){
			def obv = param['observation'] 
			def title = param['title']
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			def image = obv.mainImage()
			def imagePath = image.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, config.speciesPortal.resources.images.galleryThumbnail.suffix)
			def imageLink = config.speciesPortal.observations.serverURL + "/" +  imagePath
			urlList.add(["obvId":obv.id, "imageLink":imageLink, "imageTitle": title])
		}
		return urlList
	}
	
	private Recommendation getRecommendation(recoName, canName) {
		def reco, taxonConcept;
		if(canName) {
			//findBy returns first...assuming taxon concepts wont hv same canonical name and different rank 
			taxonConcept = TaxonomyDefinition.findByCanonicalFormIlike(canName);
			log.debug "Resolving recoName to canName : "+taxonConcept.canonicalForm
			reco = Recommendation.findByNameIlike(taxonConcept.canonicalForm);
			log.debug "Found taxonConcept : "+taxonConcept;
			log.debug "Found reco : "+reco;
			if(!reco) {
				reco = new Recommendation(name:taxonConcept.canonicalForm, taxonConcept:taxonConcept);
				recommendationService.save(reco);
			}
		}
		
		else if(recoName) {
			def c = Recommendation.createCriteria();
			def result = c.list {
				ilike('name', recoName);
				(taxonConcept) ? eq('taxonConcept', taxonConcept) : isNull('taxonConcept');
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
	
	List findAllTagsSortedByObservationCount(int max){
	
		def tag_ids = TagLink.executeQuery("select tag.id from TagLink group by tag_id order by count(tag_id) desc", [max:max]);
		
		def tags = []
		
		for (tag_id in tag_ids){
			tags.add(Tag.get(tag_id).name);		
		}
		return tags;
	}
	
	Map getNearbyObservations(String observationId, int limit){
		
		def sql =  Sql.newInstance(dataSource);
		
		def resultSet = sql.rows("select g2.id,  ROUND(ST_Distance_Sphere(g1.st_geomfromtext, g2.st_geomfromtext)/1000) as distance from observation_locations as g1, observation_locations as g2 where g1.id = :observationId and g1.id <> g2.id order by ST_Distance(g1.st_geomfromtext, g2.st_geomfromtext) limit :max", [observationId: Integer.parseInt(observationId), max:limit])
		
		def nearbyObservations = []
		
		for (row in resultSet){
			nearbyObservations.add(["observation":Observation.findById(row.getProperty("id")), "title":"Found "+row.getProperty("distance")+" km away"])	
		}
		
		return ["observations":createUrlList2(nearbyObservations)]
	}
	
	Set getAllTagsOfUser(userId){
		List obvs = Observation.findAll("from Observation as obv where obv.author.id = :userId ", [userId :userId]);
		Set tagSet = new HashSet();
		obvs.each{ tagSet.addAll(it.tags) }
		return tagSet;
	}

        /**
         * Filter observations by group, habitat, tag, user, species
         * max: limit results to max: if max = -1 return all results
         * offset: offset results: if offset = -1 its not passed to the 
         * executing query
         */
        Map getFilteredObservations(params, max, offset){
		params.sGroup = (params.sGroup)? params.sGroup : SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = (params.habitat)? params.habitat : Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id
		params.habitat = params.habitat.toLong()
		//params.userName = springSecurityService.currentUser.username;
		
		def query = "select obv from Observation obv "
		def queryParams = [:]
		def filterQuery = ""
                def activeFilters = [:]

		if(params.sGroup){
			params.sGroup = params.sGroup.toLong()
			def groupId = getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			}else{

				filterQuery += " where obv.group.id = :groupId "
				queryParams["groupId"] = groupId
                                activeFilters["sGroup"] = groupId
			}
		}
		
		if(params.tag){
			query = "select obv from Observation obv,  TagLink tagLink "
			(filterQuery == "")? (filterQuery += "  where ") : (filterQuery += "  and ")
			filterQuery +=  " obv.id = tagLink.tagRef and tagLink.type like :tagType and tagLink.tag.name like :tag "
			
			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'observation'
                        activeFilters["tag"] = params.tag
		}
		
		
		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
			(filterQuery == "")? (filterQuery += "  where ") : (filterQuery += "  and ")
			filterQuery += " obv.habitat.id = :habitat " 
			queryParams["habitat"] = params.habitat
                        activeFilters["habitat"] = params.habitat
		}
		
		if(params.userId){
			(filterQuery == "")? (filterQuery += " where ") : (filterQuery += " and ")
			filterQuery += " obv.author.id = :userId "
			queryParams["userId"] = params.userId.toLong()
                        activeFilters["userId"] = params.habitat
		}
		
		if(params.speciesName && (params.speciesName != grailsApplication.config.speciesPortal.group.ALL)){
			(filterQuery == "")? (filterQuery += " where ") : (filterQuery += " and ")
			filterQuery += " obv.maxVotedSpeciesName like :speciesName "
			queryParams["speciesName"] = params.speciesName
		}
                
                if(params.bounds){
                    def bounds = params.bounds.split(",")
                    
                    def swLat = bounds[0]   
                    def swLon = bounds[1]    
                    def neLat = bounds[2]    
                    def neLon = bounds[3]    

		    (filterQuery == "")? (filterQuery += " where ") : (filterQuery += " and ")
		    filterQuery += " obv.latitude > " + swLat + " and  obv.latitude < " + neLat + " and obv.longitude > " + swLon + " and obv.longitude < " + neLon  
		    activeFilters["bounds"] = params.bounds

                }

		def orderByClause = "order by obv." + (params.sort ? params.sort : "createdOn") +  " desc"

		query += filterQuery + orderByClause
	
                if(max != -1)
                    queryParams["max"] = max

                if(offset != -1)        
		    queryParams["offset"] = offset
		
		def observationInstanceList = Observation.executeQuery(query, queryParams)
                
                return [observationInstanceList:observationInstanceList, queryParams:queryParams, activeFilters:activeFilters]    
        }
}
