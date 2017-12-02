package species.dataset

import java.util.Date;

import org.grails.rateable.Rateable;
import org.grails.taggable.Taggable;

import com.vividsolutions.jts.geom.Geometry;

import content.eml.UFile;

import species.Language;
import species.TaxonomyDefinition;
import species.auth.SUser;
import speciespage.ObservationService;

/**
 * @author sravanthi
 *
 */

class TaxonomicCoverage {

    Long groupId;

	//static hasMany = [taxons:TaxonomyDefinition];

	static constraints = {

	}

	static mapping = {

	}
}

