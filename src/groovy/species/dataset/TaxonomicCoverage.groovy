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

    String groupIds;

	static constraints = {
        groupIds nullable:false;
	}

	static mapping = {

	}

    Set groups() {
        Set gps = new HashSet();
        if(groupIds) {
            groupIds.split(',').each {
                gps << Integer.parseInt(it);
            }
        }
        return gps;
    }

    void updateGroups(Set groups) {
        this.groupIds = groups.join(',');
    }
}

