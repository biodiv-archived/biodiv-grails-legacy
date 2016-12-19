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
import species.participation.UploadLog;
import grails.converters.JSON;
import org.apache.log4j.Level;

class FactService extends AbstractObjectService {

    static transactional=false;
    def utilsService;

    Map upload(String file, Map params, UploadLog dl) {
        int noOfFactsLoaded = 0;

        File spreadSheet = new File(file);
        dl.writeLog("Loading facts from ${file}", Level.INFO);

        SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).each { m ->

            dl.writeLog("Loading facts ${m}");
            if(!m['taxonid']) {
                writeLog("Finding species from species name ${m['name']}");
                m['taxonid'] = findSpeciesIdFromName(m['name'])?.taxonConcept?.id;
            }

            TaxonomyDefinition pageTaxon = m['taxonid'] ? TaxonomyDefinition.findById(Long.parseLong(m['taxonid'].trim())) : null;
            if(!pageTaxon) {
                dl.writeLog("Not a valid taxon ${m['taxonid']}", Level.ERROR);
                return;
            }

            Species species = pageTaxon.createSpeciesStub();
            if(updateFacts(m, species, dl)) {
                noOfFactsLoaded++;
            }
        }
        if(noOfFactsLoaded) {
            Sql sql = Sql.newInstance(dataSource);
            println sql.executeUpdate("""
            update taxonomy_definition set traits = g.item from (
                select x.page_taxon_id, array_agg_custom(ARRAY[ARRAY[x.tid, x.tvid]]) as item from (select f.page_taxon_id, t.id as tid, tv.id as tvid, tv.value from fact f, trait t, trait_value tv where f.trait_id = t.id and f.trait_value_id = tv.id ) x group by x.page_taxon_id
            ) g where g.page_taxon_id=id;
            """);

        }
        dl.writeLog("Successfully added ${noOfFactsLoaded} facts");
        return ['success':true, 'msg':"Loaded ${noOfFactsLoaded} facts."];
    }

    private Long findSpeciesIdFromName(String name) {
        XMLConverter converter = new XMLConverter();
        TaxonomyDefinition taxon = converter.getTaxonConceptFromName(name, TaxonomyRank.SPECIES.ordinal(), false, null);
        return taxon ? taxon.findSpeciesId() : null;
    }

    Map updateFacts(Map m, object, UploadLog dl=null, boolean replaceFacts = false) {
        def writeLog;
        if(dl) writeLog = dl.writeLog;
        else writeLog = utilsService.writeLog;

        boolean success = false;
        Map result = [:]
        result['facts_updated'] = [];
        result['facts_created'] = [];
        Trait.withSession { session ->
            //def session =  sessionFactory.currentSession;
            session.setFlushMode(FlushMode.MANUAL);

            writeLog("Loading facts ${m}");

            String attribution = m['attribution'];
            //String speciesName = m['name']?.trim();
            SUser contributor = m['contributor'] ? SUser.findByEmail(m['contributor']?.trim()) : null;
            if(!contributor) {
                writeLog("Not a valid contributor email address ${m['contributor']}", Level.ERROR);
                return;
            }

            License license = m['license'] ? License.findByName(License.fetchLicenseType(m['license'].trim())) : null;
            if(!license) {
                writeLog("Not a valid license ${m['license']}", Level.ERROR);
                return;
            }

            writeLog("Loading facts for object ${object.class}:${object.id}");
            m.each { key, value ->
                try {
                    if(!value) {
                        return;
                    }
                    key = key.trim();

                    switch(key) {
                        case ['name', 'taxonid', 'attribution','contributor', 'license', 'objectId', 'objectType', 'controller', 'action', 'traitId', 'replaceFacts'] : break;
                        default : 
                        value = value ? value.trim() : null ;
                        writeLog("Loading trait ${key} : ${value}");

                        Trait trait;
                        TaxonomyDefinition pageTaxon;
                        println object.class.getCanonicalName();
                        switch(object.class.getCanonicalName()) {
                            case 'species.Species': 
                            pageTaxon = object.taxonConcept;
                            if(pageTaxon) {
                                writeLog("validate if this trait ${key} can be given to this pageTaxon ${pageTaxon} as per traits taxon scope");
                                trait = Trait.getValidTrait(key, pageTaxon);
                            }
                            break;
                            case 'species.participation.Observation': 
                            //TODO:validatetrait as per speciesgroup taxon list for observations
                            def t = Trait.read(Long.parseLong(key));
                            if(Trait.isValidTrait(t, object.group.getTaxon())) {
                                trait = t;
                            } else {
                                writeLog("${t} is not valid as per taxon", Level.ERROR);
                            }

                            if(t.isNotObservationTrait) {
                                trait = null;
                                writeLog("${t} is not observation trait", Level.ERROR);
                            }
                            break;
                        }

                        println "Got trait ${trait}";

                        if(!trait) {
                           writeLog("Cannot find trait ${key}", Level.ERROR);
                        } else {
                            Fact.withTransaction {
                            def factsCriteria = Fact.createCriteria();
                            List<Fact> facts = factsCriteria.list {
                                eq ('trait', trait)
                                eq ('objectType', object.class.getCanonicalName())
                                eq ('objectId', object.id)
                            }

                            List<TraitValue> traitValues = [];
                            if(trait.traitTypes == Trait.TraitTypes.MULTIPLE_CATEGORICAL) {
                                if(replaceFacts) {
                                    facts.each {fact ->
                                        fact.delete(flush:true);
                                    }
                                    facts.clear();
                                }
                                value.split(',').each { v ->
                                    writeLog("Loading trait ${trait} with value ${v.trim()}");
                                    def x = TraitValue.findByTraitAndValueIlike(trait, v.trim());
                                    if(x) traitValues << x;
                                }
                            } else {
                                def x = TraitValue.findByTraitAndValueIlike(trait, value);
                                if(x) traitValues << x;
                            }
                            println "Got traitValues ====================="
                            println traitValues;

                            if(!traitValues) {
                               writeLog("Cannot find [trait:value] [${trait} : ${value}]", Level.ERROR);
                                return;
                            }

                            if(!facts) {
                                //writeLog("Creating new fact");
                                Fact fact = new Fact();
                                fact.trait = trait;
                                fact.pageTaxon = pageTaxon;
                                fact.objectId = object.id;
                                fact.objectType = object.class.getCanonicalName();
                                fact.traitValue = traitValues[0];
                                facts << fact;
                            } 
                            println facts;
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
                                            Fact fact = new Fact();
                                            fact.trait = trait;
                                            fact.pageTaxon = pageTaxon;
                                            fact.objectId = object.id;
                                            fact.objectType = object.class.getCanonicalName();
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

                                default : 
                               writeLog("Invalid trait type ${trait.traitTypes}", Level.ERROR);
                            }

                            boolean isUpdate = false;
                            facts.each { fact ->
                                if(fact.id) {
                                    isUpdate = true;
                                    writeLog("Updating fact ${fact}");
                                } else {
                                    isUpdate = false;
                                    writeLog("Creating new fact ${fact}");
                                }
                                fact.attribution = attribution;
                                fact.contributor = contributor;
                                fact.license = license;
                                fact.isDeleted = false;
                                if(!fact.hasErrors() && !fact.save()) { 
                                    writeLog("Error saving fact ${fact.id} ${fact.trait.name} : ${fact.traitValue} ${fact.pageTaxon}", Level.ERROR);
                                    fact.errors.allErrors.each { writeLog(it.toString(), Level.ERROR) }
                                    success = false;
                                } else {
                                    success = true;
                                    if(isUpdate) 
                                        result['facts_updated'] << fact;
                                    else
                                        result['facts_created'] << fact;

                                    writeLog("Successfully added fact");
                                }
                            }
                        }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    writeLog("Error saving fact ${key} : ${value} for ${object}", Level.ERROR);
                }
            } 
            println "=========================================================================="
            session.flush();
            session.clear();
            // we need to disconnect and get a new DB connection here
            def connection = session.disconnect();
            if(connection) {
                println "---------------------------------";
                connection.close();
            }

            session.reconnect(dataSource.connection);
        }
        result['success'] = success;
        return result;
    }
}
