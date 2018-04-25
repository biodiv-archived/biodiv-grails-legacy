package content.eml

import species.groups.SpeciesGroup
import species.Habitat
import speciespage.ObservationService

//import org.hibernatespatial.GeometryUserType
import com.vividsolutions.jts.geom.Point;
import species.participation.Observation
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Geometry;


class GeospatialCoverage {	

    String description;	

    double minLatitude;
    double maxLatitude;
    double minLongitude;
    double maxLongitude;

    static constraints = {
    }
	
    static mapping = {
    }

}
