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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author sravanthi
 *
 */
@JsonIgnoreProperties([])
class GeographicalCoverage {
	//Geographic Coverage
	String placeName;
//	String reverseGeocodedName

	boolean geoPrivacy = false;
	//XXX to be removed after locationScale migration
	String locationAccuracy;
//	LocationScale locationScale;

    @JsonIgnore
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
		locationAccuracy(nullable: true)
		topology nullable:true, validator : { val, obj ->
			if(!val){
				return true
			}
			return ObservationService.validateLocation(val, obj)
		}
	}

	static mapping = {
		columns {
			topology (type:org.hibernate.spatial.GeometryType, class:com.vividsolutions.jts.geom.Geometry)
		}
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
}


