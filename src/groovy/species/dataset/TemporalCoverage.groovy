package species.dataset

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
class TemporalCoverage {

	Date fromDate;
	Date toDate;
    String dateAccuracy;

	static constraints = {
		fromDate(nullable:false, validator : {val, obj ->
            println val
            println  "DDDDDDDDDDDDDDAAAAAAAAAAAAAAAAAAAATTTTTTTTTTTTTTTTTTTTEEEEEEEEEEEEEEEEEE"
			if(!val){
				return false
			}
			return val < new Date()
		})
		toDate(nullable:true, validator : {val, obj ->
			if(!val){
				return true
			}
			return val < new Date() ;//&& val >= obj.fromDate
		})
        dateAccuracy nullable:true;
	}

	static mapping = {

	}

}


