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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

class FactService extends AbstractObjectService {

    static transactional=false;
    def utilsService;

    Map upload(String file, Map params, UploadLog dl) {
        int noOfFactsLoaded = 0;

        File spreadSheet = new File(file);
        if(!spreadSheet.exists()) {
            return ['success':false, 'msg':"Cant find the file at ${file}."];
        }
        dl.writeLog("Loading facts from ${file}", Level.INFO);

        SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).eachWithIndex { m,index ->

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

                                List traitValues = [];
                                if(trait.traitTypes == Trait.TraitTypes.MULTIPLE_CATEGORICAL) {
                                    if(replaceFacts) {
                                        facts.each {fact ->
                                            fact.delete(flush:true);
                                        }
                                        facts.clear();
                                    }
                                    value.split(',').each { v ->
                                        writeLog("Loading trait ${trait} with value ${v.trim()}");
                                        def x = getTraitValue(trait, v.trim());
                                        if(x) traitValues << x;
                                    }
                                } else {
                                    def x = getTraitValue(trait, value);
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
                                    setTraitValue(fact, traitValues[0]);
                                    facts << fact;
                                } 
                                println facts;
                                //fact = facts[0];

                                if(trait.traitTypes == Trait.TraitTypes.MULTIPLE_CATEGORICAL) {
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
                                                setTraitValue(fact, tV);
                                                facts << fact;
                                            }
                                        }
                                    }
                                } else {
                                    setTraitValue(facts[0], traitValues[0]); 
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

    private def getTraitValue(Trait trait, String value) {
        if(!value) return;
        if(trait.traitTypes == TraitTypes.SINGLE_CATEGORICAL || trait.traitTypes == TraitTypes.MULTIPLE_CATEGORICAL) { 
            println "===="
            println value;
            return TraitValue.findByTraitAndValueIlike(trait, value.trim());
        }
        else {
            return value.trim();
        }
    }

    private void setTraitValue(Fact fact, value) {
        switch(fact.trait.traitTypes) {
            case Trait.TraitTypes.RANGE : 
            setTraitRange(fact, value);
            break;
            default:
            fact.traitValue = value;
        }
    }

    private void setTraitRange(Fact fact, value) {
        String[] v = value.split(':');
        if(v.length > 1) {
            switch(fact.trait.dataTypes) {
                case Trait.DataTypes.DATE:
                fact.fromDate = getFromDate(v[0], fact.trait.units);
                fact.toDate = getToDate(v[1], fact.trait.units);
                break;
                default: 
                fact.value = v[0];
                fact.toValue = v[1];
            }
        } else if(v[0]) {
            switch(fact.trait.dataTypes) {
                case Trait.DataTypes.DATE:
                fact.fromDate = getFromDate(v[0]);
                fact.toDate = getToDate(v[0]);
                break;
                default: 
                fact.value = v[0];
                fact.toValue = v[0];
            }
        }
    }

    private Date getFromDate(String d, Units units) {
        switch(units) {
            case Units.MONTH:
            return new SimpleDateFormat("dd-MMMM-yyyy").parse("1-"+d+"-"+Calendar.getInstance().get(Calendar.YEAR));

//            GregorianCalendar gc = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), getMonth(d), 1);
//            return new java.util.Date(gc.getTime().getTime());
            break;
            default:
            return utilsService.parseDate(d,false);
        }
    }

    private Date getToDate(String d, Units units) {
       switch(units) {
            case Units.MONTH:
            return new SimpleDateFormat("dd-MMMM-yyyy").parse(getMonthEndDate(d)+"-"+d+"-"+Calendar.getInstance().get(Calendar.YEAR));
            break;
            default:
            return utilsService.parseDate(d,false);
        }
    }

    private int getMonthEndDate(String month) {
        Date date = new SimpleDateFormat("MMMM").parse(month)
        Calendar cal = Calendar.getInstance();
        cal.setTime(date)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
