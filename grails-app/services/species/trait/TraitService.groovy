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

class TraitService extends AbstractObjectService {

    static transactional=false;

    Map upload(String file, Map params, UploadLog dl) {
        //def request = WebUtils.retrieveGrailsWebRequest()?.getCurrentRequest();
        Language languageInstance = utilsService.getCurrentLanguage();
        println languageInstance;
println "------------------------____"
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
println headers;
        int traitNameHeaderIndex = -1;
        int taxonIdHeaderIndex = -1;
        for(int i=0; i<headers.size(); i++) {
            if(headers[i].trim().equalsIgnoreCase('trait')) {
                traitNameHeaderIndex = i;
            }
            if(headers[i].trim().equalsIgnoreCase('taxon id')) {
                taxonIdHeaderIndex = i;
            }
        }

        if (traitNameHeaderIndex == -1 || taxonIdHeaderIndex == -1) {
            dl.writeLog("Trait name column and/or taxonId column is not defined", Level.ERROR);
            return ['noOfTraitsLoaded':noOfTraitsLoaded, 'msg':logMsgs];
        }

        while(row) {
            if(row[traitNameHeaderIndex] == null || row[traitNameHeaderIndex] == '' || row[taxonIdHeaderIndex] == null || row[taxonIdHeaderIndex] == '') {
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

            taxons_scope.each { taxon_scope ->

                Trait trait = Trait.findByNameAndTaxon(row[traitNameHeaderIndex], taxon_scope);
                if(!trait) {
                    dl.writeLog("Creating new trait with name ${row[traitNameHeaderIndex]} and taxon ${taxon_scope}");
                    trait = new Trait();
                } else {
                    dl.writeLog("Updating trait ${trait} with name ${row[traitNameHeaderIndex]} and taxon ${taxon_scope}");
                }

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
                        trait.icon = row[index].trim();
                        break;

                        case 'taxon id':
                        //TODO: if taxon id is wrong catch exception/trow exception
                        trait.taxon = taxon_scope;
                        break;

                        case 'trait definition':
                        trait.description = row[index].trim();
                        break;

                        case 'spm':
                        trait.field = getField(row[index], languageInstance);
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
            }
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

    private  getTaxon(String taxonList) {
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

        for(int i=0; i<headers.size(); i++) {
            if(headers[i].equalsIgnoreCase('trait')) {
                traitNameHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('value')) {
                valueHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('taxon id')) {
                taxonIdHeaderIndex = i;
            }
        }
        println headers
        if (traitNameHeaderIndex == -1 || valueHeaderIndex == -1 || taxonIdHeaderIndex == -1) {
            dl.writeLog("Some of trait name, value and taxonid columns are not defined", Level.ERROR);
            return ['noOfvalueLoaded':noOfValuesLoaded, 'msg':logMsgs];
        }

        while(row) {

            if(row[traitNameHeaderIndex] == null || row[traitNameHeaderIndex] == '' || row[valueHeaderIndex] == null || row[valueHeaderIndex] == '' || row[taxonIdHeaderIndex] == null || row[taxonIdHeaderIndex] == '') {
                dl.writeLog("Ignoring row " + row, Level.WARN);
                row = reader.readNext();
                continue;
            }

            TaxonomyDefinition taxon;
            try {
                taxon = TaxonomyDefinition.read(Long.parseLong(row[taxonIdHeaderIndex].trim()));
                if(!taxon){
                    dl.writeLog("Cannot find taxon " + row[taxonIdHeaderIndex].trim(), Level.ERROR);
                }
            } catch(e) {
                dl.writeLog("Error getting taxon from ${row[taxonIdHeaderIndex]} : ${e.getMessage()}", Level.ERROR);
                e.printStackTrace();
            }

            Trait trait;
            try {
                trait = Trait.findByNameAndTaxon(row[traitNameHeaderIndex].trim(), taxon);
                if(!trait){
                    dl.writeLog("Cannot find trait ${row[traitNameHeaderIndex]} and ${row[taxonIdHeaderIndex]}", Level.ERROR);
                }
            } catch(e) {
                dl.writeLog("Error getting trait from ${row[traitNameHeaderIndex]} and ${row[taxonIdHeaderIndex]} : ${e.getMessage()}", Level.ERROR);
                e.printStackTrace();
            }

            TraitValue traitValue = TraitValue.findByValueAndTrait(row[valueHeaderIndex].trim(), trait);

            if(!traitValue) {
                dl.writeLog("Creating new trait value ${row[valueHeaderIndex]} for trait ${trait.name} and taxon ${taxon}");
                traitValue = new TraitValue();
            } else {
                dl.writeLog("Updating trait value ${traitValue} with ${row[valueHeaderIndex]} for trait ${trait.name} and taxon ${taxon}");
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
                    traitValue.icon=row[index].trim()
                    break;
                    case 'value definition' : 
                    traitValue.description=row[index].trim()
                    break;
                    case 'taxon id' : 
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

    private boolean validateTrait(Trait trait, String value){
       // Trait traitObj = Trait.findById(trait);
        boolean rValue
        trait.dataTypes.each{
            switch(it) {

                case "BOOLEAN":
                if(Boolean.parseBoolean(value)){
                    rValue=true;
                }
                break;

                case "NUMERIC":
                if(Integer.parseInt(value)){
                    rValue=true;
                }
            }
        }
        return rValue;
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
        def instanceList = hqlQuery.list();

        allInstanceCountQuery.setProperties(queryParts.queryParams)
        allInstanceCount = allInstanceCountQuery.list()[0]

        queryParts.queryParams.trait = params.trait;

        return [instanceList:instanceList, instanceTotal:allInstanceCount, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
    }
    
    def getFilterQuery(params) {

        Map queryParams = [isDeleted : false]
        Map activeFilters = [:]

        String query = "select "
        String taxonQuery = '';

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

        if(params.taxon) {
            def taxon = TaxonomyDefinition.read(params.taxon.toLong());
            if(taxon) {
                queryParams['taxon'] = taxon.id;
                activeFilters['taxon'] = taxon.id;
                //taxonQuery = " join obv.taxon.hierarchies reg ";
                //query += taxonQuery;

                def classification;
                if(params.classification)
                    classification = Classification.read(Long.parseLong(params.classification))
                if(!classification)
                    classification = Classification.findByName(grailsApplication.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY);

                List parentTaxon = taxon.parentTaxonRegistry(classification).get(classification).collect {it.id};
                queryParams['classification'] = classification.id 
                queryParams['parentTaxon'] = parentTaxon 
                activeFilters['classification'] = classification.id
    
                filterQuery += " and obv.taxon.id in (:parentTaxon) ";

            }
        }
        
		def allInstanceCountQuery = "select count(*) from Trait obv " +taxonQuery+" "+((params.tag)?tagQuery:'')+((params.featureBy)?featureQuery:'')+filterQuery
	
        orderByClause = " order by " + orderByClause;

        return [query:query, allInstanceCountQuery:allInstanceCountQuery, filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

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

}
