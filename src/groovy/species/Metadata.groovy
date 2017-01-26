package species;

import species.auth.SUser;
import species.groups.SpeciesGroup
import species.Habitat

import org.hibernatespatial.GeometryUserType

import com.vividsolutions.jts.geom.Point;

import species.participation.Observation

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Geometry;

import speciespage.ObservationService;
import species.participation.Featured;
import species.utils.Utils;
import species.Language;
import species.dataset.Dataset;
import species.trait.Fact;
abstract class Metadata {
	
	public enum LocationScale {
		APPROXIMATE ("Approximate"),
		ACCURATE ("Accurate"),
		LOCAL ("Local"),
		REGION ("Region"),
		COUNTRY ("Country")
		
		private String value;

		LocationScale(String value) {
			this.value = value;
		}

		String value() {
			return this.value;
		}
		
		static LocationScale getEnum(value){
			if(!value) return null
			
			if(value instanceof LocationScale)
				return value
			
			value = value.toUpperCase().trim()
            println value
			switch(value){
				case 'APPROXIMATE':
					return LocationScale.APPROXIMATE
				case 'ACCURATE':
					return LocationScale.ACCURATE
				case 'LOCAL':
					return LocationScale.LOCAL
				case 'REGION':
					return LocationScale.REGION
				case 'COUNTRY':
					return LocationScale.COUNTRY
				default:
					return null	
			}
		}
	}
	
	//Geographic Coverage
	String placeName;
	String reverseGeocodedName
	
	boolean geoPrivacy = false;
	//XXX to be removed after locationScale migration
	String locationAccuracy;
	LocationScale locationScale;
    Geometry topology;
     
	double latitude;
	double longitude;
	
    //Taxonomic Coverage
    SpeciesGroup group;
	Habitat habitat;

    //Temporal Coverage
	Date fromDate;
	Date toDate;
	
    Date createdOn = new Date();
	Date lastRevised = createdOn;

    // Language
    Language language;

	License license

    String externalId;
    String externalUrl;
    String viaId;
    String viaCode;

    Date lastInterpreted;
    Date lastCrawled;

    Dataset dataset;

    def grailsApplication
	def activityFeedService
    def observationService
    def utilsService;
    //TODO: Contributions and Attributions

    static constraints = {
		placeName(nullable:true)
		reverseGeocodedName(nullable:true)
		latitude(nullable: true)
		longitude(nullable:true)
		locationAccuracy(nullable: true)
		topology nullable:true, validator : { val, obj ->
			if(!val){
				return true
			}
			return ObservationService.validateLocation(val, obj)
		}
        fromDate nullable:true, validator : {val, obj ->
			if(!val){
				return true
			} 
			return val < new Date()
		}
		toDate nullable:true, validator : {val, obj ->
			if(!val){
				return true
			}
			return val < new Date() && val >= obj.fromDate
		}
		license nullable:false
		language nullable:false
		externalId nullable:true
		externalUrl nullable:true
		viaId nullable:true
		viaCode nullable:true
		dataset nullable:true
        lastInterpreted nullable:true, validator : {val, obj ->
			if(!val){
				return true
			} 
			return val < new Date()
		}
        lastCrawled nullable:true, validator : {val, obj ->
			if(!val){
				return true
			} 
			return val < new Date()
		}
    }
	
    static mapping = {
        columns {
            topology (type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)
        }
    }

    String title(){
        return ""
    }

    String notes(Language userLanguage = null){
        return ""
    }

    String summary(Language userLanguage = null) {
        return "";
    }

    List featuredNotes() {
        return Featured.featuredNotes(this, null);
    }

	def onAddComment(comment){
		//updateTimeStamp()
	}
	
	def onAddActivity(af, flushImmidiatly=true){
		updateTimeStamp(flushImmidiatly)
	}
	
	private updateTimeStamp(flushImmidiatly=true){
		lastRevised = new Date();
		saveConcurrently(null, flushImmidiatly);
	}
	
	def updateLatLong(){
		if(!topology){
			return
		}
		def centroid =  topology.getCentroid()
		latitude = (float) centroid.getY()
		longitude = (float) centroid.getX()
	}
	
	private saveConcurrently(f = {}, flushImmidiatly=true){
		try{
			if(f) f()
			if(!save(flush:flushImmidiatly)){
				errors.allErrors.each { log.error it }
			}
		}catch(org.hibernate.StaleObjectStateException e){
			attach()
			def m = merge()
			if(!m.save(flush:flushImmidiatly)){
				m.errors.allErrors.each { log.error it }
			}
		}
	}

	SpeciesGroup fetchSpeciesGroup() {
		return this.group?:SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS); 
	}
	
	def fetchGeoPrivacyAdjustment(SUser reqUser=null){
		if(!geoPrivacy || utilsService.ifOwns(author)){
			return 0
		}
		//for backend thred e.g download request reqUser will be passed as argument
		if(reqUser && (reqUser.id == author.id || utilsService.isAdmin(reqUser))){
			return 0
		}
		return Utils.getRandomFloat()
	}

    Map getTraitFacts() {
        def factList = Fact.findAllByObjectIdAndObjectTypeAndIsDeleted(this.id, this.class.getCanonicalName(), false);
        Map traitFactMap = [:]
        Map queryParams = ['trait':[:]];
        //def conRef = []
        factList.each { fact ->
            if(!traitFactMap[fact.trait.id]) {
                traitFactMap[fact.trait.id] = []
                queryParams['trait'][fact.trait.id] = '';
                traitFactMap['fact'] = []
            }
            if(fact.traitValue) {
                traitFactMap[fact.trait.id] << fact.traitValue
                queryParams['trait'][fact.trait.id] += fact.traitValue.id+',';
            } else if(fact.value) {
                traitFactMap[fact.trait.id] << fact.value+(fact.toValue?":"+fact.toValue:'')
            } 
            if(fact.fromDate && fact.toDate)
                traitFactMap[fact.trait.id] << fact.fromDate+":"+fact.toDate

            traitFactMap['fact'] << fact.id
        }
        queryParams.trait.each {k,v->
            queryParams.trait[k] = v[0..-2];
        }
        return ['traitFactMap':traitFactMap, 'queryParams':queryParams];
    }

    Map getTraits(boolean isObservationTrait=false, boolean isParticipatory = true, boolean showInObservation=false) {
        def traitList = traitService.getFilteredList(['sGroup':this.group.id, 'isObservationTrait':isObservationTrait,'isParticipatory':isParticipatory, 'showInObservation':showInObservation], -1, -1).instanceList;
        def r = getTraitFacts();
        r['traitList'] = traitList; 
        return r;
    }


}
