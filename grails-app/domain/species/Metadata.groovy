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
	String location;
	boolean geoPrivacy = false;
	String locationAccuracy;
    Geometry topology;
     
	//XXX to be remove after all migration and api changes
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
		location(nullable: true)
		latitude(nullable: true)
		longitude(nullable:true)
		locationAccuracy(nullable: true)
        //topology(nullable:false)
		fromDate (nullable:true)
		toDate (nullable:true)
    }
	
    static mapping = {
        columns {
            topology (type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)
        }
    }
/*
    double getLatitude() {
        if(topology)
            return topology.getCentroid().getX();
    }

    double getLongitude() {
        if(topology) 
            return topology.getCentroid().getY();
    }
*/
}
