package species.participation

import species.Language;
import species.TaxonomyDefinition;
import species.auth.Role;
import species.utils.Utils;

class Recommendation {
	
	String name;
	TaxonomyDefinition taxonConcept;
	Date lastModified = new Date();
	
	//To distinguish between scientific and common name
	boolean isScientificName = true;
	//if its common name then can have language
	Long languageId = null;
	
	static constraints = {
		name(blank:false, unique:['taxonConcept', 'languageId']);
		taxonConcept nullable:true;
		languageId(nullable:true);
	}

	static mapping = { 
		version false; 
	}

	void setName(String name) {
		this.name = Utils.cleanName(name);
	}	
	
	/**
	*
	* @param reco
	* @return
	*/
   def getRecommendationDetails(Observation obv) {
	   def map = [:]
	   map.put("recoId", this.id);
	   map.put("isScientificName", isScientificName);
	   
	   if(this?.taxonConcept) {
		   map.put("speciesId", this?.taxonConcept?.findSpeciesId());
		   map.put("canonicalForm", this?.taxonConcept?.canonicalForm)
	   } else {
		   map.put("name", this?.name)
	   }
	   
	   def recos = RecommendationVote.withCriteria {
		   eq('recommendation', this)
		   eq('observation', obv)
		   min('votedOn')
	   }
	   map.put("authors", recos.collect{it.author})
	   map.put("votedOn", recos.collect{it.votedOn})
	   
	   def recoComments = []
	   recos.each {
		   String comment = it.comment;
		   if(comment){
			   recoComments << [recoVoteId:it.id, comment:comment, author:it.author, votedOn:it.votedOn]
		   }
	   }
	   map.put("recoComments", recoComments);
	   return map;
   }
}
