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
    String dirtyListReason;
	
	// added this column for optimizing case insensitive sql query
	String lowercaseMatchName

    def grailsApplication
	def namelistService
	def namelistUtilService

	static hasMany = [author:String, year:String, hierarchies:TaxonomyRegistry]
    static mappedBy = [hierarchies:'taxonDefinition']

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
		dirtyListReason nullable:true;
		lowercaseMatchName nullable:true;
	}

	static mapping = {
		sort "rank"
		version false;
		tablePerHierarchy true
	}

    Species findSpecies() {
        return Species.findByTaxonConcept(this);
    }

	Long findSpeciesId() {
		return findSpecies()?.id
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
	 * Returns immediate parent taxons canonicals as per all classifications
	 * @return
	 */
	List<TaxonomyDefinition> immediateParentTaxonCanonicals() {
		List<TaxonomyDefinition> result = [];
		TaxonomyRegistry.findAllByTaxonDefinition(this).each { TaxonomyRegistry reg ->
			//TODO : better way : http://stackoverflow.com/questions/673508/using-hibernate-criteria-is-there-a-way-to-escape-special-characters
			def tokens = reg.path.tokenize('_')
			result.add(TaxonomyDefinition.get(Long.parseLong(tokens[-2])).canonicalForm);
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
   	
	List<TaxonomyDefinition> fetchDefaultHierarchy() {
        def classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);
        return parentTaxonRegistry(classification).get(classification);
    }

   Map longestParentTaxonRegistry(Classification classification) {
       def result = [:];
       def res = TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(this, classification);
        def longest= null;
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
       if(!longest) {
           result.put(classification , l);
           result.put('regId', null);
           return result;

       }
       longest.path.tokenize('_').each { taxonDefinitionId ->
           l.add(TaxonomyDefinition.get(Long.parseLong(taxonDefinitionId)));
       }
       result.put('regId', longest.id);
       result.put(classification , l);
       return result;
   }

   public boolean snapToIBPHir(List<Classification> hirList, Classification targetHir){
	   if(TaxonomyRegistry.findByTaxonDefinitionAndClassification(this, targetHir)){
		   println  "Already has one IBP hierarchy. Returning " + this
		   return true
	   }
	   
	   List<TaxonomyRegistry> trs = []
	   
	   hirList.each { hir ->
		  trs.addAll(TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(this, hir))
	   }
	   
	   if(trs.isEmpty()){
		   println "No hir found for name " + this
		   return false
	   }
	   
	   boolean isSnapped = false
	   for(int i = 0; i < trs.size(); i++){
		   TaxonomyRegistry tr = trs[i]
		   if(isSnapped)
		   		break
				   
		    TaxonomyDefinition pTd = tr.parentTaxonDefinition 
			if(pTd){
				pTd.snapToIBPHir(hirList, targetHir)   
			 }
			 isSnapped = _sanpToImmediateParent(tr, targetHir)
			 println "======== checking for path " + tr + "  path " + tr.path + "   result snap " + isSnapped
	   }
	   
	   return isSnapped
	   
   }
   
   
   private boolean _sanpToImmediateParent(TaxonomyRegistry sourceTr,  Classification targetHir){
	   if(!sourceTr.parentTaxonDefinition){
		   println "No parent "
		   return false
	   }
	   if(sourceTr.parentTaxonDefinition.status != NameStatus.ACCEPTED){
		   println "Immediate parent has following status " + sourceTr.parentTaxonDefinition.status
		   return false
	   }
	   TaxonomyRegistry targetTr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(sourceTr.parentTaxonDefinition, targetHir)
	   if(!targetTr){
		   	println  "Immediate parent does not have ibp hir or this is the raw name at kingdom level " + this
	   		return false
	   }
	   
	   
	   TaxonomyRegistry ibpTr = new TaxonomyRegistry()
	   ibpTr.properties = targetTr.properties
	   ibpTr.parentTaxon = targetTr
	   ibpTr.parentTaxonDefinition = targetTr.taxonDefinition
	   ibpTr.taxonDefinition = sourceTr.taxonDefinition
	   ibpTr.path = targetTr.path + "_" + sourceTr.taxonDefinition.id
	   ibpTr.contributors = null
	   
	   if(!ibpTr.save(flush:true)){
			ibpTr.errors.allErrors.each { log.error it }
	   }else{
	   		return true
	   }		   
			   
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
	
	def beforeInsert(){
		super.beforeInsert()
		lowercaseMatchName = canonicalForm.toLowerCase()
	}
	
	def beforeUpdate(){
		super.beforeUpdate()
		if(lowercaseMatchName != canonicalForm.toLowerCase())
			lowercaseMatchName = canonicalForm.toLowerCase()
	}
	
	def afterInsert(){
//		TaxonomyDefinition.withNewSession{
//			println "================================ calling post preocess"
//			postProcess()
//		}
		
	}
	
	public postProcess(){
		curateNameByCol()
		println "----------------------------------- adding col hir"
		addColHir()
		println "---------------------- adding IBP hir"
		
		List hirList = [ Classification.findByName(grailsApplication.config.speciesPortal.fields.CATALOGUE_OF_LIFE_TAXONOMIC_HIERARCHY), Classification.findByName('IUCN Taxonomy Hierarchy (2010)'), Classification.findByName("Author Contributed Taxonomy Hierarchy"), Classification.findByName("FishBase Taxonomy Hierarchy"), Classification.findByName("GBIF Taxonomy Hierarchy")]
		def trHir = Classification.findByName("IBP Taxonomy Hierarchy");
		snapToIBPHir(hirList, trHir)
	}
	
	private addColHir(){
		Classification classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.CATALOGUE_OF_LIFE_TAXONOMIC_HIERARCHY);
		def hir = TaxonomyRegistry.findByTaxonDefinitionAndClassification(this, classification)
		if(hir || !matchId){
			log.debug "Hir already present or No match found on COL"
			return
		}
		
		Map colData = namelistUtilService.getColDataFromColId(matchId)
		if(!colData){
			return
		}
		
		List taxonList = []
		boolean abort = false
		println "colIdPath ---- " + colData.colIdPath
		List colIdList = colData.colIdPath.tokenize("_")
		colIdList.each {
			if(abort) return
			def td = TaxonomyDefinition.findByMatchId(it)
			if(!td){
				abort = true
				return
			}
			taxonList << td
		}
		if(abort){
			log.error "Some name is missing while adding col hir aborting please check db " + colIdList
			return
		}
		
		taxonList << this
		List pathList = []
		String path = ""
		TaxonomyDefinition prevTaxon = null
		TaxonomyRegistry prevReg = null
		taxonList.each { TaxonomyDefinition td ->
			pathList << td.id 
			TaxonomyRegistry tr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(td, classification)
			if(!tr){
				tr = new TaxonomyRegistry()
				tr.path = pathList.join("_")
				tr.parentTaxonDefinition = prevTaxon
				tr.taxonDefinition = td
				tr.parentTaxon = prevReg
				tr.classification =  classification
				println "------ saving col registry " + tr
				if(!tr.save(flush:true)){
					tr.errors.allErrors.each { log.error it }
				}
			}
			prevTaxon = td
			prevReg = tr
		}
	}
	
	private curateNameByCol(){
		if((status != NameStatus.ACCEPTED) || matchId)
			return
		
			
		def colData = namelistService.searchCOL(canonicalForm, 'name')
		println "--------------------------- " + colData
		def acceptedMatch = namelistService.validateColMatch(this, colData)
		println "------------------ came here " + acceptedMatch
		if(!acceptedMatch){
			log.debug "No match found on col so returning without adding col hir"
			return
		}
		if(!status.value().equalsIgnoreCase(acceptedMatch.nameStatus)) {
			log.debug "Status from col is different so not adding col hir/info " + status + " col status " + acceptedMatch.nameStatus
			return
		}
		
		namelistService.processDataForMigration(this, acceptedMatch, colData.size(), true)
		
	}
}
