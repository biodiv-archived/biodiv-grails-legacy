package species.dataset;


import java.util.Date;

import org.grails.rateable.Rateable;
import org.grails.taggable.Taggable;

import com.vividsolutions.jts.geom.Geometry;

import content.eml.UFile;

import species.Language;
import species.Metadata.LocationScale;
import species.TaxonomyDefinition;
import species.auth.SUser;
import speciespage.ObservationService;

/**
 * @author sravanthi
 *
 */

class GeographicalCoverage {
	//Geographic Coverage
	String placeName;
//	String reverseGeocodedName

	boolean geoPrivacy = false;
	//XXX to be removed after locationScale migration
//	String locationAccuracy;
//	LocationScale locationScale;
	Geometry topology;

	double latitude;
	double longitude;

    String locationScale;
    //static hasOne = [locationScale:LocationScale];

	static constraints = {
		placeName(nullable:true)
		//reverseGeocodedName(nullable:true)
		latitude(nullable: true)
		longitude(nullable:true)
		//locationAccuracy(nullable: true)
		topology nullable:true, validator : { val, obj ->
			if(!val){
				return true
			}
			return ObservationService.validateLocation(val, obj)
		}
	}

	static mapping = {
		columns {
			topology (type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)
		}
	}
}


