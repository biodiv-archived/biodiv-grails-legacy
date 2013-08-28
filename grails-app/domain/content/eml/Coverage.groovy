package content.eml

import species.groups.SpeciesGroup
import species.Habitat
import speciespage.ObservationService

import org.hibernatespatial.GeometryUserType
import com.vividsolutions.jts.geom.Point;
import species.participation.Observation
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Geometry;
/**
 * 
 * Geographical and taxonomic coverage of resources
 *
 */
class Coverage {
	
	String placeName;
	String reverseGeocodedName
	//String location;
	float latitude;
	float longitude;
	boolean geoPrivacy = false;
	String locationAccuracy;
    Geometry topology;

	static hasMany = [speciesGroups:SpeciesGroup, habitats:Habitat]
	

    static constraints = {
		placeName(nullable:true)
		reverseGeocodedName(nullable:true)
		//location(nullable: true)
		latitude(nullable: true)
		longitude(nullable:true)
		locationAccuracy(nullable: true)
		topology(nullable: true)
//		topology validator : { val, obj ->
//			return ObservationService.validateLocation(val, obj)
//		}
    }
	
    static mapping = {
        columns {
            topology (type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)
        }
    }

	static belongsTo = [Document]
	
	def beforeUpdate(){
		if(isDirty('topology')){
			updateLatLong()
		}
	}
	
	def beforeInsert(){
		updateLatLong()
	}
	
	private  updateLatLong(){
		def centroid =  topology?.getCentroid()
		if(centroid){
			latitude = (float) centroid.getY()
			longitude = (float) centroid.getX()
		}
	}
}
