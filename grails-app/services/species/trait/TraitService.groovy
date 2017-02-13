package species.trait

import species.participation.DownloadLog;
import content.eml.UFile;
import groovy.sql.Sql
import species.sourcehandler.importer.AbstractImporter;
import species.sourcehandler.importer.CSVTraitsImporter;
import org.apache.commons.io.FilenameUtils;
import java.util.Date;
import species.auth.SUser;
import species.sourcehandler.XMLConverter;
import species.TaxonomyDefinition;
import species.ScientificName.TaxonomyRank

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import species.trait.Trait;
import species.Species;
import species.Field;
import species.trait.Trait.DataTypes;
import species.trait.Trait.TraitTypes;
import species.trait.Trait.Units;
import species.formatReader.SpreadsheetReader;
import species.Language;
import species.License;
import org.hibernate.FlushMode;
import species.AbstractObjectService;
import org.codehaus.groovy.grails.web.util.WebUtils;
import species.AbstractObjectService;
import species.participation.UploadLog;
import grails.converters.JSON;
import org.apache.log4j.Level;
import species.Classification;
import species.trait.Fact;
import species.trait.TraitValue;
import species.utils.Utils;
import species.groups.SpeciesGroup;
import species.TaxonomyDefinition;
import species.utils.ImageUtils;
import species.utils.ImageType;


class TraitService extends AbstractObjectService {

    static transactional=false;
    Map upload(String file, Map params, UploadLog dl) {
        //def request = WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest();
        Language languageInstance = utilsService.getCurrentLanguage();
        Map result = uploadTraitDefinitions(file, dl, languageInstance);
        if(params.tvFile){
            uploadTraitValues(params.tvFile, dl, languageInstance);            
        }
        return result;
    }

    //TO BE DELETED AND MOVED TO UTILSSERVICE
    private CSVReader getCSVReader(File file) {
        char separator = '\t'
        if(file.exists()) {
            CSVReader reader = new CSVReader(new FileReader(file), separator, CSVWriter.NO_QUOTE_CHARACTER);
            return reader
        }
        return null;
    }

    Map uploadTraitDefinitions(String file, UploadLog dl, Language languageInstance) {
        if(!file){
            dl.writeLog("Not Loading trait definitions ", Level.INFO);
            return [msg:"No Traits Load here !!!!"]
        }
        int noOfTraitsLoaded = 0;
        dl.writeLog("Loading trait definitions from ${file}", Level.INFO);

        CSVReader reader = getCSVReader(new File(file))
        String[] headers = reader.readNext();//headers
        String[] row = reader.readNext();
        int traitNameHeaderIndex = -1;
        int taxonIdHeaderIndex = -1;
        int traitIdHeaderIndex = -1;
        int updateHeaderIndex = -1;
        int languageHeaderIndex = -1;
        for(int i=0; i<headers.size(); i++) {
            if(headers[i].trim().equalsIgnoreCase('trait')) {
                traitNameHeaderIndex = i;
            } else if(headers[i].trim().equalsIgnoreCase('taxonid')) {
                taxonIdHeaderIndex = i;
            } else if(headers[i].trim().equalsIgnoreCase('traitid')) {
                traitIdHeaderIndex = i;
            } else if(headers[i].trim().equalsIgnoreCase('new/update')) {
                updateHeaderIndex = i;
            }else if(headers[i].trim().equalsIgnoreCase('language')) {
                languageHeaderIndex = i;
            }
        }
        if (traitNameHeaderIndex == -1 || taxonIdHeaderIndex == -1 || updateHeaderIndex == -1) {
            dl.writeLog("Trait name column and/or taxonId column or update column is not defined", Level.ERROR);
            return ['noOfTraitsLoaded':noOfTraitsLoaded, 'msg':"Trait name column and/or taxonId column or update column is not defined"];
        }

        def rootDir = grailsApplication.config.speciesPortal.traits.rootDir
        File traitResourceDir = new File(rootDir);
        if(!traitResourceDir.exists()) {
            traitResourceDir.mkdir();
        }
        traitResourceDir = new File(traitResourceDir, UUID.randomUUID().toString()+File.separator+"resources");
        traitResourceDir.mkdirs();
        String resourceFromDir = (new File(file)).getParent();

        while(row) {
            if(row[traitNameHeaderIndex] == null || row[traitNameHeaderIndex] == '') {
                dl.writeLog("Ignoring row " + row, Level.WARN);
                row = reader.readNext();
                continue;
            }

            List taxons_scope = [];
            row[taxonIdHeaderIndex].tokenize(",").each { taxonId ->
                try {
                    TaxonomyDefinition t = TaxonomyDefinition.read(Long.parseLong(taxonId?.trim()));
                    if(t) taxons_scope << t;
                    else {
                        dl.writeLog("Cannot find taxon " + taxonId, Level.ERROR);
                    }
                } catch(e) {
                    dl.writeLog("Error getting taxon from ${taxonId} : ${e.getMessage()}", Level.ERROR);
                    e.printStackTrace();
                }
            }

            //            taxons_scope.each { taxon_scope ->
            Trait trait = null;
            TraitTranslation traitTranslation = null;
            languageInstance = Language.findByName(row[languageHeaderIndex].trim());
            boolean isTranslate = false;
            if(row[updateHeaderIndex]?.equalsIgnoreCase('update')) {
                if(row[traitIdHeaderIndex]) {
                    println "------------------------------------"+ row[traitIdHeaderIndex]
                    trait = Trait.get(Long.parseLong(row[traitIdHeaderIndex]));
                    println "----------------trait" + trait
                    println "----------------languageInstance" + languageInstance                    
                    if(trait && languageInstance){
                        traitTranslation = TraitTranslation.findByTraitAndLanguage(trait,languageInstance);
                        if(!traitTranslation){
                            traitTranslation = new TraitTranslation();  
                            isTranslate = true;          
                        }
                    }else{
                        println "-----------------------------------------------------"
                        println traitTranslation
                        dl.writeLog("Updating trait ${trait} with name ${row[traitNameHeaderIndex]} and taxon ${taxon_scope}");    
                    }
                    println "-----------------------------------------------------"
                    println traitTranslation
                    dl.writeLog("Updating trait ${trait} with name ${row[traitNameHeaderIndex]}");
                } 
                else {
                    if(taxon_scope.size() == 1) {
                        trait = Trait.executeQuery("select t from Trait t join t.taxon taxon where t.name=? and taxon = ?", [row[traitNameHeaderIndex],  taxons_scope[0]]);
                    } else {
                        dl.writeLog("Trait id is required to update trait ${row[traitNameHeaderIndex]}", Level.ERROR);
                    }
                }
            } else if( row[updateHeaderIndex]?.equalsIgnoreCase('new') ){
                trait = new Trait();
                traitTranslation = new TraitTranslation();
                dl.writeLog("Creating new trait with name ${row[traitNameHeaderIndex]}");
            }

            if(trait && traitTranslation) {
                //trait = new Trait();
                headers.eachWithIndex { header, index ->

                    switch(header.toLowerCase()) {

                        case 'trait' :
                        //traitInstance = Trait.findByName(row[index].toLowerCase().trim())
                        //if(!traitInstance){trait.name = row[index].toLowerCase().trim();}
                        //else{i 
                        if(!isTranslate) trait.name = row[index].trim();
                        traitTranslation.name=row[index].trim();
                        //}
                        break;

                        /*case 'values' : 
                        if(!traitInstance){trait.values = row[index].trim()}
                        else{traitInstance.values = row[index].trim()}
                        break;
                         */
                        case 'datatype' : 
                        if(!isTranslate) trait.dataTypes = Trait.fetchDataTypes(row[index].trim());
                        break;

                        case 'traittype' :
                        if(!isTranslate) trait.traitTypes = Trait.fetchTraitTypes(row[index].trim());
                        break;

                        case 'units' : 
                        if(!isTranslate) trait.units = Trait.fetchUnits(row[index].trim());
                        break;

                        case 'trait source' : 
                        if(!isTranslate) trait.source = row[index].trim();
                        traitTranslation.source=row[index].trim();
                        break;

                        case 'trait icon' : 
                        if(!isTranslate) trait.icon = migrateIcons(row[index].trim(), traitResourceDir, resourceFromDir);
                        break;

                        case 'taxonid':
                        //TODO: if taxon id is wrong catch exception/trow exception                        
                        if(!isTranslate){
                            trait.taxon?.clear();
                            taxons_scope.each { taxon_scope ->
                                trait.addToTaxon(taxon_scope);
                            }
                        }
                        break;

                        case 'trait definition':
                        if(!isTranslate) trait.description = row[index].trim();
                        traitTranslation.description=row[index].trim();
                        break;

                        case 'spm':
                        if(!isTranslate){
                            trait.field = getField(row[index], languageInstance);
                        }
                        break;

                        case 'isobvtrait':
                        if(!isTranslate) trait.isNotObservationTrait = !row[index]?.trim()?.toBoolean();
                        break;

                        case 'isparticipatory':
                        if(!isTranslate) trait.isParticipatory = row[index]?.trim()?.toBoolean();
                        break;

                        case 'showinobservation':
                        if(!isTranslate) trait.showInObservation = row[index]?.trim()?.toBoolean();
                        break;


                    } 
                }
                if(isTranslate){
                    traitTranslation.language = languageInstance;
                    traitTranslation.trait = trait;
                    if(!traitTranslation.hasErrors() && traitTranslation.save(flush:true)) {
                        println "Successfully inserted/updated traitTranslation"
                        dl.writeLog("Successfully inserted/updated traitTranslation");                        
                    }else{
                        dl.writeLog("Failed to save TraitTranslation", Level.ERROR);
                        println "Failed to save TraitTranslation"
                        println traitTranslation;
                    }
                }else if(!trait.hasErrors() && trait.save(flush:true)) {
                    dl.writeLog("Successfully inserted/updated trait");
                    noOfTraitsLoaded++;
                    traitTranslation.language = languageInstance;
                    traitTranslation.trait = trait;
                    if(!traitTranslation.hasErrors() && traitTranslation.save(flush:true)) {
                        println "Successfully inserted/updated traitTranslation"
                        dl.writeLog("Successfully inserted/updated traitTranslation");                        
                    }else{
                        dl.writeLog("Failed to save TraitTranslation", Level.ERROR);
                        println "Failed to save TraitTranslation"
                        println traitTranslation;
                    }
                } else {
                    dl.writeLog("Failed to save trait", Level.ERROR);
                    trait.errors.allErrors.each { 
                        dl.writeLog(it.toString(), Level.ERROR); 
                    }

                }
            } else {
                dl.writeLog("Not a valid update/new column ${row[updateHeaderIndex]}", Level.ERROR);
            }


            //}
            row = reader.readNext();
        }

        dl.writeLog("\n====================================\nSuccessfully added ${noOfTraitsLoaded} traits\n====================================\n");
        return ['success':true, 'msg':"Loaded ${noOfTraitsLoaded} traits."];
    }

    private Field getField(String string, Language languageInstance) {

        def f = string.tokenize("|");

        Field field;
        if(f.size() == 1) {
            f = Field.findByLanguageAndConcept(languageInstance, f[0].trim());
        } else if (f.size() == 2) {
            f = Field.findByLanguageAndConceptAndCategory(languageInstance, f[0].trim(), f[1].trim());
        } else  if (f.size() == 3) {
            f = Field.findByLanguageAndConceptAndCategoryAndSubCategory(languageInstance, f[0].trim(), f[1].trim(), f[2].trim());
        } 
    }

    private getTaxon(String taxonList) {
        def x = TaxonomyDefinition.read(Long.parseLong(taxonList.trim()));
        if(x) {
            return x;
        }
    }

    Map uploadTraitValues(String file, UploadLog dl, Language languageInstance) {
        int noOfValuesLoaded=0;

        dl.writeLog("Loading trait values from ${file}", Level.INFO);

        CSVReader reader = getCSVReader(new File(file))
        String[] headers = reader.readNext();//headers
        String[] row = reader.readNext();

        int traitNameHeaderIndex = -1;
        int valueHeaderIndex = -1;
        int taxonIdHeaderIndex=-1;
        int traitIdHeaderIndex=-1;
        int updateHeaderIndex=-1;
        int valueIdHeaderIndex=-1;
        int languageHeaderIndex = -1;
        for(int i=0; i<headers.size(); i++) {
            if(headers[i].equalsIgnoreCase('trait')) {
                traitNameHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('value')) {
                valueHeaderIndex = i;
            }
            if(headers[i].trim().equalsIgnoreCase('valueid')) {
                valueIdHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('taxonid')) {
                taxonIdHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('traitid')) {
                traitIdHeaderIndex = i;
            }
            if(headers[i].trim().equalsIgnoreCase('new/update')) {
                updateHeaderIndex = i;
            }
            if(headers[i].trim().equalsIgnoreCase('language')) {
                languageHeaderIndex = i;
            }
        }
        if (traitNameHeaderIndex == -1 || valueHeaderIndex == -1 || traitIdHeaderIndex == -1) {
            dl.writeLog("Some of trait name, value and traitid columns are not defined", Level.ERROR);
            return ['noOfvalueLoaded':noOfValuesLoaded, 'msg':"Some of trait name, value and traitid columns are not defined"];
        }

        def rootDir = grailsApplication.config.speciesPortal.traits.rootDir
        File traitResourceDir = new File(rootDir);
        if(!traitResourceDir.exists()) {
            traitResourceDir.mkdir();
        }
        traitResourceDir = new File(traitResourceDir, UUID.randomUUID().toString()+File.separator+"resources");
        traitResourceDir.mkdirs();

        String resourceFromDir = (new File(file)).getParent();

        while(row) {

            if(row[traitNameHeaderIndex] == null || row[traitNameHeaderIndex] == '' || row[valueHeaderIndex] == null || row[valueHeaderIndex] == '') {
                dl.writeLog("Ignoring row " + row, Level.WARN);
                row = reader.readNext();
                continue;
            }

            /*            TaxonomyDefinition taxon;
            try {
            taxon = TaxonomyDefinition.read(Long.parseLong(row[taxonIdHeaderIndex].trim()));
            if(!taxon){
            dl.writeLog("Cannot find taxon " + row[taxonIdHeaderIndex].trim(), Level.ERROR);
            }
            } catch(e) {
            dl.writeLog("Error getting taxon from ${row[taxonIdHeaderIndex]} : ${e.getMessage()}", Level.ERROR);
            e.printStackTrace();
            }
             */
            Trait trait;
            TraitValue traitValue;
            TraitValueTranslation traitValueTranslation = null;
            languageInstance = Language.findByName(row[languageHeaderIndex].trim());
            Boolean isTranslate =false;
            try {
                if(row[traitIdHeaderIndex]) {
                    trait = Trait.read(Long.parseLong(row[traitIdHeaderIndex]));
                } else if(taxonIdHeaderIndex != -1 && row[taxonIdHeaderIndex] && row[taxonIdHeaderIndex]!= '') {
                    List traits = Trait.executeQuery("select t from Trait t join t.taxon taxon where t.name=? and taxon.id = ?", [row[traitNameHeaderIndex], Long.parseLong(row[taxonIdHeaderIndex])]);
                    if(traits?.size() == 1)
                        trait = traits[0];
                } else {
                    List traits = Trait.executeQuery("select t from Trait t where t.name=? ", [row[traitNameHeaderIndex].trim()]);
                    if(traits?.size() == 1)
                        trait = traits[0];
                    //trait = Trait.findByNameAndTaxon(row[traitNameHeaderIndex].trim(), taxon);
                }
            } catch(e) {
                dl.writeLog("Error getting trait from ${row[traitNameHeaderIndex]} and ${row[taxonIdHeaderIndex]} : ${e.getMessage()}", Level.ERROR);
                e.printStackTrace();
            }
            if(!trait){
                dl.writeLog("Cannot find trait ${row[traitNameHeaderIndex]}", Level.ERROR);
                row = reader.readNext();
                continue;
            }
            traitValue = TraitValue.findByValueAndTrait(row[valueHeaderIndex].trim(), trait);

            if(row[updateHeaderIndex]?.equalsIgnoreCase('update')) {                
                traitValue = TraitValue.get(Long.parseLong(row[valueIdHeaderIndex].trim()))
                if(traitValue){
                    traitValueTranslation = TraitValueTranslation.findByTraitValueAndLanguage(traitValue,languageInstance);
                    if(!traitValueTranslation){
                        traitValueTranslation = new TraitValueTranslation();
                        isTranslate =true;
                    }
                }
                dl.writeLog("Updating trait value ${row[valueHeaderIndex]} has been multiple entry!!!");
            }

            if(!traitValue && !traitValueTranslation) {
                dl.writeLog("Creating new trait value ${row[valueHeaderIndex]} for trait ${trait.name}");
                traitValue = new TraitValue();
                traitValueTranslation = new TraitValueTranslation();
            }
            headers.eachWithIndex { header, index ->
                switch(header.toLowerCase()) {
                    case 'trait' :
                    if(!isTranslate) traitValue.trait = trait;
                    break;
                    case 'value' :
                    println "====="
                    println row[index];
                    if(!isTranslate) traitValue.value=row[index].trim();
                    traitValueTranslation.value=row[index].trim();
                    break;
                    case 'value source' : 
                    traitValue.source=row[index].trim()
                    traitValueTranslation.source=row[index].trim();
                    break;
                    case 'value icon' : 
                    if(!isTranslate) traitValue.icon=migrateIcons(row[index].trim(),traitResourceDir, resourceFromDir);
                    break;
                    case 'value definition' : 
                    traitValue.description=row[index].trim()
                    traitValueTranslation.description=row[index].trim();
                    break;
                    //case 'taxon id' : 
                    //traitValue.taxon = taxon
                    break;
                } 
            }
            if(isTranslate){
                traitValueTranslation.language = languageInstance;
                traitValueTranslation.traitValue = traitValue;
                if(!traitValueTranslation.hasErrors() && traitValueTranslation.save(flush:true)) {
                    println "passed here Successfully inserted/updated traitValueTranslation"
                    dl.writeLog("Successfully inserted/updated traitValueTranslation");                        
                }else{
                    dl.writeLog("Failed to save traitValueTranslation", Level.ERROR);
                    println "Failed to save traitValueTranslation"
                    println traitValueTranslation;
                }
            }else if(!traitValue.hasErrors() && traitValue.save(flush:true)) {
                dl.writeLog("Successfully inserted/updated trait value");
                noOfValuesLoaded++;
                traitValueTranslation.language = languageInstance;
                traitValueTranslation.traitValue = traitValue;
                if(!traitValueTranslation.hasErrors() && traitValueTranslation.save(flush:true)) {
                    println "------------------------Successfully inserted/updated traitValueTranslation"
                    dl.writeLog("Successfully inserted/updated traitValueTranslation");                        
                }else{
                    dl.writeLog("Failed to save traitValueTranslation", Level.ERROR);
                    println "Failed to save traitValueTranslation"
                    println traitValueTranslation;
                }
            }else {
                dl.writeLog("Failed to save trait value", Level.ERROR);
                traitValue.errors.allErrors.each { 
                    dl.writeLog(it.toString(), Level.ERROR); 
                }
            }

            row = reader.readNext();
        }

        dl.writeLog("\n====================================\nSuccessfully added ${noOfValuesLoaded} trait values\n====================================\n");
        return ['success':true, 'msg':"Loaded ${noOfValuesLoaded} trait values."];
    }

    Map getFilteredList(def params, int max, int offset) {

        def queryParts = getFilterQuery(params) 
        String query = queryParts.query;
        long allInstanceCount = 0;

        query += queryParts.filterQuery + queryParts.orderByClause

        log.debug "query : "+query;
        log.debug "allInstanceCountQuery : "+queryParts.allInstanceCountQuery;

        log.debug query;
        log.debug queryParts.queryParams;
        def allInstanceCountQuery = sessionFactory.currentSession.createQuery(queryParts.allInstanceCountQuery)

        def hqlQuery = sessionFactory.currentSession.createQuery(query)

        if(max > -1){
            hqlQuery.setMaxResults(max);
            queryParts.queryParams["max"] = max
        }
        if(offset > -1) {
            hqlQuery.setFirstResult(offset);
            queryParts.queryParams["offset"] = offset
        }

        hqlQuery.setProperties(queryParts.queryParams);
        def instanceList;
        instanceList = hqlQuery.list();

        allInstanceCountQuery.setProperties(queryParts.queryParams)
        allInstanceCount = allInstanceCountQuery.list()[0]

        if(!queryParts.queryParams.trait) 
        queryParts.queryParams.trait = params.trait;

        Sql sql = Sql.newInstance(dataSource);
        List numericTraitMinMax =  sql.rows("""
        select min(f.value::float)::integer,max(f.to_value::float)::integer,t.id from fact f,trait t where f.trait_id = t.id and t.data_types='NUMERIC' group by t.id;
        """);
        return [instanceList:instanceList, instanceTotal:allInstanceCount, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters, 'traitFactMap':queryParts.traitFactMap, 'object':queryParts.object,numericTraitMinMax:numericTraitMinMax];
    }

    def getFilterQuery(params) {

        def allSGroupId = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        def othersSGroupId = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).id


        Map queryParams = [isDeleted : false]
        Map activeFilters = [:]
        Map r;

        String query = "select "
        String taxonQuery = '';
        String groupByQuery = '';

        def orderByClause = params.sort ? "  obv." + params.sort +  " desc, obv.id asc": " obv.id asc"

        if(params.fetchField) {
            query += " obv.id as id,"
            params.fetchField.split(",").each { fetchField ->
                if(!fetchField.equalsIgnoreCase('id'))
                    query += " obv."+fetchField+" as "+fetchField+","
            }
            query = query [0..-2];
            queryParams['fetchField'] = params.fetchField
        }else if(params.filterProperty == 'nearByRelated' && !params.bounds) {
            query += " g2 "
        } 
        else {
            query += " obv "
        }
        query += " from Trait obv "

        def filterQuery = " where obv.isDeleted = :isDeleted "

        //TODO: check logic
        if(params.featureBy == "false") {
            featureQuery = ", Featured feat "
            query += featureQuery;
            filterQuery += " and obv.id != feat.objectId and feat.objectType = :featType "
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Dataset.class.getCanonicalName();
        }

        if(params.tag) {
            tagQuery = ",  TagLink tagLink "
            query += tagQuery;
            //mapViewQuery = "select obv.topology from Observation obv, TagLink tagLink "
            filterQuery +=  " and obv.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "

            queryParams["tag"] = params.tag
            queryParams["tagType"] = GrailsNameUtils.getPropertyName(Observation.class);
            activeFilters["tag"] = params.tag
        }

        /*if(params.user) {
          filterQuery += " and obv.author.id = :user "
          queryParams["user"] = params.user.toLong()
          activeFilters["user"] = params.user.toLong()
          }*/

        if(params.isFlagged && params.isFlagged.toBoolean()){
            filterQuery += " and obv.flagCount > 0 "
            activeFilters["isFlagged"] = params.isFlagged.toBoolean()
        }

        if(params.sGroup) {
            params.sGroup = params.sGroup.toLong()
            //def groupId = getSpeciesGroupIds(params.sGroup)
            if(!params.sGroup){
                log.debug("No groups for id " + params.sGroup)
            }else{
                List<TaxonomyDefinition> taxons = SpeciesGroup.read(params.sGroup)?.getTaxon(); 
                def classification;
                if(params.classification)
                    classification = Classification.read(Long.parseLong(params.classification))
                        if(!classification)
                            classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);
                List parentTaxon = [];
                taxons.each {taxon ->
                    if(taxon) {
                    parentTaxon.addAll(taxon.parentTaxonRegistry(classification).get(classification).collect {it.id});
                    }
                }
                queryParams['classification'] = classification.id; 
                queryParams['sGroup'] = params.sGroup;
                queryParams['parentTaxon'] = parentTaxon ;
                //queryParams['taxons'] = taxons;
                activeFilters['classification'] = classification.id;
                activeFilters['sGroup'] = params.sGroup;

                  if(params.showInObservation && params.showInObservation.toBoolean()){
                 filterQuery += ' and obv.showInObservation = :showInObservation '
                 queryParams['showInObservation'] = true ; 
                }

 
                taxonQuery = " left join obv.taxon taxon left join taxon.hierarchies as reg, SpeciesGroupMapping sgm ";
                query += taxonQuery;
                String inQuery = '';
                if(params.sGroup == othersSGroupId) {
                    filterQuery += " and taxon is null  ";
                } else if(params.sGroup == allSGroupId) {
                    filterQuery += " ";// and reg.classification.id = :classification and ( ${inQuery} ) and taxon is null  ";
                } else if(parentTaxon) {
                    inQuery = " taxon.id in (:parentTaxon) or " 
                    filterQuery += " and taxon is null or (reg.classification.id = :classification and ( ${inQuery} (cast(sgm.taxonConcept.id as string) = reg.path or reg.path like '%!_'||sgm.taxonConcept.id||'!_%' escape '!' or reg.path like sgm.taxonConcept.id||'!_%'  escape '!' or reg.path like '%!_' || sgm.taxonConcept.id escape '!'))) and sgm.speciesGroup.id = :sGroup ";
                }


                groupByQuery = " group by obv ";
            }
        }

        if(params.taxon) {
            def taxon = TaxonomyDefinition.read(params.taxon.toLong());
            if(taxon) {
                queryParams['taxon'] = taxon.id;
                activeFilters['taxon'] = taxon.id;
                taxonQuery = " left join obv.taxon taxon ";
                query += taxonQuery;

                def classification;
                if(params.classification)
                    classification = Classification.read(Long.parseLong(params.classification));

                if(!classification)
                    classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);

                queryParams['classification'] = classification.id; 
                activeFilters['classification'] = classification.id;
                List parentTaxon = taxon.parentTaxonRegistry(classification).get(classification).collect {it.id};
                if(parentTaxon) {
                    queryParams['parentTaxon'] = parentTaxon ;

                    filterQuery += " and taxon.id in (:parentTaxon) or taxon.id is null";
                }

            }
        }

        if(params.isObservationTrait && params.isObservationTrait.toBoolean()){
            filterQuery += " and obv.isNotObservationTrait = :isNotObservationTrait "
            queryParams["isNotObservationTrait"] = !params.isObservationTrait.toBoolean()
            activeFilters["isNotObservationTrait"] = !params.isObservationTrait.toBoolean()
        }

        if(params.isParticipatory && params.isParticipatory.toBoolean()){
            filterQuery += " and obv.isParticipatory = :isParticipatory "
            queryParams["isParticipatory"] = params.isParticipatory.toBoolean()
            activeFilters["isParticipatory"] = params.isParticipatory.toBoolean()
        }

        if(params.objectId && params.objectType){
            Long objectIdL;
            try {
                objectIdL = params.long('objectId');
            } catch(Exception e) {
                e.printStackTrace();
            }
            if(objectIdL) {
                /*query += " , Fact fact "
                filterQuery += " and fact.objectId = :objectId and fact.objectType = :objectType and fact.isDeleted = false "
                queryParams["objectId"] = objectIdL;
                queryParams["objectType"] = params.objectType;
                activeFilters["objectId"] = objectIdL;
                activeFilters["objectType"] = params.objectType;
                */
                def object = grailsApplication.getDomainClass(params.objectType).clazz.read(objectIdL);
                r = object.getTraitFacts();
                queryParams.putAll(r.queryParams);
                r['object'] = object;
            }
        }

        def allInstanceCountQuery = "select count(*) from Trait obv " +taxonQuery+" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery
        orderByClause = " order by " + orderByClause;
        return [query:query, allInstanceCountQuery:allInstanceCountQuery, filterQuery:filterQuery+groupByQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters, 'traitFactMap':r?.traitFactMap, 'object':r?.object];

    }

    def getAllFilter(filters){
        def trait,traitValue,traitFilter=[:];
        filters.each{ f -> 
            trait = Trait.findByName(f);
            if(trait){
                traitValue = TraitValue.findAllByTrait(trait);
                traitFilter[""+trait.name]=traitValue          
            }
        }
        return traitFilter
    }    

    def createTraitValue(Trait traitInstance, params){

        def valueCount = params.valueCount?params.valueCount:0;
        for(def i=1;i<=valueCount;i++){
            TraitValue traitValueInstance = new TraitValue();
            traitValueInstance.value = params["value_"+i];
            traitValueInstance.description = params["traitDesc_"+i];
            traitValueInstance.source = params["traitSource_"+i];
            traitValueInstance.trait = traitInstance
            traitValueInstance.icon = getTraitIcon(params["icon_"+i])

            if (!traitValueInstance.hasErrors() && traitValueInstance.save(flush: true)) {
                def msg = "Trait Value Added Successfully"
            }
            else{
                def errors = [];
                traitValueInstance.errors.allErrors .each {
                    def formattedMessage = messageSource.getMessage(it, null);
                    errors << [field: it.field, message: formattedMessage]
                }
            }
        }
    }

    private String getTraitIcon(String icon) {    
        if(!icon) return;
        def resource = null;
        def rootDir = grailsApplication.config.speciesPortal.traits.rootDir

        File iconFile = new File(rootDir , icon);
        if(!iconFile.exists()) {
            log.error "COULD NOT locate icon file ${iconFile.getAbsolutePath()}";
        }

        resource = iconFile.absolutePath.replace(rootDir, "");
        return resource;
    }

    def delete(params){
        log.debug "deleting trait "+params.id
        String messageCode;
        String url = utilsService.generateLink(params.controller, 'list', []);
        String label = Utils.getTitleCase(params.controller?:'trait')
        def messageArgs = [label, params.id]
        def errors = [];
        boolean success = false;
        if(!params.id) {
            messageCode = 'default.not.found.message'
        } else {
            try {
                def traitInstance = Trait.get(params.id.toLong())
                if (traitInstance) {
                    try {
                        traitInstance.isDeleted = true;

                        if(!traitInstance.hasErrors() && traitInstance.save(flush: true)){
                            messageCode = 'default.deleted.message'
                            url = utilsService.generateLink(params.controller, 'list', [])
                            success = true;
                        } else {
                            messageCode = 'default.not.deleted.message'
                            url = utilsService.generateLink(params.controller, 'show', [id: params.id])
                            traitInstance.errors.allErrors.each { log.error it }
                            traitInstance.errors.allErrors .each {
                                def formattedMessage = messageSource.getMessage(it, null);
                                errors << [field: it.field, message: formattedMessage]
                            }

                        }
                    }
                    catch (org.springframework.dao.DataIntegrityViolationException e) {
                        messageCode = 'default.not.deleted.message'
                        url = utilsService.generateLink(params.controller, 'show', [id: params.id])
                        e.printStackTrace();
                        log.error e.getMessage();
                        errors << [message:e.getMessage()];
                    }
                } 
                else {
                    messageCode = 'default.not.found.message'
                    url = utilsService.generateLink(params.controller, 'list', [])
                }
            } catch(e) {
                e.printStackTrace();
                url = utilsService.generateLink(params.controller, 'list', [])
                messageCode = 'default.not.deleted.message'
                errors << [message:e.getMessage()];
            }
        }

        String message = messageSource.getMessage(messageCode, messageArgs.toArray(), Locale.getDefault())

        return [success:success, url:url, msg:message, errors:errors]
    }

    private String migrateIcons(icon, usersDir, fromDir=null){
        if(!icon) return;
        def rootDir = grailsApplication.config.speciesPortal.traits.rootDir
        File file = utilsService.getUniqueFile(usersDir, Utils.generateSafeFileName(icon));
        if(!fromDir) fromDir = grailsApplication.config.speciesPortal.content.rootDir; 
        File fi = new File(fromDir+"/trait/"+icon);
        (new AntBuilder()).copy(file: fi, tofile: file)
        ImageUtils.createScaledImages(file, usersDir,true);
        def file_name = file.name.toString();
        return usersDir.absolutePath.replace(rootDir, "")+'/'+file_name;

    }
}
