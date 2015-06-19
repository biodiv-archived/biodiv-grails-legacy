package species.participation

import org.junit.Before;

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
	boolean isFlagged = false;
	String flaggingReason;

	// added this column for optimizing case insensitive sql query 
	String lowercaseName;
	
	static constraints = {
		name(blank:false, unique:['taxonConcept', 'languageId']);
		taxonConcept nullable:true;
		languageId(nullable:true);
        isFlagged nullable:true;
		lowercaseName nullable:true;
		flaggingReason nullable:true;
	}

	static mapping = { 
		version false; 
	}

	void setName(String name) {
		name = Utils.cleanName(name);
		//if common name
		if(!isScientificName){
			name = Utils.getTitleCase(name)
		}
		
		this.name = name
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
	   }
	   
	   map.put("name", this?.name)
	   def recos = RecommendationVote.withCriteria {
		   eq('recommendation', this)
		   eq('observation', obv)
		   min('votedOn')
	   }
	   map.put("authors", recos.collect{it.author})
	   map.put("votedOn", recos.collect{it.votedOn})
	   map.put("noOfVotes", recos.size())
	   
	   def allRecos = RecommendationVote.withCriteria {
		   eq('observation', obv)
	   }
	   map.put("totalVotes", allRecos.size())
	   
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
   
   def beforeInsert(){
	   lowercaseName = name.toLowerCase()
   }
   
   def beforeUpdate(){
	   if(lowercaseName != name.toLowerCase())
		   lowercaseName = name.toLowerCase()
   }
}
