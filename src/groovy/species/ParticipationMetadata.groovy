package species

import species.auth.SUser;
import org.grails.taggable.Taggable;
import org.grails.rateable.*
import species.groups.UserGroup;

abstract class ParticipationMetadata extends Metadata implements Taggable, Rateable {
	
    SUser author;//or uploader from sourcedata
    String originalAuthor;

    int rating;
	long visitCount = 0;
	int flagCount = 0;
	int featureCount = 0;
    boolean isDeleted = false;


    static constraints = {
		author nullable:true
		originalAuthor nullable:true
    	featureCount nullable:false
    }

	static mapping = {
        tablePerHierarchy false
//        tablePerSubClass true
    }

	def beforeInsert(){
	}
	
	def beforeUpdate(){
	}
	
	def incrementPageVisit(){
		visitCount++
	}


}
