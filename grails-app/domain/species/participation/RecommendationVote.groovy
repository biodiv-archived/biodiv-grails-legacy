package species.participation

import species.TaxonomyDefinition;
import species.auth.SUser;
import species.Species;

class RecommendationVote {
	
	def activityFeedService
	
	public enum ConfidenceType {
		CERTAIN("I am certain"),
		UNSURE("I am unsure"),
		
		private String value;
		
		ConfidenceType(String value) {
			this.value = value;
		}
		
		static list() {
			return [CERTAIN, UNSURE];
		}
		String value() {
			return this.value;
		}
	}
	
	Recommendation recommendation;
	ConfidenceType confidence;
	Date votedOn = new Date();
	float userWeight;
	String comment;
	Recommendation commonNameReco;
	
	static belongsTo = [observation:Observation, author:SUser];
	
	static constraints = {
		author(unique:['observation']);
		votedOn validator : {val -> val < new Date()};
		confidence(nullable:true);
		commonNameReco nullable:true, blank: true;
		comment nullable:true, blank: true;
		comment (size:0..400);
	}
	
	static mapping = {
		comment type:'text';
	}
	
	def beforeDelete(){
		//XXX commenting this so that add/agree and remove both will be in sync
		//activityFeedService.deleteFeed(this)
	}

    def updateSpeciesTimeStamp() {
        def taxCon = this.recommendation?.taxonConcept
        if(taxCon) {
            def sp = Species.findByTaxonConcept(taxCon);
            if(sp) {
            sp.lastUpdated = new Date();
            if(!sp.save(flush:true)) {
                this.errors.each { log.error it }
            }
            }
        }
    }
    
}
