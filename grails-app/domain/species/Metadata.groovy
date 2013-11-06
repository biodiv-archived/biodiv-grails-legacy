package species;

import species.groups.SpeciesGroup
import species.Habitat

import org.hibernatespatial.GeometryUserType
import com.vividsolutions.jts.geom.Point;
import species.participation.Observation
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Geometry;
import speciespage.ObservationService;
import species.participation.Featured;

abstract class Metadata {

    //Geographic Coverage
	String placeName;
	String reverseGeocodedName
	
	boolean geoPrivacy = false;
	String locationAccuracy;
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
    }
	
    static mapping = {
        columns {
            topology (type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)
        }
    }

    String notes(){
        return ""
    }

    String summary() {
        return "";
    }

    List featuredNotes() {
        return Featured.featuredNotes(this);
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

}
