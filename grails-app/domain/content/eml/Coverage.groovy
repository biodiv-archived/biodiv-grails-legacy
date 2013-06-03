package content.eml

import species.groups.SpeciesGroup
import species.Habitat

/**
 * 
 * Geographical and taxonomic coverage of resources
 *
 */
class Coverage {
	
	String placeName;
	String reverseGeocodedName
	String location;
	float latitude;
	float longitude;
	boolean geoPrivacy = false;
	String locationAccuracy;
	
	static hasMany = [speciesGroups:SpeciesGroup, habitats:Habitat]
	

    static constraints = {
		placeName(nullable:true)
		reverseGeocodedName(nullable:true)
		location(nullable: true)
		latitude(nullable: true)
		longitude(nullable:true)
		locationAccuracy(nullable: true)
    }
	
	static belongsTo = [Document]
}
