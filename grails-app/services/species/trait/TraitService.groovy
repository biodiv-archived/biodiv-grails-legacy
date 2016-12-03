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
        uploadTraitValues(params.tvFile, dl, languageInstance);
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
        int noOfTraitsLoaded = 0;
        dl.writeLog("Loading trait definitions from ${file}", Level.INFO);

        CSVReader reader = getCSVReader(new File(file))
        String[] headers = reader.readNext();//headers
        String[] row = reader.readNext();
        int traitNameHeaderIndex = -1;
        int taxonIdHeaderIndex = -1;
        int traitIdHeaderIndex = -1;
        int updateHeaderIndex = -1;
        for(int i=0; i<headers.size(); i++) {
            if(headers[i].trim().equalsIgnoreCase('trait')) {
                traitNameHeaderIndex = i;
            } else if(headers[i].trim().equalsIgnoreCase('taxonid')) {
                taxonIdHeaderIndex = i;
            } else if(headers[i].trim().equalsIgnoreCase('traitid')) {
                traitIdHeaderIndex = i;
            } else if(headers[i].trim().equalsIgnoreCase('new/update')) {
                updateHeaderIndex = i;
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
            if(row[updateHeaderIndex]?.equalsIgnoreCase('update')) {
                if(row[traitIdHeaderIndex]) {
                    trait = Trait.get(Long.parseLong(row[traitIdHeaderIndex]));
                    dl.writeLog("Updating trait ${trait} with name ${row[traitNameHeaderIndex]} and taxon ${taxon_scope}");
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
                dl.writeLog("Creating new trait with name ${row[traitNameHeaderIndex]}");
            }

            if(trait) {
                //trait = new Trait();
                headers.eachWithIndex { header, index ->

                    switch(header.toLowerCase()) {

                        case 'trait' :
                        //traitInstance = Trait.findByName(row[index].toLowerCase().trim())
                        //if(!traitInstance){trait.name = row[index].toLowerCase().trim();}
                        //else{i 
                        if(!trait.name) trait.name = row[index].trim();
                        //}
                        break;

                        /*case 'values' : 
                        if(!traitInstance){trait.values = row[index].trim()}
                        else{traitInstance.values = row[index].trim()}
                        break;
                         */
                        case 'datatype' : 
                        trait.dataTypes = Trait.fetchDataTypes(row[index].trim());
                        break;

                        case 'traittype' :
                        trait.traitTypes = Trait.fetchTraitTypes(row[index].trim());
                        break;

                        case 'units' : 
                        trait.units = Trait.fetchUnits(row[index].trim());
                        break;

                        case 'trait source' : 
                        trait.source = row[index].trim();
                        break;

                        case 'trait icon' : 
                        trait.icon = migrateIcons(row[index].trim(), traitResourceDir);
                        break;

                        case 'taxonid':
                        //TODO: if taxon id is wrong catch exception/trow exception
                        trait.taxon?.clear();
                        taxons_scope.each { taxon_scope ->
                            trait.addToTaxon(taxon_scope);
                        }
                        break;

                        case 'trait definition':
                        trait.description = row[index].trim();
                        break;

                        case 'spm':
                        trait.field = getField(row[index], languageInstance);
                        break;

                        case 'isobvtrait':
                        trait.isNotObservationTrait = !row[index]?.trim()?.toBoolean();
                        break;

                        case 'isparticipatory':
                        trait.isParticipatory = row[index]?.trim()?.toBoolean();
                        break;

                        case 'showinobservation':
                        trait.showInObservation = row[index]?.trim()?.toBoolean();
                        break;


                    } 
                }

                if(!trait.hasErrors() && trait.save(flush:true)) {
                    dl.writeLog("Successfully inserted/updated trait");
                    noOfTraitsLoaded++;
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

        for(int i=0; i<headers.size(); i++) {
            if(headers[i].equalsIgnoreCase('trait')) {
                traitNameHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('value')) {
                valueHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('taxonid')) {
                taxonIdHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('traitid')) {
                traitIdHeaderIndex = i;
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
            try {
                if(row[traitIdHeaderIndex]) {
                    trait = Trait.read(Long.parseLong(row[traitIdHeaderIndex]));
                } else if(taxonIdHeaderIndex != -1 && row[taxonIdHeaderIndex] && row[taxonIdHeaderIndex]!= '') {
                    List traits = Trait.executeQuery("select t from Trait t join t.taxon taxon where t.name=? and taxon.id = ?", [row[traitNameHeaderIndex], Long.parseLong(row[taxonIdHeaderIndex])]);
                    if(traits?.size() == 1)
                        trait = traits[0];
                } else {
                    List traits = Trait.executeQuery("select t from Trait t where t.name=? ", [row[traitNameHeaderIndex]]);
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


            TraitValue traitValue = TraitValue.findByValueAndTrait(row[valueHeaderIndex].trim(), trait);

            if(!traitValue) {
                dl.writeLog("Creating new trait value ${row[valueHeaderIndex]} for trait ${trait.name}");
                traitValue = new TraitValue();
            } else {
                dl.writeLog("Updating trait value ${traitValue} with ${row[valueHeaderIndex]} for trait ${trait.name}");
            }

            headers.eachWithIndex { header, index ->
                switch(header.toLowerCase()) {
                    case 'trait' :
                    traitValue.trait = trait;
                    break;
                    case 'value' :
                    traitValue.value=row[index].trim();
                    break;
                    case 'value source' : 
                    traitValue.source=row[index].trim()
                    break;
                    case 'value icon' : 
                    traitValue.icon=migrateIcons(row[index].trim(),traitResourceDir);
                    break;
                    case 'value definition' : 
                    traitValue.description=row[index].trim()
                    break;
                    //case 'taxon id' : 
                    //traitValue.taxon = taxon
                    break;
                } 
            }

            if(!traitValue.hasErrors() && traitValue.save(flush:true)) {
                dl.writeLog("Successfully inserted/updated trait value");
                noOfValuesLoaded++;
            }
            else {
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

        queryParts.queryParams.trait = params.trait;

        return [instanceList:instanceList, instanceTotal:allInstanceCount, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
    }

    def getFilterQuery(params) {

        def allSGroupId = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.ALL).id
        def othersSGroupId = SpeciesGroup.findByName(grailsApplication.config.speciesPortal.group.OTHERS).id


        Map queryParams = [isDeleted : false]
        Map activeFilters = [:]

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

                filterQuery += ' and obv.showInObservation = :showInObservation '
                queryParams['showInObservation'] = true ;
 
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
                    classification = Classification.read(Long.parseLong(params.classification))
                        if(!classification)
                            classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);

                List parentTaxon = taxon.parentTaxonRegistry(classification).get(classification).collect {it.id};
                queryParams['classification'] = classification.id 
                    queryParams['parentTaxon'] = parentTaxon 
                    activeFilters['classification'] = classification.id

                    filterQuery += " and taxon.id in (:parentTaxon) or taxon.id is null";

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

        def allInstanceCountQuery = "select count(*) from Trait obv " +taxonQuery+" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery

        orderByClause = " order by " + orderByClause;
        return [query:query, allInstanceCountQuery:allInstanceCountQuery, filterQuery:filterQuery+groupByQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

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


    def migrateIcons(icon, usersDir){
        if(!icon) return;
        def rootDir = grailsApplication.config.speciesPortal.traits.rootDir
        File file = utilsService.getUniqueFile(usersDir, Utils.generateSafeFileName(icon));
        File fi = new File(grailsApplication.config.speciesPortal.content.rootDir +"/trait/"+icon);
        (new AntBuilder()).copy(file: fi, tofile: file)
        ImageUtils.createScaledImages(file, usersDir,true);
        def file_name = file.name.toString();
        return usersDir.absolutePath.replace(rootDir, "")+'/'+file_name;

    }
}
