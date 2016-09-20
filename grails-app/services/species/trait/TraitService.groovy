package species.trait

import species.participation.DownloadLog;
import content.eml.UFile;
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
import groovy.sql.Sql
import species.Language;
import species.License;


class TraitService {

    static transactional=false;

    //    static final BIODIV_NAMESPACE = "http://indiabiodiversity.org/schema/terms/";
    public static final int IMPORT_BATCH_SIZE = 10;

    //protected Dataset dataset;
    //    private String resourceURI = "http://localhost.indiabiodiversity.org/species/show/" 

    private static Map propertyValueIcons;
    def springSecurityService;
    def utilsService;
    def grailsApplication;
    def dataSource;

    void init() {
        // Make a TDB-backed dataset
        def traitsDir = new File(grailsApplication.config.speciesPortal.traits.databaseDir);
        if(!traitsDir.exists()) {
            traitsDir.mkdir();
        }
        String directory = grailsApplication.config.speciesPortal.traits.databaseDir;
        // dataset = getDataset(directory);
        // TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;

        //TODO: populating icons from file
        //TOBE removed
        CSVReader reader = getCSVReader(new File("${grailsApplication.config.speciesPortal.traits.traitValueFile}"))
        reader.readNext();//headers

        propertyValueIcons = [:];
        String[] row = reader.readNext();
        while(row) {
            for(int i=0; i<1; i++) {
                if(row[i]) {
                    row[i] = row[i].trim().toLowerCase().replaceAll('\\s+', '_');
                }
            }
            if(row[0] && row[1] && row[2]) {
                if(!propertyValueIcons.get(row[0])) {
                    propertyValueIcons[row[0]]= [:];
                }
                propertyValueIcons[row[0]][row[1].trim().toLowerCase().replaceAll('\\s+', '_')] = row[2];
            }
            row = reader.readNext();
        }
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

    private Long findTaxonIdFromName(String name) {
        XMLConverter converter = new XMLConverter();
        TaxonomyDefinition taxon = converter.getTaxonConceptFromName(name, TaxonomyRank.SPECIES.ordinal(), false, null);
        return taxon ? taxon.id : null;
    }

    private boolean isValidTrait(String traitName, traitValue, Long id) {
        println "Validating trait ${traitName} with value ${traitValue} for taxon ${id}"
        //FIX: HACK till loading of traits and validation is inplace
        //return true;

        Trait trait = Trait.findByName(traitName);
        //if(!trait) return false;
        boolean isValid = false;
        isValidType(traitName,traitValue,trait)
        return true;
        trait.valueContraints.each { constraint ->
            isValid = isValid && constraint.validate(trait, traitValue);
        }
        return isValid && isValidPropertyAsPerTaxon(trait, id);
    }

    private boolean isValidType (String traitName, TraitValue traitValue, Trait trait){
        //Trait trait=Trait.findByName(traitName);
        //ValueConstraint traitvalueconstraint= ValueConstraint.findAllByTraitId(trait.id.toLong())
        boolean isValid = false;
        println "==========Value Constarints========"+trait?.valueConstraint
        println "======Trait========"+trait?.id
        println "=======Trait Name========"+traitName
        println "========Trait Value======="+traitValue

        trait?.valueConstraint.each {
            def value = it?.toUpperCase().trim()
            switch(value){
                case 'CATEGORICAL':
                break;
                case 'INTEGER':
                Integer.parseInt(value)
                break;
                case 'FLOAT':
                Double.parseDouble(value)
                break;
                case 'TEXT':
                break;
                case 'BOOLEAN':
                println "====Boolean====="+Boolean.parseBoolean(value)
                break;
                case 'ORDERED':
                break;
                case 'RANGE':
                return value.indexOf('-') != -1
                break;
                case 'DATE':
                return UtilsService.parseDate(value) != null
            }
        }
        return true
    }

    private boolean isValidPropertyAsPerTaxon(Trait trait, Long id) {
        TaxonomyDefinition taxonInstance = TaxonomyDefinition.read(id);
        if(!taxonInstance || !trait) return false;
        List<TaxonomyDefinition> parentTaxon = taxonInstance.parentTaxon();
        return parentTaxon.intersect(trait.taxon).size() > 0
    }

    Map loadTraitDefinitions(String file, Language languageInstance) {
        int noOfTraitsLoaded = 0;
        List<String> logMsgs = [];

        log.info "Loading trait definitions from ${file}";
        logMsgs << "Loading trait definitions from ${file}";

        CSVReader reader = getCSVReader(new File(file))
        String[] headers = reader.readNext();//headers
        String[] row = reader.readNext();
println headers;
        int traitNameHeaderIndex = -1;
        int taxonIdHeaderIndex = -1;
        for(int i=0; i<headers.size(); i++) {
            if(headers[i].equalsIgnoreCase('trait')) {
                traitNameHeaderIndex = i;
            }
            if(headers[i].equalsIgnoreCase('taxon id')) {
                taxonIdHeaderIndex = i;
            }
        }

        if (traitNameHeaderIndex == -1 || taxonIdHeaderIndex == -1) {
            log.error "Trait name column and/or taxonId column is not defined";
            logMsgs << "Trait name column and/or taxonId column is not defined";
            return ['noOfTraitsLoaded':noOfTraitsLoaded, 'msg':logMsgs];
        }

        while(row) {
            if(row[traitNameHeaderIndex] == null || row[traitNameHeaderIndex] == '' || row[taxonIdHeaderIndex] == null || row[taxonIdHeaderIndex] == '') {
                log.error "Ignoring row ${row}";
                logMsgs << "Ignoring row " + row;
                row = reader.readNext();
                continue;
            }

            List taxons_scope = [];
            row[taxonIdHeaderIndex].tokenize(",").each { taxonId ->
                try {
                    TaxonomyDefinition t = TaxonomyDefinition.read(Long.parseLong(taxonId?.trim()));
                    if(t) taxons_scope << t;
                    else {
                        log.error "Cannot find taxon ${taxonId}";
                        logMsgs << "Cannot find taxon " + taxonId;
                    }
                } catch(e) {
                    log.error "Error getting taxon from ${taxonId} : ${e.getMessage()}";
                    logMsgs << "Error getting taxon from ${taxonId} : ${e.getMessage()}";
                    e.printStackTrace();
                }
            }

            taxons_scope.each { taxon_scope ->

                Trait trait = Trait.findByNameAndTaxon(row[traitNameHeaderIndex], taxon_scope);
                if(!trait) {
                    log.debug "Creating new trait for ${row[traitNameHeaderIndex]} and taxon ${taxon_scope}";
                    logMsgs << "Creating new trait for ${row[traitNameHeaderIndex]} and taxon ${taxon_scope}";
                    trait = new Trait();
                } else {
                    log.debug "Updating trait ${trait} for ${row[traitNameHeaderIndex]} and taxon ${taxon_scope}";
                    logMsgs << "Updating trait ${trait} for ${row[traitNameHeaderIndex]} and taxon ${taxon_scope}";
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

                if(!trait.hasErrors() && !trait.save(flush:true)) {
                    log.error "Failed to save trait";
                    logMsgs <<  "Failed to save trait";
                    trait.errors.allErrors.each { 
                        log.error it 
                        logMsgs <<  it 
                    }
                } else {
                    log.debug "Successfully inserted/updated trait ${trait.name} : ${trait.taxon}";
                    noOfTraitsLoaded++;
                }
            }
            row = reader.readNext();
        }
        return ['noOfTraitsLoaded':noOfTraitsLoaded, 'msg':logMsgs];
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

    Map loadTraitValues(String file, Language languageInstance) {
        int noOfValuesLoaded=0;
        List<String> logMsgs = [];

        log.info "Loading trait values from ${file}";
        logMsgs << "Loading trait values from ${file}";

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
            log.error "Some of trait name, value and taxonid columns are not defined";
            logMsgs << "Some of trait name, value and taxonid columns are not defined";
            return ['noOfvalueLoaded':noOfValuesLoaded, 'msg':logMsgs];
        }

        while(row) {

            if(row[traitNameHeaderIndex] == null || row[traitNameHeaderIndex] == '' || row[valueHeaderIndex] == null || row[valueHeaderIndex] == '' || row[taxonIdHeaderIndex] == null || row[taxonIdHeaderIndex] == '') {
                log.error "Ignoring row ${row}";
                logMsgs << "Ignoring row " + row;
                row = reader.readNext();
                continue;
            }

            TaxonomyDefinition taxon;
            try {
                taxon = TaxonomyDefinition.read(Long.parseLong(row[taxonIdHeaderIndex].trim()));
                if(!taxon){
                    log.error "Cannot find taxon ${row[taxonIdHeaderIndex]}";
                    logMsgs << "Cannot find taxon " + row[taxonIdHeaderIndex];
                }
            } catch(e) {
                log.error "Error getting taxon from ${row[taxonIdHeaderIndex]} : ${e.getMessage()}";
                logMsgs << "Error getting taxon from ${row[taxonIdHeaderIndex]} : ${e.getMessage()}";
                e.printStackTrace();
            }

            Trait trait;
            try {
                trait = Trait.findByNameAndTaxon(row[traitNameHeaderIndex], taxon);
                if(!trait){
                    log.error "Cannot find trait ${row[traitNameHeaderIndex]} and ${row[taxonIdHeaderIndex]}";
                    logMsgs << "Cannot find trait ${row[traitNameHeaderIndex]} and ${row[taxonIdHeaderIndex]}";
                }
            } catch(e) {
                log.error "Error getting trait from ${row[traitNameHeaderIndex]} and ${row[taxonIdHeaderIndex]} : ${e.getMessage()}";
                logMsgs << "Error getting trait from ${row[traitNameHeaderIndex]} and ${row[taxonIdHeaderIndex]} : ${e.getMessage()}";
                e.printStackTrace();
            }

            TraitValue traitValue = TraitValue.findByValueAndTrait(row[valueHeaderIndex], trait);

            if(!traitValue) {
                log.debug "Creating new trait value for ${row[traitNameHeaderIndex]} and taxon ${row[taxonIdHeaderIndex]}";
                logMsgs << "Creating new trait value for ${row[traitNameHeaderIndex]} and taxon ${row[taxonIdHeaderIndex]}";
                traitValue = new TraitValue();
            } else {
                log.debug "Updating trait value ${traitValue} for ${row[traitNameHeaderIndex]} and taxon ${taxon}";
                logMsgs << "Updating trait value ${traitValue} for ${row[traitNameHeaderIndex]} and taxon ${taxon}";
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
                    traitValue.taxon = taxon
                    break;

                } 
            }

            if(!traitValue.hasErrors() && !traitValue.save(flush:true)) {
                log.error "Failed to save trait vlaue";
                logMsgs <<  "Failed to save trait value";
                traitValue.errors.allErrors.each { log.error it; logMsgs << it; }
            }else{
                log.debug "Successfully inserted/updated value ${traitValue.value} : ${traitValue.taxon}";
                noOfValuesLoaded++;
            }

            row = reader.readNext();
        }
        return ['noOfValuesLoaded':noOfValuesLoaded, 'msg':logMsgs];
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

    Map loadFacts(String file, Language languageInstance) {
        int noOfFactsLoaded = 0;
        List<String> logMsgs = [];

        log.info "Loading facts from ${file}";
        logMsgs << "Loading facts from ${file}";

        //CSVReader reader = getCSVReader(new File(file))
        //String[] headers = reader.readNext();//headers

        File spreadSheet = new File(file);
        SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).each { m ->
            String attribution = m['attribution'];
            String speciesName = m['name'];
            SUser contributor = SUser.findByEmail(m['contributor'].trim());
            License license = License.findByName(License.fetchLicenseType("cc " + m['license']));

            if(!m['taxonid']) {
                log.debug "Finding species from species name ${m['name']}";
                logMsgs << "Finding species from species name ${m['name']}";
                m['taxonid'] = findSpeciesIdFromName(m['name'])?.taxonConcept?.id + '';
            }
            TaxonomyDefinition pageTaxon = m['taxonid'] ? TaxonomyDefinition.findById(Long.parseLong(m['taxonid'].trim())) : null;
            if(!pageTaxon) {
                log.error "Not a valid taxon ${m['taxonid']}";
                logMsgs << "Not a valid taxon ${m['taxonid']}";
                return;
            }
            Species species = pageTaxon.createSpeciesStub();

            log.debug "Loading facts for taxon ${pageTaxon} and page ${species}";
            logMsgs << "Loading facts for taxon ${pageTaxon} and page ${species}";
            m.each { key, value ->
                try {
                    if(!value) {
                        return;
                    }
                    switch(key) {
                        case ['name', 'taxonid', 'attribution','contributor', 'license'] : break;
                        default : 
                        log.debug "Loading trait ${key} : ${value}";
                        logMsgs <<  "Loading trait ${key} : ${value}";

                        //TODO: validate if this trait can be given to this pageTaxon as per traits taxon scope
                        Trait trait = Trait.getValidTrait(key, pageTaxon);
                        println "Got trait ${trait}";

                        if(!trait) {
                            log.error "Cannot find trait ${key} for taxon scope ${pageTaxon}";
                            logMsgs << "Cannot find trait ${key} for taxon scope ${pageTaxon}";
                            //log.warn "Ignoring fact ${key}:${value} for ${pageTaxon}";
                            //logMsgs << "Ignoring fact ${key}:${value} for ${pageTaxon}";
                        } else {
                            List<Fact> facts = SpeciesFact.findAllByTraitAndPageTaxonAndObjectId(trait, pageTaxon, species.id);
                            println "GOt existing facts ===================="
                            println facts;
                            println "Value"
                            println value;

                            List<TraitValue> traitValues = [];
                            value.split(',').each { v ->
                                println v
                                traitValues << TraitValue.findByTraitAndValueIlike(trait, v);
                            }
                            println "Got traitValues ====================="
                            println traitValues;

                            if(!facts) {
                                log.debug "Creating new fact";
                                logMsgs << "Creating new fact";
                                Fact fact = new SpeciesFact();
                                fact.trait = trait;
                                fact.pageTaxon = pageTaxon;
                                fact.objectId = species.id;
                                fact.traitValue = traitValues[0];
                                facts << fact;
                            } 

                            //fact = facts[0];

                            switch(trait.traitTypes) {
                                case Trait.TraitTypes.MULTIPLE_CATEGORICAL : 
                                boolean isExistingValue = false;
                                traitValues.each { tV ->
                                    if(tV) {
                                        isExistingValue = false;
                                        println "tv -> ${tV}"
                                        facts.each { f ->
                                            println "f -> ${f}"
                                            if(tV.value.equalsIgnoreCase(f.traitValue.value)) {
                                                isExistingValue = true;
                                            }
                                        }
                                        if(!isExistingValue) {
                                            Fact fact = new SpeciesFact();
                                            fact.trait = trait;
                                            fact.taxon = pageTaxon;
                                            fact.objectId = species.id;
                                            fact.traitValue = tV;
                                            facts << fact;
                                        }
                                    }
                                }
                                break;

                                case Trait.TraitTypes.SINGLE_CATEGORICAL : 
                                facts[0].traitValue = traitValues[0]; 
                                break;
                                case Trait.TraitTypes.BOOLEAN : 
                                facts[0].traitValue = traitValues[0]; 
                                break;
                                case Trait.TraitTypes.RANGE : 
                                facts[0].traitValue = traitValues[0]; 
                                break;
                                case Trait.TraitTypes.DATE : 
                                facts[0].traitValue = traitValues[0]; 
                                break;

                                default : log.error "Invalid trait type ${trait.traitTypes}"
                            }

                            facts.each { fact ->
                                if(fact.id) {
                                    log.debug "Updating fact ${fact}";
                                    logMsgs << "Updating fact ${fact}";
                                } else {
                                    log.debug "Creating new fact ${fact}";
                                    logMsgs << "Creating new fact ${fact}";
                                }
                                fact.attribution = attribution;
                                fact.contributor = contributor;
                                fact.license = license;
                                if(!fact.hasErrors() && !fact.save(flush:true)) { 
                                    log.error "Error saving fact ${fact.id} ${fact.trait.name} : ${fact.traitValue} ${fact.pageTaxon}"
                                    logMsgs << "Error saving fact ${fact.id} ${fact.trait.name} : ${fact.traitValue} ${fact.pageTaxon}"
                                    fact.errors.allErrors.each { log.error it; logMsgs << it; }
                                } else {
                                    noOfFactsLoaded++;
                                    log.debug "Successfully added fact";
                                    logMsgs << "Successfully added fact";
                                }
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    log.error "Error saving fact ${key} : ${value} for ${pageTaxon}"
                    logMsgs << "Error saving fact ${key} : ${value} for ${pageTaxon}"

                }

            }            println "=========================================================================="
        }
        return ['noOfFactsLoaded':noOfFactsLoaded, 'msg':logMsgs];
    }

    private Long findSpeciesIdFromName(String name) {
        XMLConverter converter = new XMLConverter();
        TaxonomyDefinition taxon = converter.getTaxonConceptFromName(name, TaxonomyRank.SPECIES.ordinal(), false, null);
        return taxon ? taxon.findSpeciesId() : null;
    }

    List listTraits(def params){
        println "params"+params
        def sql =  Sql.newInstance(dataSource);
        def query = sql.rows("select trait_taxonomy_definition_id from trait_taxonomy_definition where taxonomy_definition_id=:taxonId",[taxonId:params.taxon?.toLong()]);
        println "================="+query
        List<Trait> traitList = []
        for (row in query) {
            traitList.add(Trait.findById(row.getProperty("trait_taxonomy_definition_id")))
        }
        println "=================="+traitList.id
        //def traitList=Trait.findAllByTaxonomyDefinition(TaxonomyDefinition.get(params.taxon?.toLong()))
        //def traitList=TaxonomyDefinition.findById(params.taxon?.toLong())
        //println "+++++++++++++++++"+traitList.traitTaxonomyDefinition

        return traitList
    }

    Map showTrait(Long id) {
        Trait trait = Trait.findById(id)
        TaxonomyDefinition coverage = trait.taxonomyDefinition
        def taxons = [:];
        Field field;
        taxons = TraitFacts.findAllByTrait(trait);
        field = Field.findById(trait.fieldId);
        println "field"+field.concept
        println "taxons"+taxons.taxon.name
        println "coverage"+coverage.name
        return [trait:trait, coverage:coverage.name, species:taxons.taxon.name, field:field.concept];
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


