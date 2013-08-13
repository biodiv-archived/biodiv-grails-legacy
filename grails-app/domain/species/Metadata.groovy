package species;

import species.groups.SpeciesGroup
import species.Habitat

import org.hibernatespatial.GeometryUserType
import com.vividsolutions.jts.geom.Point;
import species.participation.Observation
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Geometry

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

    //TODO: Contributions and Attributions

    static constraints = {
		placeName(nullable:true)
		reverseGeocodedName(nullable:true)
		latitude(nullable: false)
		longitude(nullable:false)
		locationAccuracy(nullable: true)
        topology(nullable:false)
		fromDate validator : {val, obj -> val < new Date()}
		toDate nullable:true, validator : {val, obj ->
			if(!val){
				return true
			}else{
			 	return val < new Date() && val >= obj.fromDate
			}
		}
    }
	
    static mapping = {
        columns {
            topology (type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)
        }
    }
	
}
