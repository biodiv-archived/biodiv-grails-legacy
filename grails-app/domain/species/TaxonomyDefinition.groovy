package species

import java.util.List;

import species.ScientificName.TaxonomyRank
import species.groups.SpeciesGroup;
import species.utils.Utils;
import species.NamesMetadata.NameStatus;
import species.NamesMetadata.COLNameStatus;

class TaxonomyDefinition extends ScientificName {


	int rank;
	String name;
    NamesMetadata.NameStatus status = NamesMetadata.NameStatus.ACCEPTED;
	SpeciesGroup group;
	String threatenedStatus;
	ExternalLinks externalLinks;
    boolean isFlagged = false;
	String flaggingReason;
    NamesMetadata.COLNameStatus colNameStatus;
    int noOfCOLMatches = -50;
    String oldId;
    boolean isDeleted = false;

	static hasMany = [author:String, year:String]

	static constraints = {
		name(blank:false)
		canonicalForm nullable:false;
		group nullable:true;
		isFlagged nullable:true;
		isDeleted nullable:true;
		threatenedStatus nullable:true;
		flaggingReason nullable:true;
		externalLinks nullable:true;
		colNameStatus nullable:true;
		noOfCOLMatches nullable:true;
		oldId nullable:true;
	}

	static mapping = {
		sort "rank"
		version false;
		tablePerHierarchy true
	}

	Long findSpeciesId() {
		return Species.findByTaxonConcept(this)?.id;
	}

	void setName(String name) {
		this.name = Utils.cleanName(name);
	}

	/**
	 * Returns parents as per all classifications
	 * @return
	 */
	List<TaxonomyDefinition> parentTaxon() {
		List<TaxonomyDefinition> result = [];
		TaxonomyRegistry.findAllByTaxonDefinition(this).each { TaxonomyRegistry reg ->
			//TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
			reg.path.tokenize('_').each { taxonDefinitionId ->
				result.add(TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId)));
			}
		}
		return result;
	}

	/**
	 * Returns parents as per all classifications
	 * @return
	 */
	Map<Classification, List<TaxonomyDefinition>> parentTaxonRegistry() {
		Map<List<TaxonomyDefinition>> result = [:];
        def regList = TaxonomyRegistry.findAllByTaxonDefinition(this);
        for(TaxonomyRegistry reg in regList) {
			//TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
			def l = []
			reg.path.tokenize('_').each { taxonDefinitionId ->
				l.add(TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId)));
			}
			result.put(reg.classification , l);
		}
		return result;
	}
	
	/**
	* Returns parents as per classification
	* @return
	*/
   Map<Classification, List<TaxonomyDefinition>> parentTaxonRegistry(Classification classification) {
	   Map<List<TaxonomyDefinition>> result = [:];
	   TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(this, classification).each { TaxonomyRegistry reg ->
		   //TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
		   def l = []
		   reg.path.tokenize('_').each { taxonDefinitionId ->
			   l.add(TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId)));
		   }
		   result.put(reg.classification , l);
	   }
	   return result;
   }
   
    Map<Classification, List<TaxonomyDefinition>> longestParentTaxonRegistry(Classification classification) {
	    Map<List<TaxonomyDefinition>> result = [:];
        def res = TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(this, classification)
	    def longest;
        int max = 0;
        res.each { TaxonomyRegistry reg ->
		   //TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
		    def tokens = reg.path.tokenize('_')
            if(tokens.size()>max) {
                longest = reg
                max = tokens.size();
            }
        }
        def l = []
        longest.path.tokenize('_').each { taxonDefinitionId ->
            l.add(TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId)));
        }
        result.put(longest.classification , l);
        return result;
    }

   Map fetchGeneralInfo(){
	   return [name:name, rank:TaxonomyRank.getTRFromInt(rank).value().toLowerCase(), position:position, nameStatus:status.toString().toLowerCase(), authorString:authorYear, source:matchDatabaseName, via: viaDatasource, matchId: matchId ]
   }

    def addSynonym(SynonymsMerged syn) {
        AcceptedSynonym.createEntry(this, syn);
        return;
    }

    List<SynonymsMerged> fetchSynonyms() {
        return AcceptedSynonym.fetchSynonyms(this);
    }

    def removeSynonym(SynonymsMerged syn) {
        if(!syn)return;
        AcceptedSynonym.removeEntry(this, syn);
        return;
    }
    
    //Removes as accepted name from all synonyms
    def removeAsAcceptedName() {
        def synonyms = this.fetchSynonyms();
        synonyms.each { syn ->
            this.removeSynonym(syn);
        }
    }

}
