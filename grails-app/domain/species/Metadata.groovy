package species;

import species.groups.SpeciesGroup
import species.Habitat

import org.hibernatespatial.GeometryUserType
import com.vividsolutions.jts.geom.Point;
import species.participation.Observation
import com.vividsolutions.jts.geom.MultiPolygon;


abstract class Metadata {
	
    //Geographic Coverage
	String placeName;
	String reverseGeocodedName
	String location;
	float latitude;
	float longitude;
	boolean geoPrivacy = false;
	String locationAccuracy;
    Point loc;
    MultiPolygon areas;
    
    //Taxonomic Coverage
    SpeciesGroup group;
	Habitat habitat;

    //TODO: Temporal Coverage
    Date createdOn = new Date();
	Date lastRevised = createdOn;

    //TODO: Controbutions and Attributions

    static constraints = {
		placeName(nullable:true)
		reverseGeocodedName(nullable:true)
		location(nullable: true)
		latitude(nullable: true)
		longitude(nullable:true)
		locationAccuracy(nullable: true)
		loc(nullable: true)
		areas(nullable: true)
    }
	
    static mapping = {
        columns {
            loc type: GeometryUserType, sqlType: "Geometry"
            area type: GeometryUserType, sqlType: "Geometry"
        }
    }

}
