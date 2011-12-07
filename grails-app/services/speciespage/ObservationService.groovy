package speciespage

import java.util.Date;

import species.Resource;
import species.Resource.ResourceType;
import species.auth.SUser;
import species.participation.Observation;
import species.participation.ObservationException;
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
		Observation observation = new Observation();

		
		if(params.author)  {
			observation.author = params.author;
		}

		if(params.url) {
			observation.url = params.url;
		}
		
		observation.notes = params.notes;
		observation.observedOn = params.observedOn?:new Date();
		
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
		def reco = getRecommendation(params);
		def author = params.author;
		ConfidenceType confidence = getConfidenceType(params.confidence);
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
		def observation = Observation.get(params.obvId);
		def reco = getRecommendation(params);
		def author = params.author;
		ConfidenceType confidence = getConfidenceType(params.confidence);
		return new RecommendationVote(observation:observation, recommendation:reco, author:author, confidence:confidence);
	}
	/**
	 * 
	 * @return
	 */
	private Recommendation getRecommendation(params) {
		def reco;
		if(params.recoId) {
			reco = Recommendation.get(params.recoId)
		} else {
			reco = new Recommendation(name:params.recoName);
			recommendationService.save(reco);
		}
		return reco;
	}

	/**
	 * 
	 */
	private List<Resource> saveResources(Observation observation, resourcesXML) {
		XMLConverter converter = new XMLConverter();
		println resourcesXML;
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
			if(type.value().equals(confidenceType)) {
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
		params.file?.each { key, file ->
			Node image = new Node(images, "image");
			if(file) {
				File f = new File(uploadDir, file);
				new Node(image, "fileName", f.absolutePath);
				//new Node(image, "source", imageData.get("source"));
				new Node(image, "caption", params?.title?.getAt(key));
				new Node(image, "contributor", params.author.username);
				new Node(image, "license", params?.license?.getAt(key));
			} else {
				log.warn("No reference key for image : "+key);
			}
		}
		return resources;
	}
}
