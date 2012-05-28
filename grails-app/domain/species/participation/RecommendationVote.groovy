package species.participation

import species.Contributor;
import species.TaxonomyDefinition;
import species.auth.SUser;


class RecommendationVote {
	
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
	
	SUser author;
	Recommendation recommendation;
	ConfidenceType confidence;
	Date votedOn = new Date();
	float userWeight;
	String comment;
	
	static belongsTo = [observation:Observation];
	
	static constraints = {
		author(unique:['observation']);
		votedOn validator : {val -> val < new Date()};
		confidence(nullable:true);
		comment nullable:true, blank: true;
		comment (size:0..400);
	}
	
	static mapping = {
		comment type:'text';
	}

}
