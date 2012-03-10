package speciespage

import java.util.Date;

import species.Resource;
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
		observation.habitat = params.habitat;
		
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
	
	Map getRelatedObservationBySpeciesName(params){
		def obvId = params.id.toLong()
		def speciesName = getSpeciesName(obvId)
		log.debug speciesName
		return getRelatedObservationBySpeciesName(speciesName, params)
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
			query = "from Observation as obv where obv.group.id in (:groupIds) order by obv.createdOn desc"
			obvs = Observation.findAll(query, [groupIds:groupIds, max:params.limit.toInteger(), offset:params.offset.toInteger()])
		}else{
			query = "from Observation as obv where obv.group.id = :groupId order by obv.createdOn desc"
			obvs = Observation.findAll(query, [groupId:groupIds, max:params.limit.toInteger(), offset:params.offset.toInteger()])
		}
		log.debug(" == obv size " + obvs.size())
		return createUrlList(obvs)
	}
	
	Object getSpeciesGroupIds(groupId){
		def groupName = SpeciesGroup.read(groupId).name
		//if filter group is all
		if(groupName == grailsApplication.config.speciesPortal.group.ALL){
			return null
		}else if(groupName == grailsApplication.config.speciesPortal.group.OTHERS){
			//if group is others
			def groupNameList = ['Animals', 'Arachnids', 'Archaea', 'Bacteria', 'Chromista', 'Viruses', 'Kingdom Protozoa', 'Mullusks', 'Others']
			def groupIds = SpeciesGroup.executeQuery("select distinct sg.id from SpeciesGroup sg where sg.name is null or sg.name in (:groupNameList)", [groupNameList:groupNameList])
			return groupIds
		}else{
			return groupId
		}
	}
	
	
	String getSpeciesName(obvId){
		def speciesList = []
		Observation.read(obvId).recommendationVote.each{speciesList << it.recommendation.name}
		return getMaxRepeatedElementFromList(speciesList)
	}
	
	Map getRelatedObservationBySpeciesName(speciesName, params){
		println "speciesName  ==== " + speciesName
		def recId = Recommendation.findByName(speciesName).id
		def countQuery = "select count(*) from RecommendationVote recVote where recVote.recommendation.id = :recId and recVote.observation.id != :parentObv"
		def countParams = [parentObv:params.id.toLong(), recId:recId]
		def count = RecommendationVote.executeQuery(countQuery, countParams)
		def query = "select recVote.observation from RecommendationVote recVote where recVote.recommendation.id = :recId and recVote.observation.id != :parentObv  order by recVote.votedOn desc "
		def m = [parentObv:params.id.toLong(), recId:recId, max:params.limit.toInteger(), offset:params.offset.toInteger()]
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
	
	
	private static getMaxRepeatedElementFromList(list){
		list.sort()
		def max = 1
		def currentElement = list[0]
		def maxElement = currentElement
		def currentCounter = 0
		for(spe in list){
			if(spe == currentElement){
				currentCounter++
			}else{
				if(currentCounter > max){
					maxElement = currentElement
				}
				currentElement = spe
				currentCounter = 1
			}
		}
		if(currentCounter > max){
			maxElement = currentElement
		}
		
		return maxElement
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
				int i = index - 1;
				files.add(i, val);
				titles.add(i, params.get('title_'+index));
				licenses.add(i, params.get('license_'+index));
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
	
	
	
}
