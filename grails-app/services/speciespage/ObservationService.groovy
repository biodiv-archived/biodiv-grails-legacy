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
		
		observation.group = SpeciesGroup.get(params.group?.id);
		observation.notes = params.notes;
		observation.observedOn = params.observedOn?:new Date();
        observation.placeName = params.place_name;
		observation.reverseGeocodedName = params.place_name;
		observation.location = 'POINT(' + params.longitude + ' ' + params.latitude + ')'
        observation.latitude = Float.parseFloat(params.latitude);
        observation.longitude = Float.parseFloat(params.longitude);
        observation.locationAccuracy = params.location_accuracy;
		//observation.geoPrivacy = params.geo_privacy;
		
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
	/**
	 * 
	 * @return
	 */
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
