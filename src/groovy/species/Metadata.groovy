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
	}

	
	
	//Geographic Coverage
	String placeName;
	String reverseGeocodedName
	
	boolean geoPrivacy = false;
	//XXX to be removed after locationScale migration
	String locationAccuracy;
	LocationScale locationScale;
    Geometry topology;
     
	float latitude;
	float longitude;
	
    //Taxonomic Coverage
    SpeciesGroup group;
	Habitat habitat;

    //Temporal Coverage
	Date fromDate;
	Date toDate;
	
    Date createdOn = new Date();
	Date lastRevised = createdOn;

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
		locationScale(nullable: true)
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
		if(reqUser && (reqUser.id == author.id || utilsService.isAdmin(reqUser.id))){
			return 0
		}
		return Utils.getRandomFloat()
	}

}
