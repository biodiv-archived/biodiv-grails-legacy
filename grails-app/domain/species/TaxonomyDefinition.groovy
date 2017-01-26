package species

import java.util.List;

import org.hibernate.Hibernate;

import species.ScientificName.TaxonomyRank
import species.groups.SpeciesGroup;
import species.utils.Utils;
import species.NamesMetadata.NameStatus;
import species.NamesMetadata.COLNameStatus;
import species.participation.NamelistService
import species.participation.NamePermission
import species.sourcehandler.XMLConverter;
import species.participation.ActivityFeedService
import species.auth.SUser
import grails.converters.JSON
import groovy.sql.Sql

class TaxonomyDefinition extends ScientificName {
	
	public static List UPDATE_SQL_LIST = []

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
    Long speciesId;	
	// added this column for optimizing case insensitive sql query
	String lowercaseMatchName
	
	//When user want to create absolute new name and dont want to use col curation at any point of the time then
	// set this flat to false 
	boolean doColCuration = true
    String defaultHierarchy;

    def grailsApplication
	def namelistService
	def namelistUtilService
	def activityFeedService
	def dataSource

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
		defaultHierarchy nullable:true;
		speciesId nullable:true;
	}

	static mapping = {
		sort "rank"
		version false;
		tablePerHierarchy true
        defaultHierarchy type:'text'
		activityDescription type:'text'
		
	}
	
	static transients = [ "doColCuration" ]

    Species findSpecies() {
        if(speciesId)
            return Species.get(speciesId);//Species.findByTaxonConcept(this);
        else return null;
    }

	Long findSpeciesId() {
		return speciesId;//findSpecies()?.id
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
   	
	List fetchDefaultHierarchy() {
        if(defaultHierarchy) 
            return JSON.parse(this.defaultHierarchy);
        return null;
        /*
        def classification = Classification.fetchIBPClassification()
        return parentTaxonRegistry(classification).get(classification);
        */
    }
	
	TaxonomyDefinition fetchRoot(){
		TaxonomyRegistry ibpClassi = TaxonomyRegistry.findByTaxonDefinitionAndClassification(this, Classification.fetchIBPClassification())
		if(ibpClassi){
			return TaxonomyDefinition.read(ibpClassi.path.tokenize('_')[0])
		}
	}
	
	
	String fetchRootId(){
		def hir = fetchDefaultHierarchy()
		if(hir)
			return hir[0].id
	}
	
	String fetchRootName(){
		def hir = fetchDefaultHierarchy()
		if(hir)
			return hir[0].canonicalForm
	}
	
	String fetchParentName(){
		def hir = fetchDefaultHierarchy()
		if(hir && (hir.size() > 1))
			return hir[-2].canonicalForm
			
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
		   println  "Already has one IBP hierarchy. Returning >>>  " + this + " col id " +  matchId + "  hir " + TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(this, targetHir)
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
           println trs[i]
		   TaxonomyRegistry tr = trs[i]
		   if(isSnapped)
		   		break
				   
		    TaxonomyDefinition pTd = tr.parentTaxonDefinition 
			if(pTd){
                println "Snapping parent : "+pTd
				pTd.snapToIBPHir(hirList, targetHir)   
			 }
			 isSnapped = _sanpToImmediateParent(tr, targetHir)
			 println "======== checking for path " + tr + "  path " + tr.path + "   result snap " + isSnapped
	   }
	   
	   return isSnapped
	   
   }
   
   
   private boolean _sanpToImmediateParent(TaxonomyRegistry sourceTr,  Classification targetHir){
	   if(sourceTr.parentTaxonDefinition && (sourceTr.parentTaxonDefinition.status != NameStatus.ACCEPTED)){
		   println "Immediate parent has following status " + sourceTr.parentTaxonDefinition.status
		   return false
	   }
	   
	   TaxonomyRegistry targetTr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(sourceTr.parentTaxonDefinition, targetHir)
	   if(!targetTr){
		   	println  "Immediate parent does not have ibp hir or this is the raw name at kingdom level " + this
	   }
	   
	   
	   TaxonomyRegistry ibpTr = new TaxonomyRegistry()
	   	if(targetTr){
	   		ibpTr.properties = targetTr.properties
	   		ibpTr.parentTaxon = targetTr
	   		ibpTr.parentTaxonDefinition = targetTr.taxonDefinition
	   		ibpTr.path = targetTr.path + "_" + sourceTr.taxonDefinition.id
	   	}else{
	   		ibpTr.classification = targetHir
	   		ibpTr.parentTaxon = null
	   		ibpTr.parentTaxonDefinition = null
	   		ibpTr.path = sourceTr.taxonDefinition.id
	   	}

	   ibpTr.taxonDefinition = sourceTr.taxonDefinition
	   ibpTr.contributors = null
	   
	   if(!ibpTr.save(flush:true)){
			ibpTr.errors.allErrors.each { log.error it }
	   }else{
	   		return true
	   }		   
			   
   }
   
   public List fetchUpdateTaxonRegSql(id){
	   List sqlStrings = []
	   //not moving children for name at level kingdom, phylum and class
	   if(rank <= 2){
		   return sqlStrings
	   }

		Classification ibpClassification = Classification.fetchIBPClassification()
		TaxonomyRegistry tr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(this, ibpClassification)
		if(!tr){
			return sqlStrings
		}
		
		//have to look if this node exist in mid or in start. Note end is not required because we are moviing chlidren
		//code here handles both start and mid condition
		String newPathPrefix = "'" + tr.path + "_'"
		String splitter = "'_" + id + "_'"
		String startOffset =  ("" + id).length() + 1 // if id is 12345 then subsitute is 12345_ so length + 1
	
		String startSql = "update taxonomy_registry set path =  (" + newPathPrefix + " ||  right(path, char_length(path) - " + startOffset + "))" ;
		String midSql = "update taxonomy_registry set path =  (" + newPathPrefix + " ||  right(path, char_length(path)- strpos(path, " + splitter + ") - " + startOffset + "))" ;
	
		String whereClause = " where classification_id = " + ibpClassification.id
		String startCond = " and path like '" + id + "\\_%';"
		String midCond = " and path like '%\\_" + id + "\\_%';"
		
		String startQuery = startSql + whereClause + startCond
		String midQuery = midSql + whereClause + midCond
		
		sqlStrings.add(startQuery)
		sqlStrings.add(midQuery)
		
		return sqlStrings
   }
   
   
   // update the path of children in ibp classification when node moves to clean state.
   private void moveChildren(List sqlStrings){
	   //not moving children for name at level kingdom, phylum and class
	   if(rank <= 2){
		   return 
	   }

		Classification ibpClassification = Classification.fetchIBPClassification()
		TaxonomyRegistry tr = TaxonomyRegistry.findByTaxonDefinitionAndClassification(this, ibpClassification)
		if(!tr){
			return 
		}

		sqlStrings.addAll(fetchUpdateTaxonRegSql(id))
		
		if(tr.parentTaxonDefinition){
   			tr.parentTaxonDefinition.moveChildren(sqlStrings)
   		}
   		
   }

   	public boolean createTargetHirFromTaxonReg(TaxonomyRegistry tr, Classification targetClassifi){
		int tCount = TaxonomyRegistry.countByTaxonDefinitionAndClassification(this, targetClassifi)
		if(tCount > 1){
			println ">>>>>>>>>>>>>>>>>>>>>>>>>>. more than one ibp hir " +  "    taxon " + this + " hir==  "  + TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(this, targetClassifi)
		}
		   
		TaxonomyRegistry targetHir = TaxonomyRegistry.findByTaxonDefinitionAndClassification(this, targetClassifi)
		if(targetHir && (targetHir.path == tr.path)){
			return true
		}
		//updating existing hir with new hir. mostly used when name is in clean state and hir is given by user
		if(targetHir){
			if(tr.parentTaxonDefinition){
				tr.parentTaxonDefinition.createTargetHirFromTaxonReg(tr.parentTaxon, targetClassifi)
				targetHir.parentTaxon = TaxonomyRegistry.findByTaxonDefinitionAndClassification(tr.parentTaxonDefinition, targetClassifi)
				targetHir.parentTaxonDefinition = tr.parentTaxonDefinition
				targetHir.path = tr.path
			}else{
				//at kingdom level
				targetHir.path = id
				targetHir.parentTaxon = null
				targetHir.parentTaxonDefinition = null
				targetHir.contributors = null
			}
			if(!targetHir.save(flush:true)){
				 targetHir.errors.allErrors.each { log.error it }
			}
			return true
			
		}else{
			if(!tr.parentTaxonDefinition){
				log.debug "Came up to kingdom level but no Target hir found so starting target hir from kingdom"
				TaxonomyRegistry ibpTr = new TaxonomyRegistry()
				ibpTr.classification = targetClassifi
				ibpTr.taxonDefinition = this
				ibpTr.path = id
				ibpTr.contributors = null
				
				if(!ibpTr.save(flush:true)){
					 ibpTr.errors.allErrors.each { log.error it }
				}
				return true
			}
			
			tr.parentTaxonDefinition.createTargetHirFromTaxonReg(tr.parentTaxon, targetClassifi)
			_sanpToImmediateParent(tr, targetClassifi)
			
			return true
		}   
   }
   
   Map fetchGeneralInfo(){
	   return [name:name, canonicalForm:canonicalForm, rank:TaxonomyRank.getTRFromInt(rank).value().toLowerCase(), position:position, nameStatus:status.toString().toLowerCase(), authorString:authorYear, source:matchDatabaseName, via: viaDatasource, matchId: matchId , nameSourceId:nameSourceId]
   }

    def addSynonym(SynonymsMerged syn) {
        AcceptedSynonym.createEntry(this, syn);
        return;
    }

    List<SynonymsMerged> fetchSynonyms(def particularValue = '') {
        return AcceptedSynonym.fetchSynonyms(this,particularValue);
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
	
	def createSpeciesStub() {
		if(!id) return;

		Species s = Species.get(this.findSpeciesId());
		if(s){
			return s
		}
		
		XMLConverter converter = new XMLConverter();
		
        s = new Species();
		s.taxonConcept = this
		s.title = s.taxonConcept.italicisedForm;
		s.guid = converter.constructGUID(s);
		
		if(!s.save(flush:true)){
			s.errors.allErrors.each {log.error it}
		}
		return s;
	}
	
	public postProcess(){
		if( this.instanceOf(SynonymsMerged)){
			println "Not doing any post process for synonyms"
			return
		}
		
		if((position != NamesMetadata.NamePosition.CLEAN) && doColCuration){
			curateNameByCol()
			log.debug "Adding col hir === " + this.name + " id " + this.id
			addColHir()
		}
		log.debug "Adding IBP hir === "+ this.name + " id " + this.id
		List hirList = [ Classification.findByName(grailsApplication.config.speciesPortal.fields.CATALOGUE_OF_LIFE_TAXONOMIC_HIERARCHY), Classification.findByName('IUCN Taxonomy Hierarchy (2010)'), Classification.findByName("Author Contributed Taxonomy Hierarchy"), Classification.findByName("FishBase Taxonomy Hierarchy"), Classification.findByName("GBIF Taxonomy Hierarchy")]
		def trHir = Classification.fetchIBPClassification()
		snapToIBPHir(hirList, trHir)
		
	}
	
	private addColHir(){
		Classification classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.CATALOGUE_OF_LIFE_TAXONOMIC_HIERARCHY);
		def hir = TaxonomyRegistry.findByTaxonDefinitionAndClassification(this, classification)
		if(hir || !matchId){
			log.debug "Hir already present or No match found on COL " + this
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
	
	private boolean curateNameByCol(){
		log.debug "-------------matchId------------------ " + matchId
		if((status != NameStatus.ACCEPTED) || matchId )
			return true
		
		def colData = namelistService.searchCOL(canonicalForm, 'name')
		def acceptedMatch = namelistService.validateColMatch(this, colData)
		if(colData && !acceptedMatch){
			log.debug "No match found on col so returning without adding col hir"
			return false
		}
		if(!acceptedMatch){
			log.debug "No match found on col so returning without adding col hir"
			return false
		}
		if(!status.value().equalsIgnoreCase(acceptedMatch.nameStatus)) {
			log.debug "Status from col is different so not adding col hir/info " + status + " col status " + acceptedMatch.nameStatus
			return false
		}
		
		namelistService.processDataForMigration(this, acceptedMatch, colData.size(), true)
		return true
	}
	
	def addSynonymFromCol(List synList){
		if(!synList || (status != NameStatus.ACCEPTED))
			return
			
		synList.each {syn ->
			def s = SynonymsMerged.findByMatchId(syn.id)
			if(s){
				addSynonym(s)
			}else{
				NamesParser nameParser = new NamesParser()
				def parsedNames =  nameParser.parse([syn.name]);
				if(!parsedNames[0]?.canonicalForm) {
					log.error "Not able to parse " + syn.name
				}else{
					SynonymsMerged synToAdd
					def name = parsedNames[0]
					def tds = NamelistService.searchIBP(parsedNames[0].canonicalForm, syn.authorString,  NamesMetadata.NameStatus.SYNONYM, syn.parsedRank, false, parsedNames[0].normalizedForm, true)
					if(!tds.isEmpty()){
						log.debug "Name is synonym in new system so reusing " + tds
						synToAdd = tds[0]
					}else{
						synToAdd = new SynonymsMerged()
						synToAdd.normalizedForm = parsedNames[0].normalizedForm;
						synToAdd.italicisedForm = parsedNames[0].italicisedForm;
						synToAdd.binomialForm = parsedNames[0].binomialForm;
						synToAdd.canonicalForm = parsedNames[0].canonicalForm
						synToAdd.status = NamesMetadata.NameStatus.SYNONYM
					}
					
					synToAdd.name = syn.name
					synToAdd.matchId = syn.id
					synToAdd.position = NamesMetadata.NamePosition.WORKING
					synToAdd.rank = syn.parsedRank
					synToAdd.authorYear = syn.authorString
					synToAdd.relationship = XMLConverter.getRelationship(null)
					
					try{
						def mergedSyn = synToAdd.merge()
						synToAdd = mergedSyn ?:synToAdd
						if(!synToAdd.save(flush:true)){
							synToAdd.errors.allErrors.each { println  it }
						}
						addSynonym(synToAdd)
					}catch(e){
						e.printStackTrace()
					}
				}
			}
			
		}	
		
	}
	
	
	def updatePosition(String pos, Map nameSourceInfo = [:], TaxonomyRegistry latestHir = null, TaxonomyDefinition parsedName = null){
		def newPosition = NamesMetadata.NamePosition.getEnum(pos)
		if(newPosition){
			this.position = newPosition
			if(this.position == NamesMetadata.NamePosition.CLEAN){
				// name is moving to clean state.. overwrite all info from spreadsheet
				//println "-------------- >>>>>>>>>>> -------------- Name source info " + nameSourceInfo
				
				//updating name
				if(parsedName){	
					this.name =  parsedName.name
					this.canonicalForm = parsedName.canonicalForm
					this.normalizedForm =  parsedName.normalizedForm
					this.italicisedForm = parsedName.italicisedForm
					this.binomialForm = parsedName.binomialForm
					this.authorYear = parsedName.authorYear
				}
				
				def fieldsConfig = grailsApplication.config.speciesPortal.fields
				def tmpMatchDatabaseName = nameSourceInfo?.get("" + fieldsConfig.NAME_SOURCE)
				def tmpNameSourceId = nameSourceInfo?.get("" + fieldsConfig.NAME_SOURCE_ID)
				def tmpViaDatasource = nameSourceInfo?.get("" + fieldsConfig.VIA_SOURCE)
				matchDatabaseName = tmpMatchDatabaseName
				nameSourceId = tmpNameSourceId
				viaDatasource = tmpViaDatasource
			}
			
			//this one should create ibp hir and getting priority over all the hirerchies
			if((this.position == NamesMetadata.NamePosition.CLEAN) || NamePermission.isAdmin(springSecurityService.currentUser)){ 
				if(latestHir && (latestHir.taxonDefinition == this)){
					Classification ibpClassification = Classification.fetchIBPClassification()
					createTargetHirFromTaxonReg(latestHir, ibpClassification)
					List sqlStrings = []
					moveChildren(sqlStrings)
					UPDATE_SQL_LIST.addAll(sqlStrings)
					//XXX these sql's will be run in speciesbulkupload job at the end
					//excuteSql(sqlStrings);
					//println "finished move children === "
				}
			}
			if(!save()) {
				errors.allErrors.each { log.error it }
			}
		}
	}
	
	/**
	 * This method is used to update ibp hir from given hir. 
	 * This is to update hir by admin from namelist UI actoin only for one name at a time 
	 * @param latestHir
	 * @return
	 */
	
	def updateIBPHir(TaxonomyRegistry latestHir){
		if(latestHir && (latestHir.taxonDefinition == this) && NamePermission.isAdmin(springSecurityService.currentUser)){
			Classification ibpClassification = Classification.fetchIBPClassification()
			createTargetHirFromTaxonReg(latestHir, ibpClassification)
			List sqlStrings = []
			moveChildren(sqlStrings)
			excuteHirUpdateSql(sqlStrings)
			utilsService.clearCache("defaultCache")
		}
	}

	private void excuteHirUpdateSql(List sqlStrings){
		if(!sqlStrings)
			return

		Sql sql = new Sql(dataSource)
		sqlStrings.each { String s ->
			log.debug " Path update query " + s
			try{
				int updateCount = sql.executeUpdate(s);
				log.debug " updated path count  " + 	updateCount
				}catch(e){
					e.printStackTrace()
				}
		}
		String defHirUpdateSql = """ update taxonomy_definition set default_hierarchy = g.dh from (select x.lid, json_agg(x) dh from (select s.lid, t.id, t.name, t.canonical_form, t.rank from taxonomy_definition t, (select taxon_definition_id as lid, regexp_split_to_table(path,'_')::integer as tid from taxonomy_registry tr where tr.classification_id = 265799 order by tr.id) s where s.tid=t.id order by lid, t.rank) x group by x.lid) g where g.lid=id; """
		try{
			int hirupdateCount = sql.executeUpdate(defHirUpdateSql);
			log.debug " Default hir update count " + hirupdateCount
		}catch(e){
			e.printStackTrace()
		}
	}
	
	
	public String fetchLogSummary(){
		return name + "\n" 
	}
	
	def updateNameSignature(List userList = [springSecurityService.currentUser]){
		def ns = createNameSignature()
		if(ns != activityDescription){
			activityDescription = ns
			if(!save()) {
				this.errors.allErrors.each { log.error it }
			}else{
				userList.each {
					//println "--Adding actvity Feed for user " + it + "  FEED " + ns
					if(it)
						activityFeedService.addActivityFeed(this, this, it, ActivityFeedService.TAXON_NAME_UPDATED, ns);
				}
			}
			
		}
	}
	
	private String createNameSignature(){
		String lineBreak = "<br/>"
		String s = ""
		s += "Name : " + name  + lineBreak
		s += "Rank : " + TaxonomyRank.getTRFromInt(rank).value()  + lineBreak
		s += "Position : " + position  + lineBreak
		s += "Name Status : " + status.toString() + lineBreak
		s += "Author : " + authorYear  + lineBreak
		s += "Source : " + matchDatabaseName  + lineBreak
		s += "Via Datasource : " + viaDatasource  + lineBreak
		s += "Match Id : " + matchId + lineBreak

		s += "Hierarchy : " + fetchDefaultHierarchy().collect{it.name}.join("->")  + lineBreak
		//s += "Number of COL Matches : " + noOfCOLMatches + lineBreak
		if(isFlagged) {
			s += "IsFlagged reason : " + flaggingReason.tokenize('###')[-1];
		}
		
		return s
	}
	
	
	def updateNameStatus(String str){
		if("accepted".equalsIgnoreCase(str)){
			namelistService.changeSynToAcc(id)
		}
	}
	
	def updateContributors(List<SUser> users){
		if(!users) return
		
		//not adding contributors to existing accepted name above genus level
		if(contributors && (status == NameStatus.ACCEPTED) && (rank < 7))
			return
		
		try{
			users.minus(contributors)
		
			users.each { u ->
				this.addToContributors(u)
			}
			
			if(!save(flush:true)){
				this.errors.allErrors.each { log.error it }
			}
		}catch(Exception e){
			log.error e.getMessage()
			e.printStackTrace()
		}
	}
	
	static TaxonomyDefinition fetchAccepted(TaxonomyDefinition td) {
		if(!td)
			return null
		
		if(td.status == NameStatus.ACCEPTED){
			return td
		}
		
		List acceptedList = AcceptedSynonym.fetchAcceptedNames(td);
		if(acceptedList &&  (acceptedList.size() == 1)){
			return acceptedList[0]
		}
		
		return null
	}

	def fetchList(params) {
		return namelistService.getNamesFromTaxon(params);
	}

    static List fetchExportableFields(def grailsApplication=null) {
        return [['field':'id', 'name':'TaxonId', 'default':true, 'dbField':'id'], [ 'field' : 'name', 'name':'Name', 'default':true, 'dbField':'name'], ['field':'canonicalForm', 'name':'Canonical Form', 'default':false, 'dbField':'canonical_form'], ['field':'authorYear', 'name':'Author & Year', 'default':true, 'dbField':'author_year'], ['field': 'rank', 'name':'Rank', 'default':true, 'dbField':'rank'], ['field':'status', 'name':'Status', 'default':true, 'dbField':'status'], ['field':'position', 'name':'Position', 'default':true, 'dbField':'position'], ['field':'matchDatabaseName', 'name':'Source', 'default':true, 'dbField':'match_database_name'], ['field':'matchId', 'name':'Match Id', 'default':true, 'dbField':'match_id'], ['field':'viaDatasource', 'name':'Via Database', 'default':true, 'dbField':'via_datasource'], ['field': 'defaultHierarchy', 'name' : grailsApplication?grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY:'Taxonomy Hierarchy', 'default':true, 'dbField':'default_hierarchy'], ['field' : 'group', 'name':'Species Group', 'default':true, 'dbField':'group_id'], ['field':'speciesId', 'name':'Species Id', 'default':false, 'dbField':'species_id'], ['field':'isFlagged', 'name':'Is Flagged', 'default':false, 'dbField':'is_flagged']]; 
    }
	
	public boolean isParent(){
		Classification ibpClassification = Classification.fetchIBPClassification()
		TaxonomyRegistry tr = TaxonomyRegistry.findByParentTaxonDefinitionAndClassification(this, ibpClassification)
		return tr?true:false
	}

    @Override
    String toString() {
        return "<${this.class} : ${id} - ${name}>"
    }
}
