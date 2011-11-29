package species.participation

import species.Contributor;
import species.TaxonomyDefinition;


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
	
	Contributor author;
	Recommendation recommendation;
	ConfidenceType confidence;
	Date votedOn;
	
	static belongsTo = [observation:Observation];
	
	static constraints = {
		recommendation(unique:['author']);
	}
	
	static mapping = {
		version:false;
	}

}
