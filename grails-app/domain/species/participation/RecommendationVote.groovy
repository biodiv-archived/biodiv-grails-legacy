package species.participation

import species.Contributor;
import species.TaxonomyDefinition;
import species.auth.SUser;


class RecommendationVote {
	
	public enum ConfidenceType {
		CERTAIN("I am certain"),
		UNSURE("I am unsure"),
		DONT_KNOW("I dont know");
		
		private String value;
		
		ConfidenceType(String value) {
			this.value = value;
		}
		
		String value() {
			return this.value;
		}
	}
	
	SUser author;
	Recommendation recommendation;
	ConfidenceType confidence;
	Date votedOn = new Date();
	
	static belongsTo = [observation:Observation];
	
	static constraints = {
		recommendation(unique:['author', 'observation']);
		votedOn(max:new Date());
	}
	
	static mapping = {
		
	}

}
