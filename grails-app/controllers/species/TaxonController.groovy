package species

import java.sql.ResultSet;

import species.ScientificName.TaxonomyRank;
import species.formatReader.SpreadsheetReader;
import species.sourcehandler.MappedSpreadsheetConverter;
import species.sourcehandler.XMLConverter;
import grails.converters.JSON;
import grails.converters.XML;
import grails.plugin.springsecurity.annotation.Secured;
import grails.web.JSONBuilder;
import groovy.sql.GroovyRowResult;
import groovy.sql.Sql
import groovy.xml.MarkupBuilder;
import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import species.auth.SUser;
import species.ScientificName.RelationShip
import species.NamesMetadata.NamePosition;


class TaxonController {

    def dataSource
    def taxonService;
    def springSecurityService;
    def activityFeedService;
    def utilsService;
    def grailsApplication;
    def messageSource
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    //def combinedHierarchy = Classification.findByName(grailsApplication.config.speciesPortal.fields.COMBINED_TAXONOMIC_HIERARCHY);

    /**
     * 
     */
    def index = {
    }

    /**
     * 
     */
    def listHierarchy = {
        log.debug params;
        //cache "taxonomy_results"
        includeOriginHeader();

        int level = params.n_level ? Integer.parseInt(params.n_level)+1 : 0
        def parentId = params.nodeid  ?: null
        def expandAll = params.expand_all  ? (new Boolean(params.expand_all)).booleanValue(): false
        def expandSpecies = params.expand_species  ? (new Boolean(params.expand_species)).booleanValue(): false
        Long classSystem = params.classSystem ? Long.parseLong(params.classSystem): null;
        Long speciesid = params.speciesid ? Long.parseLong(params.speciesid) : null

        /*combinedHierarchy.merge();
        if(classSystem == combinedHierarchy.id) {
            classSystem = null;
        }*/

        long startTime = System.currentTimeMillis();
        def rs = new ArrayList<GroovyRowResult>();
        if(expandSpecies) {
            //def taxonIds = getSpeciesHierarchyTaxonIds(speciesid, classSystem)
            //getHierarchyNodes(rs, 0, 8, null, classSystem, false, expandSpecies, taxonIds);
            Long regId = classSystem;
            getSpeciesHierarchy(speciesid, rs, regId);
        } else {
            def fieldsConfig = grailsApplication.config.speciesPortal.fields
            def classification = Classification.findByName(fieldsConfig.IBP_TAXONOMIC_HIERARCHY);
            def cl = Classification.read(classSystem.toLong());
            getHierarchyNodes(rs, level, level+3, parentId, classSystem, expandAll, expandSpecies, null);
            println "========RES SIZE ========== " + rs.size() 
            if(cl == classification) {
                def authorClass = Classification.findByName(fieldsConfig.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY);
                getHierarchyNodes(rs, level, level+3, parentId, authorClass.id, expandAll, expandSpecies, null,"RAW");
            }
        }
        log.debug "Time taken to build hierarchy : ${(System.currentTimeMillis()- startTime)/1000}(sec)"
        render(contentType: "text/xml", text:buildHierarchyResult(rs, classSystem))
    }

    /**
     * 
     * @param resultSet
     * @param level
     * @param parentId
     * @param classSystem
     * @param expandAll
     * @param taxonIds
     */
    private void getHierarchyNodes(List<GroovyRowResult> resultSet, int level, int tillLevel, String parentId, Long classSystem, boolean expandAll, boolean expandSpecies, List taxonIds, String position = null) {
        def sql = new Sql(dataSource)
        def rs;
        String sqlStr;

        long startTime = System.currentTimeMillis();

        if(classSystem) {
            if(!parentId) {
                sqlStr = "select t.id as taxonid, 1 as count, t.rank as rank, t.name as name, s.path as path, ${classSystem} as classsystem, t.position as position \
                    from taxonomy_registry s, \
                    taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (classSystem?"s.classification_id = :classSystem and ":"")+ 
                    (position?"t.position = :position and ": "")+
                    "t.rank = 0";
                rs = sql.rows(sqlStr, [classSystem:classSystem, position:position])
            }
            else if(level == TaxonomyRank.SPECIES.ordinal()) {
                sqlStr = "select t.id as taxonid,  1 as count, t.rank as rank, t.name as name,  s.path as path , ${classSystem} as classsystem, t.position as position\
                    from taxonomy_registry s, taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (classSystem?"s.classification_id = :classSystem and ":"")+
                    (position?"t.position = :position and ": "")+
                    +"t.rank = "+level+" and \
                    s.path ~ '^"+parentId+"_[0-9]+\$' "
                    rs = sql.rows(sqlStr , [classSystem:classSystem, position:position]);
            } else {
                sqlStr = "select t.id as taxonid, 1 as count, t.rank as rank, t.name as name,  s.path as path , ${classSystem} as classsystem, t.position as position\
                    from taxonomy_registry s, \
                    taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (classSystem?"s.classification_id = :classSystem and ":"")+
                    (position?"t.position = :position and ": "")+
                    "s.path ~ '^"+parentId+"_[0-9]+\$' " +
                    "order by t.rank, t.name asc";
                rs = sql.rows(sqlStr, [classSystem:classSystem, position:position])
            }
        } else {
            if(!parentId) {
                sqlStr = "select t.id as taxonid, 1 as count, 0 as rank, t.name as name, s.path as path , ${classSystem} as classsystem, t.position as position\
                    from taxonomy_registry s, \
                    taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (position?"t.position = :position and ": "")+
                    "t.rank = 0 group by s.path, t.id, t.name";
                rs = sql.rows(sqlStr, [position:position])
            }
            else if(level == TaxonomyRank.SPECIES.ordinal()) {
                sqlStr = "select t.id as taxonid,  1 as count, t.rank as rank, t.name as name,  s.path as path , ${classSystem} as classsystem, t.position as position\
                    from taxonomy_registry s, taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (position?"t.position = :position and ": "")
                    +"t.rank = "+level+" and \
                    s.path ~ '^"+parentId+"_[0-9]+\$' group by s.path, t.id, t.name";
                rs = sql.rows(sqlStr, [position:position]);
            } else {
                sqlStr =  "select t.id as taxonid, 1 as count, t.rank as rank, t.name as name,  s.path as path , ${classSystem} as classsystem, t.position as position\
                    from taxonomy_registry s, \
                    taxonomy_definition t \
                    where \
                    s.taxon_definition_id = t.id and "+
                    (position?"t.position = :position and ": "")+
                    "s.path ~ '^"+parentId+"_[0-9]+\$' group by s.path, t.rank, t.id, t.name" +
                    " order by t.rank asc, t.name"
                    rs = sql.rows(sqlStr, [position:position])
            }
        }
        log.debug "Time taken to execute taxon hierarchy query : ${(System.currentTimeMillis()- startTime)/1000}(sec)"
        log.debug "SQL for taxon hierarchy : "+sqlStr;
        rs.each { r ->
            r.put('expanded', false);
            r.put("speciesid", -1)
            r.put('loaded', false);
            populateSpeciesDetails(r.taxonid, r);

            resultSet.add(r);
            if(expandAll || (taxonIds && taxonIds.contains(r.taxonid))) {
                if(r.rank < TaxonomyRank.SPECIES.ordinal()) {
                    //r.put('count', getCount(r.path, classSystem));
                    if(r.rank+1 <= tillLevel) {
                        r.put('expanded', true);
                        r.put('loaded', true);
                        getHierarchyNodes(resultSet, r.rank+1, tillLevel, r.path, classSystem, expandAll, expandSpecies, taxonIds)
                    }
                }
            }
        }
    }

    /**
     * 
     * @param name
     * @param taxonId
     * @param path
     * @param classSystem
     * @return
     */
    private getSpecies(long taxonId, int level=TaxonomyRank.SPECIES.ordinal()) {
        def sql = new Sql(dataSource)
        return Species.find("from Species as s where s.taxonConcept.id = :taxonId", [taxonId:taxonId]);
    }

    /**
     * TODO:optimize 
     * @param id
     * @return
     */
    private String getSpeciesName(long id) {
        def species = Species.get(id);
        return species.taxonConcept.italicisedForm;
    }

    /**
     * 
     * @param speciesId
     * @param classSystem
     * @return
     */
    private List getSpeciesHierarchyTaxonIds(Long taxonId, Long classSystem) {
        def sql = new Sql(dataSource)
        String s = """select s.path as path 
        from taxonomy_registry s, 
        taxonomy_definition t 
        where 
        s.taxon_definition_id = t.id and 
        ${(classSystem?"s.classification_id = :classSystem and ":"")}
        s.path like '%!_"""+taxonId+"' escape '!'";

        def rs
        if(classSystem) {
            rs = sql.rows(s, [classSystem:classSystem])
        } else {
            rs = sql.rows(s)
        }
        def paths = rs.collect {it.path};


        def result = [];
        paths.each {
            it.tokenize("_").each {
                result.add(Long.parseLong(it));
            }
        }
        return result;
        //		return [Species.get(speciesId).id]
    }

    /**
     * todo:CORRECT THIS
     * @param speciesId
     * @param classSystem
     * @return
     */
    private int getCount(String parentId, long classSystem) {
        def sql = new Sql(dataSource)
        def rs = sql.rows("select count(*) as count \
            from taxonomy_registry s, \
            taxonomy_definition t \
            where \
            s.taxon_definition_id = t.id and "+
            (classSystem?"s.classification_id = :classSystem and ":"")+
            "s.path ~ '^"+parentId+"_[0-9_]+\$' " +
            " group by t.rank \
            having t.rank = :rank", [classSystem:classSystem, rank:TaxonomyRank.SPECIES.ordinal()])
        return rs[0]?.count;
    }

    /**
     * 
     */

    private List getSpeciesHierarchy(Long speciesTaxonId, List rs, Long regId) {
        List speciesHier = [];
        //int minHierarchySize = 6;
        if(regId) {
            //Classification classification = Classification.get(classSystem);
            //TaxonomyRegistry.findAllByTaxonDefinitionAndClassification(taxonConcept, classification).each {reg ->
            def reg = TaxonomyRegistry.read(regId);
                def list = [] 
                while(reg != null) {
                    def result = [id:reg.id, parentId:reg.parentTaxon?.id, 'count':1, 'rank':reg.taxonDefinition.rank, 'name':reg.taxonDefinition.name, 'path':reg.path, 'classSystem':reg.classification.id, 'expanded':true, 'loaded':true, 'isContributor':reg.isContributor()]
                    populateSpeciesDetails(reg.taxonDefinition.id, result);
                    list.add(result);
                    reg = reg.parentTaxon;
                }
                //if(list.size() >= minHierarchySize) {
                    list = list.sort {it.rank};
                    speciesHier.addAll(list);
                //}
        } else {
            TaxonomyDefinition taxonConcept = Species.read(speciesTaxonId)?.taxonConcept;
            TaxonomyRegistry.findAllByTaxonDefinition(taxonConcept).each { reg ->
                def list = [];
                while(reg != null) {					
                    def result = [id:reg.id, parentId:reg.parentTaxon?.id, 'count':1, 'rank':reg.taxonDefinition.rank, 'name':reg.taxonDefinition.name, 'path':reg.path, 'classSystem':reg.classification.id, 'expanded':true, 'loaded':true, 'isContributor':reg.isContributor()];
                    populateSpeciesDetails(speciesTaxonId, result);
                    list.add(result);					
                    reg = reg.parentTaxon;
                }
                //if(list.size() >= minHierarchySize) {
                    list = list.sort {it.rank};
                    speciesHier.addAll(list);
                //}				
            }
        }

        //removing duplicate path elements
        def temp = new HashSet();
        speciesHier.each { map ->
            //if(!temp.contains(map.path)) {
            //    temp.add(map.path);
                rs.add(map);

           // }
        }
        return rs;
    }

    /**
     * 
     */
    private void populateSpeciesDetails(Long speciesTaxonId, Map result) {
        //if(result.rank == TaxonomyRank.SPECIES.ordinal()) {
            def species = getSpecies(speciesTaxonId, result.rank);
            if(species){
                result.put("speciesid", species.id)
                result.put('speciesname', species.title)
                result.put('count', 1);
            }
        //}
    }

    /**
     * render t as XML;
     * @param rs
     * @param classSystem
     * @return
     */
    private String buildHierarchyResult(rs, classSystem) {
        def writer = new StringWriter ();
        def result = new MarkupBuilder(writer);
        int i=0;
        result.rows() {
            page (1)
            total (1)
            int size = 0;
           rs.each { r->
                size ++;
                String parentPath = "";
                if(r.path && r.path.lastIndexOf("_")!=-1) {
                    parentPath = r.path.substring(0, r.path.lastIndexOf("_"))
                }
                def id;
                if(r.containsKey(id)) {
                    id = r.id;
                } else {
                    id = r.path;
                }
                row(id:id) {
                    cell(id)
                    cell(r.path)
                    cell (r.name.trim())
                    cell (r.count)
                    cell (r["speciesid"])
                    cell (r["classsystem"])
                    cell (r.rank)
                    if(r.containsKey('parentId')) {
                    cell (r.parentId)
                    } else {
                    cell (parentPath)
                    }
                    cell (r.rank == TaxonomyRank.SPECIES.ordinal() ? true : false)
                    cell (r.expanded?:false) //for expanded
                    cell (r.loaded?:false) //for loaded
                    if(r.containsKey('isContributor')) {
                        cell (r.isContributor?:false) //for edit/delete
                    } else {
                        cell (false) //for edit/delete
                    }
                    cell (r["position"])
                }
            }
            records (size)
        }
        return writer.toString();
    }

    /**
     * 
     * @param origin
     * @return
     */
    private boolean isValid(String origin) {
        String originHost = (new URL(origin)).getHost();
        return grailsApplication.config.speciesPortal.validCrossDomainOrigins.contains(originHost)
    }

    /**
     * 
     */
    private void includeOriginHeader() {
        String origin = request.getHeader("Origin");
        if(origin) {
            String validOrigin = isValid(origin)?origin:"";
            response.setHeader("Access-Control-Allow-Origin", validOrigin);
            response.setHeader("Access-Control-Allow-Methods", request.getHeader("Access-Control-Request-Methods"));
            response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
            response.setHeader("Access-Control-Max-Age", "86400");
        }
    }

	@Secured(['ROLE_USER'])
    def create() {
        println "=======PARAMS IN CREATE======== " + params
        def msg;

        def errors = [], result=[success:false];
        if(params.classification) {

            Language languageInstance = utilsService.getCurrentLanguage(request);

            String speciesName;
            Map list = params.taxonRegistry?:[];
            List t = taxonService.getTaxonHierarchyList(list);
            speciesName = t[TaxonomyRank.SPECIES.ordinal()];

//TODO        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
//            return [success:false, msg:"You don't have permission to delete synonym"]
//        }

            try {

                if(!taxonService.validateHierarchy(t)) {
                    msg = messageSource.getMessage("default.taxon.mandatory.missing", null, RCU.getLocale(request))
                    render ([success:false, msg:msg, errors:errors] as JSON)
                    return;
                }

                def classification = params.classification ? Classification.read(params.long('classification')) : null;
                result = taxonService.addTaxonHierarchy(speciesName, t, classification, springSecurityService.currentUser, languageInstance);
                result.action = 'create';

                if(result.success) {
                    def speciesInstance = getSpecies(result.reg.taxonDefinition.id, result.reg.taxonDefinition.rank);
                    def feedInstance = activityFeedService.addActivityFeed(speciesInstance, result.reg, springSecurityService.currentUser, result.activityType );
                    utilsService.sendNotificationMail(activityFeedService.SPECIES_HIERARCHY_CREATED, speciesInstance, request, params.webaddress, feedInstance, ['info': result.activityType]);
                }


                render result as JSON
                return;
           } catch(e) {
                e.printStackTrace();
                errors << e.getMessage();
                msg = messageSource.getMessage("default.error.hierarchy", ['adding'] as Object[], RCU.getLocale(request))
                render ([success:false, msg:msg, errors:errors] as JSON)
                return;
            }
            msg = messageSource.getMessage("default.error.hierarchy", ['adding'] as Object[], RCU.getLocale(request))
            render ([success:false, msg:msg, errors:errors] as JSON)
            return;
        } else {
            errors << messageSource.getMessage("default.error.hierarchy.missing", ['classification'] as Object[], RCU.getLocale(request))
            msg = messageSource.getMessage("default.error.hierarchy", ['adding'] as Object[], RCU.getLocale(request))
            render ([success:false, msg:msg, errors:errors] as JSON)
        }

    }
    
	@Secured(['ROLE_USER'])
	def update()  {
		//Do only when params coming from curation interface
		def otherParams = [:]
        params['fromCurationInterface'] = false;
		if(params.taxonData) {
            params['fromCurationInterface'] = true;
			params << JSON.parse(params.taxonData)
			params.remove('taxonData');
			otherParams['id_details'] = params.id_details;
			otherParams['metadata'] = params.metadata;
			otherParams['spellCheck'] = params.spellCheck;
			otherParams['oldTaxonId'] = params.oldTaxonId;
		}
        if(params.metadata?.nameStatus != "accepted") {
            println "=========STATUS======== " + params.metadata?.nameStatus
        }
		println "=======PARAMS UPDATE======== " + params
		def msg;
		def errors = [], result=[success:false];
		if(params.classification) {

			println "=======1======== "
			Language languageInstance = utilsService.getCurrentLanguage(request);

			String speciesName;
			Map list = params.taxonRegistry?:[];
			List t = taxonService.getTaxonHierarchyList(list);
			speciesName = t[TaxonomyRank.SPECIES.ordinal()];

			//TODO        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
			//            return [success:false, msg:"You don't have permission to delete synonym"]
			//        }

			println "=======2======== "
			try {
				for (int i=0; i< t.size(); i++) {
					if(!t[TaxonomyRank.list()[i].ordinal()]) {
						errors << TaxonomyRank.list()[i].value() + " is missing";
					}
				}

				println "======3======== "
				if(!taxonService.validateHierarchy(t)) {
					msg = messageSource.getMessage("default.taxon.mandatory.missing", null, RCU.getLocale(request))
					render ([success:false, msg:msg, errors:errors] as JSON)
					return;
				}

				def classification, taxonregistry;
				if(params.reg) {
					println "=======4======== "
					TaxonomyRegistry reg = TaxonomyRegistry.read(params.long('reg'));
					if(reg) {
						classification = reg.classification;
                        println "======WILL DELETE THIS REG ====== " + reg
                        /*if(params['fromCurationInterface']) {
                            def updateStatus = taxonService.updateTaxonName(params, reg);
                            return
                        }*/
                        def checkContributor = true;
                        if(params.fromCurationInterface) {
                            checkContributor = false;
                        }
						result = taxonService.deleteTaxonHierarchy(reg, true, checkContributor);
                        println "======the result ===== " + result
					}
					println "=======5======== " + result
					println "=======OTHER PARAMS======== " + otherParams
					if(!result.success) {
						println "=======7======== "
						msg = messageSource.getMessage("default.error.hierarchy", ['updating'] as Object[], RCU.getLocale(request))
						render ([success:false, msg:msg] as JSON)
						return;
					}
				} else {
					println "=======6======== "
					classification = params.classification ? Classification.read(params.long('classification')) : null;
				}

				println "=========TO BOOLEAN====== " + params.abortOnNewName + "==== " + params.fromCOL
				result = taxonService.addTaxonHierarchy(speciesName, t, classification, springSecurityService.currentUser, languageInstance, (params.abortOnNewName?.toBoolean()?params.abortOnNewName?.toBoolean():false) , (params.fromCOL?.toBoolean()?params.fromCOL?.toBoolean():false), otherParams);
				result.action = 'update';
				if(params.controller != 'taxon'){
					if(result.success) {
						def speciesInstance = getSpecies(result.reg.taxonDefinition.id, result.reg.taxonDefinition.rank);
						def feedInstance = activityFeedService.addActivityFeed(speciesInstance, result.reg, springSecurityService.currentUser, result.activityType);
						utilsService.sendNotificationMail(activityFeedService.SPECIES_HIERARCHY_UPDATED, speciesInstance, request, params.webaddress, feedInstance, ['info': result.activityType]);
					}
				}
				println "========MOVE TO WKG ========== " + params.moveToWKG
				if(result.success && params.moveToWKG) {
					println "========WILL MOVE=========="
					taxonService.moveToWKG(result.taxonRegistry)
				}
				result.remove("taxonRegistry");
				render result as JSON
				return;
			} catch(e) {
				e.printStackTrace();
				errors << e.getMessage();
				msg = messageSource.getMessage("default.error.hierarchy", ['editing'] as Object[], RCU.getLocale(request))
				render ([success:false, msg:msg, errors:errors] as JSON)
				return;
			}
			msg = messageSource.getMessage("default.error.hierarchy", ['editing'] as Object[], RCU.getLocale(request))
			render ([success:false, msg:msg, errors:errors] as JSON)
			return;
		} else {
			errors << messageSource.getMessage("default.error.hierarchy.missing", ['classification'] as Object[], RCU.getLocale(request))
			msg = messageSource.getMessage("default.error.hierarchy", ['editing'] as Object[], RCU.getLocale(request))
			render ([success:false, msg:msg, errors:errors] as JSON)
		}

	}

	@Secured(['ROLE_USER'])
	def delete() {
		def errors = [];
		if(params.reg) {

			//TODO        if(!speciesPermissionService.isSpeciesContributor(speciesInstance, springSecurityService.currentUser)) {
			//            return [success:false, msg:"You don't have permission to delete synonym"]
			//        }

			try {
				TaxonomyRegistry reg = TaxonomyRegistry.read(params.long('reg'));
				def result = taxonService.deleteTaxonHierarchy(reg);
				result.action = 'delete';

				if(result.success) {
					def speciesInstance = getSpecies(reg.taxonDefinition.id, reg.taxonDefinition.rank);
					def feedInstance = activityFeedService.addActivityFeed(speciesInstance, reg, springSecurityService.currentUser, result.activityType);
					utilsService.sendNotificationMail(activityFeedService.SPECIES_HIERARCHY_DELETED, speciesInstance, request, params.webaddress, feedInstance, ['info': result.activityType]);
				}

				render result as JSON;
				return;
			} catch(e) {
				e.printStackTrace();
				errors << e.getMessage();
				msg = messageSource.getMessage("default.error.hierarchy", ['deleting'] as Object[], RCU.getLocale(request))
				render ([success:false, msg:msg, errors:errors] as JSON)
				return;
			}
			render ([success:false, msg:msg, errors:errors] as JSON)
			return;
		} else {
			errors << messageSource.getMessage("default.error.hierarchy.missing", ['Id'] as Object[], RCU.getLocale(request))
			render ([success:false, msg:msg, errors:errors] as JSON)
		}
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////Navigator query related ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////


    private def getTaxonMap(taxon, author, iucn, gbif) {
        println "cheking author"
        def classifi
        def map = taxon.longestParentTaxonRegistry(author);
        if(map.regId) {
            classifi = author
        } else {
            println "checking IUCN"
            classifi = iucn
            map = taxon.longestParentTaxonRegistry(iucn);
            if(map.regId) {
            } else {
                println "chking GBIF"
                classifi = gbif
                map = taxon.longestParentTaxonRegistry(gbif);
            }
        }
        map.put('classification', classifi);
        return map;
    }

    @Secured(['ROLE_ADMIN'])
    def createIBPHierarchyForDirtylist() {
        //for all names in dirty list
        Date startDate = new Date();
        /*def taxons = TaxonomyDefinition.withCriteria {
          eq('position', NamePosition.RAW)
          eq('status', NamesMetadata.NameStatus.ACCEPTED)
          order('rank', 'asc')
          }*/
        int i = 0;
        def ibp_classifi = Classification.findByName("IBP Taxonomy Hierarchy");
        def author = Classification.findByName("Author Contributed Taxonomy Hierarchy");
        def iucn = Classification.findByName('IUCN Taxonomy Hierarchy (2010)');
        def gbif = Classification.findByName("GBIF Taxonomy Hierarchy");
        def admin = SUser.read(1L);
        def taxons;
        int limit=10;
        int offset = 0;
        def sql;
        def tmpTableName = "tmp_table_ibp_taxonconcept"

        int unreturnedConnectionTimeout = dataSource.getUnreturnedConnectionTimeout();
        try {
            dataSource.setUnreturnedConnectionTimeout(500);

            sql = new Sql(dataSource)
            String query = " select * from taxonomy_definition where id not in (select taxon_definition_id from taxonomy_registry  where classification_id=265799) and position='RAW' and status='ACCEPTED'";

            try {
                sql.executeUpdate("CREATE TABLE " + tmpTableName +  " as " + query);
            } finally {
                sql.close();
            }
        } catch(e) {
        }finally {
            log.debug "Reverted UnreturnedConnectionTimeout to ${unreturnedConnectionTimeout}";
            dataSource.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
        }

        while(true) {
            sql = new Sql(dataSource)
            println "***************************************"
            println "***************************************"
            println "*****************"+limit+"       "+offset;
            println "***************************************"
            println "***************************************"
            //        taxons = species.TaxonomyDefinition.executeQuery("from TaxonomyDefinition t where t.id not in (select taxonDefinition.id from TaxonomyRegistry r where r.classification.id=265799) and position='RAW' and status = 'ACCEPTED' order by rank", [max:limit, offset:offset]);
            String sqlStr = "select * from "+tmpTableName;
            taxons = sql.rows(sqlStr, offset, limit);
            if(!taxons) break;
            taxons.each { t -> 
                println t;
                def taxon = TaxonomyDefinition.read(t.id);
                TaxonomyDefinition.withNewTransaction { 
                    def map = getTaxonMap(taxon, author, iucn, gbif);
                    def hierarchyNodes;
                    def reg;
                    if(map.regId) {
                        hierarchyNodes = map.get(map.get('classification'));
                    }
                    if(hierarchyNodes) {
                        def taxonNames = new ArrayList(10);
                        int j
                        for(j = hierarchyNodes.size()-2; j>=0; j--) {
                            //check which node high up in the hierarchy is in WORKING status
                            if(hierarchyNodes[j].position == NamePosition.WORKING) {
                                //update path from the node with WORKING path.
                                def map2 = hierarchyNodes[j].parentTaxonRegistry(ibp_classifi);
                                def reg2 = map2.get(ibp_classifi);
                                reg2.each{
                                    taxonNames[it.rank] = it.name;
                                }
                                break;
                            } 
                        }
                        for(int k=j+1; k<hierarchyNodes.size(); k++ ) {
                            taxonNames[hierarchyNodes[k].rank] =  hierarchyNodes[k].name;
                        }
                        def r= taxonService.addTaxonHierarchy(null, taxonNames, ibp_classifi, admin, null, false, false, null);
                    } else {
                        println "No hierarchy"
                    }
                }
            }
            offset = offset + limit;
        }

        sql = new Sql(dataSource)
        try {
            sql.executeUpdate("DROP TABLE " + tmpTableName);
        } finally {
            sql.close();
        }

        println "      total time  " + ((new Date()).getTime() - startDate.getTime())/1000;
    }



}

