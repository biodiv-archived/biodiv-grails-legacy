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
import species.dataset.DataTable;
import speciespage.ObvUtilService;
import species.participation.Observation;

class FactService extends AbstractObjectService {

    static transactional=false;
    def observationsSearchService;

    Map upload(String file, Map params, UploadLog dl) {
        int noOfFactsLoaded = 0;

        File spreadSheet = new File(file?:params.fFile);
        if(!spreadSheet.exists()) {
            return ['success':false, 'msg':"Cant find the file at ${file}."];
        }
        dl.writeLog("Loading facts from ${file?:params.fFile}", Level.INFO);

        SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).eachWithIndex { m,index ->

            dl.writeLog("============================================\n", Level.INFO);
            dl.writeLog("Loading facts ${m} from row no ${index+2}", Level.INFO);
            if(!m['taxonid']) {
                dl.writeLog("Finding species from species name ${m['name']}");
                m['taxonid'] = findSpeciesIdFromName(m['name'])?.taxonConcept?.id;
            }

            TaxonomyDefinition pageTaxon = m['taxonid'] ? TaxonomyDefinition.findById(Long.parseLong(m['taxonid'].trim())) : null;
            if(!pageTaxon) {
                dl.writeLog("Not a valid taxon ${m['taxonid']}", Level.ERROR);
                return;
            }

            Species species = pageTaxon.createSpeciesStub();

            if(params.dataTable) {
                println "Setting dataTable"
                DataTable.inheritParams(m, params);
            }

            Trait.withSession { session ->
                //def session =  sessionFactory.currentSession;
                session.setFlushMode(FlushMode.MANUAL);

                def r = updateFacts(m, species, dl);
                if(r && r.success) {
                    noOfFactsLoaded += r.noOfFactsLoaded;
                }
                session.flush();
                session.clear();
                // we need to disconnect and get a new DB connection here
                def connection = session.disconnect();
                if(connection) {
                    connection.close();
                }

            session.reconnect(dataSource.connection);
        }
        }
        if(noOfFactsLoaded) {
            Sql sql = Sql.newInstance(dataSource);
            println sql.executeUpdate("""
            update taxonomy_definition set traits = g.item from (
                select x.page_taxon_id, array_agg_custom(ARRAY[ARRAY[x.tid, x.tvid]]) as item from (select f.page_taxon_id, t.id as tid, tv.id as tvid, tv.value from fact f, trait t, trait_value tv where f.trait_id = t.id and f.trait_value_id = tv.id ) x group by x.page_taxon_id
            ) g where g.page_taxon_id=id;
            """);
            println sql.executeUpdate("""
            update taxonomy_definition set traits_json = g.item from (
                select x1.page_taxon_id, format('{%s}', string_agg(x1.item,','))::json as item from (
                    (select x.page_taxon_id,  string_agg(format('"%s":{"value":%s,"to_value":%s}', to_json(x.tid), to_json(x.value), to_json(x.to_value)), ',') as item from (select f.page_taxon_id, t.id as tid, f.value::numeric as value, f.to_value::numeric as to_value from fact f, trait t where f.trait_id = t.id and (t.data_types='NUMERIC') ) x group by x.page_taxon_id)
                    union
                    (select x.page_taxon_id,  string_agg(format('"%s":{"from_date":%s,"to_date":%s}', to_json(x.tid), to_json(x.from_date), to_json(x.to_date)), ',') as item from (select f.page_taxon_id, t.id as tid, f.from_date as from_date, f.to_date as to_date from fact f, trait t where f.trait_id = t.id and (t.data_types='DATE') ) x group by x.page_taxon_id)
                    union
                    (select x.page_taxon_id,  string_agg(format('"%s":{"r":%s,"g":%s,"b":%s}', to_json(x.tid), to_json(x.value[1]::integer), to_json(x.value[2]::integer), to_json(x.value[3]::integer)), ',') as item from (select f.page_taxon_id, t.id as tid, string_to_array(substring(f.value from 5 for length(f.value)-5),',') as value from fact f, trait t where f.trait_id = t.id and (t.data_types='COLOR')) x group by x.page_taxon_id)
                ) x1 group by x1.page_taxon_id
            ) g where g.page_taxon_id=id;
                                            """);


        }
        dl.writeLog("\n====================================\nLoaded ${noOfFactsLoaded} facts\n====================================\n", Level.INFO);
        return ['success':true, 'msg':"Loaded ${noOfFactsLoaded} facts."];
    }

    private Long findSpeciesIdFromName(String name) {
        XMLConverter converter = new XMLConverter();
        TaxonomyDefinition taxon = converter.getTaxonConceptFromName(name, TaxonomyRank.SPECIES.ordinal(), false, null);
        return taxon ? taxon.findSpeciesId() : null;
    }

    Map validateFactsFile(String file, UploadLog dl) {
        println "validateFactsFile"
        File spreadSheet = new File(file);
        if(!spreadSheet.exists()) {
            return ['success':false, 'msg':"Cant find the file at ${file}."];
        }
        dl.writeLog("Loading facts from ${file}", Level.INFO);

        List reqdHeaders = ['taxonid', 'attribution', 'contributor', 'license'];
        return validateSpreadsheetHeaders(file, dl, reqdHeaders);
    }

    Map updateFacts(Map m, object, UploadLog dl=null, boolean replaceFacts = false) {
        def writeLog;
        if(dl) writeLog = dl.writeLog;
        else writeLog = utilsService.writeLog;

        boolean success = false;
        Map result = [:]
        int noOfFactsLoaded = 0;

        result['facts_updated'] = [];
        result['facts_created'] = [];
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

        writeLog("Loading facts for object ${object.class}:${object.id}\n");
        m.each { key, value ->
            try {
                if(!value) {
                    writeLog("Value is null for trait ${key}\n", Level.ERROR);
                    log.debug "Value is null for trait ${key}."
                    //return;
                }
                key = key.trim();

                switch(key) {
                    case ['name', 'taxonid', 'attribution','contributor', 'license', 'objectId', 'objectType', 'controller', 'action', 'traitId', 'replaceFacts', 'dataTable', ObvUtilService.LOCATION, ObvUtilService.TOPOLOGY, ObvUtilService.LATITUDE, ObvUtilService.LONGITUDE, ObvUtilService.LOCATION_SCALE, ObvUtilService.OBSERVED_ON, ObvUtilService.TO_DATE, ObvUtilService.DATE_ACCURACY, ObvUtilService.AUTHOR_EMAIL, 'traitInstance'] : break;
                    default :
                    value = (value && value instanceof String)? value.trim() : null ;

                    writeLog("Loading trait ${key} : ${value}", Level.INFO);
                    Trait traitInstance;
                    TaxonomyDefinition pageTaxon;
                    switch(object.class.getCanonicalName()) {
                        case 'species.Species':
                        pageTaxon = object.taxonConcept;
                        if(pageTaxon) {
                            println m.traitInstance
                            writeLog("validate if this trait ${key} can be given to this pageTaxon ${pageTaxon} as per traits taxon scope");
                            if(m.traitInstance) {
                                Trait t  = m.traitInstance;//Trait.read(Long.parseLong(m.traitId));
                                println t
                                if(Trait.isValidTrait(t, pageTaxon)) {
                                    traitInstance = t;
                                } else {
                                    writeLog("Trait ${t} is not valid as per taxon", Level.ERROR);
                                }
                            } else {
                                traitInstance = Trait.getValidTrait(key, pageTaxon);
                            }
                        }
                        break;
                        case 'species.participation.Observation':
                        //TODO:validatetrait as per speciesgroup taxon list for observations
                        def t = Trait.read(Long.parseLong(key));
                        if(Trait.isValidTrait(t, object.group.getTaxon())) {
                            traitInstance = t;
                        } else {
                            writeLog("Trait ${t} is not valid as per taxon", Level.ERROR);
                        }

                        if(t.isNotObservationTrait) {
                            traitInstance = null;
                            writeLog("Trait ${t} is not observation trait", Level.ERROR);
                        }
                        break;
                    }

                    println "Got trait ${traitInstance}";

                    if(!traitInstance) {
                        writeLog("Cannot find trait ${key}\n", Level.ERROR);
                    } else {
                        Fact.withTransaction {
                            def factsCriteria = Fact.createCriteria();
                            List<Fact> facts = factsCriteria.list {
                                eq ('traitInstance', traitInstance)
                                eq ('objectType', object.class.getCanonicalName())
                                eq ('objectId', object.id)
                            }

                            List traitValues = [];
                            if(traitInstance.traitTypes == Trait.TraitTypes.MULTIPLE_CATEGORICAL) {
                                if(replaceFacts) {
                                    writeLog("Replacing old facts with new ones");
                                    facts.each {fact ->
                                        fact.delete(flush:true);
                                    }
                                    facts.clear();
                                }
                                value?.split(',').each { v ->
                                    writeLog("Loading trait ${traitInstance} with value ${v.trim()}");
                                    def x = getTraitValue(traitInstance, v.trim());
                                    if(x) traitValues << x;
                                }
                            } else {
                                def x = getTraitValue(traitInstance, value);
                                if(x) traitValues << x;
                            }
                            println "Got traitValues ====================="
                            println traitValues;

                            if(!traitValues) {
                                writeLog("Cannot find [trait:value] [${traitInstance} : ${value}]", Level.ERROR);
                                return;
                            }

                            if(traitValues) {
                            if(!facts) {
                                //writeLog("Creating new fact");
                                Fact fact = new Fact();
                                fact.traitInstance = traitInstance;
                                fact.pageTaxon = pageTaxon;
                                fact.objectId = object.id;
                                fact.objectType = object.class.getCanonicalName();
                                setTraitValue(fact, traitValues[0]);
                                facts << fact;
                            }
                            println facts;
                            //fact = facts[0];

                            if(traitInstance.traitTypes == Trait.TraitTypes.MULTIPLE_CATEGORICAL) {
                                boolean isExistingValue = false;
                                traitValues.each { tV ->
                                    if(tV) {
                                        isExistingValue = false;
                                        println "tv -> ${tV}"
                                        if(tV instanceof TraitValue) {
                                            facts.each { f ->
                                                println "f -> ${f}"
                                                if((f.traitValue && tV.value.equalsIgnoreCase(f.traitValue.value))) {
                                                    isExistingValue = true;
                                                }
                                            }
                                        }else if(tV instanceof String) {
                                            println 'String';
                                            facts.each { f ->
                                                println "f -> ${f}"
                                                println f.value
                                                println tV
                                                if((f.value && utilsService.hex2Rgb(tV).equalsIgnoreCase(f.value))) {
                                                    isExistingValue = true;
                                                }
                                            }
                                        }


                                        if(!isExistingValue) {
                                            Fact fact = new Fact();
                                            fact.traitInstance = traitInstance;
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
                                    writeLog("Updating fact ${fact}", Level.INFO);
                                } else {
                                    isUpdate = false;
                                    writeLog("Creating new fact ${fact}", Level.INFO);
                                }
                                fact.attribution = attribution;
                                fact.contributor = contributor;
                                fact.license = license;
                                fact.isDeleted = false;
                                if(m['dataTable']) {
                                    fact.dataTable = DataTable.read(Long.parseLong(''+m['dataTable']));
                                }
                                if(!fact.hasErrors() && !fact.save()) {
                                    writeLog("Error saving fact ${fact.id} ${fact.traitInstance.name} : ${fact.traitValue} ${fact.pageTaxon}", Level.ERROR);
                                    fact.errors.allErrors.each { writeLog(it.toString(), Level.ERROR) }
                                    writeLog('\n');
                                    success = false;
                                } else {
                                    success = true;
                                    noOfFactsLoaded++;
                                    if(isUpdate)
                                        result['facts_updated'] << fact;
                                    else
                                        result['facts_created'] << fact;

                                    writeLog("Successfully added fact\n", Level.INFO);
                                }
                            }
                            }
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
                writeLog("Error saving fact ${key} : ${value} for ${object}\n", Level.ERROR);
            }
        }
        println "=========================================================================="

        if(object instanceof Observation) {
            Sql sql = Sql.newInstance(dataSource);
            println sql.executeUpdate("""
            update observation set traits = g.item from (
                             select x.object_id, array_agg_custom(ARRAY[ARRAY[x.tid, x.tvid]]) as item from (select f.object_id, f.object_type, t.id as tid, tv.id as tvid, tv.value from fact f, trait t, trait_value tv where f.trait_instance_id = t.id and f.trait_value_id = tv.id and f.object_type='species.participation.Observation' and f.object_id="""+object.id+""") x group by x.object_id
                             ) g where g.object_id=id
            """);
            println sql.executeUpdate("""
update observation set traits_json = g.item from (
     select x1.object_id, format('{%s}', string_agg(x1.item,','))::json as item from (
        (select x.object_id,  string_agg(format('"%s":{"value":%s,"to_value":%s}', to_json(x.tid), to_json(x.value), to_json(x.to_value)), ',') as item from (select f.object_id, t.id as tid, f.value::numeric as value, f.to_value::numeric as to_value from fact f, trait t where f.trait_instance_id = t.id and (t.data_types='NUMERIC') and f.object_type='species.participation.Observation' and f.object_id="""+object.id+""") x group by x.object_id)
        union
        (select x.object_id,  string_agg(format('"%s":{"from_date":%s,"to_date":%s}', to_json(x.tid), to_json(x.from_date), to_json(x.to_date)), ',') as item from (select f.object_id, t.id as tid, f.from_date as from_date, f.to_date as to_date from fact f, trait t where f.trait_instance_id = t.id and (t.data_types='DATE')  and f.object_type='species.participation.Observation' and f.object_id="""+object.id+""") x group by x.object_id)
        union
        (select x.object_id,  string_agg(format('"%s":{"r":%s,"g":%s,"b":%s}', to_json(x.tid), to_json(x.value[1]::integer), to_json(x.value[2]::integer), to_json(x.value[3]::integer)), ',') as item from (select f.object_id, t.id as tid, string_to_array(substring(f.value from 5 for length(f.value)-5),',') as value from fact f, trait t where f.trait_instance_id = t.id and (t.data_types='COLOR')  and f.object_type='species.participation.Observation' and f.object_id="""+object.id+""") x group by x.object_id)
    ) x1 group by x1.object_id
) g where g.object_id=id;


                        """);

            observationsSearchService.publishSearchIndex(object,true);

        }

        result['success'] = success;
        result['noOfFactsLoaded'] = noOfFactsLoaded;
        return result;
    }

    private def getTraitValue(Trait traitInstance, String value) {
        if(!value) return;
        if(!(traitInstance.traitTypes == TraitTypes.RANGE || traitInstance.dataTypes == DataTypes.COLOR)) {
            return TraitValue.findByTraitInstanceAndValueIlike(traitInstance, value.trim());
        } else {
            return value.trim();
        }
    }

    private void setTraitValue(Fact fact, value) {
        switch(fact.traitInstance.traitTypes) {
            case Trait.TraitTypes.RANGE :
            setTraitRange(fact, value);
            break;
            default:
            if(fact.traitInstance.dataTypes == DataTypes.COLOR) {
                fact.value = utilsService.hex2Rgb(value);
            } else {
                fact.traitValue = value;
            }
        }
    }

    private void setTraitRange(Fact fact, value) {
        String[] v = value.split(':');
        if(v.length > 1) {
            switch(fact.traitInstance.dataTypes) {
                case Trait.DataTypes.DATE:
                fact.fromDate = getFromDate(v[0], fact.traitInstance.units);
                fact.toDate = getToDate(v[1], fact.traitInstance.units);
                if(fact.fromDate>fact.toDate) {
                    Date x = fact.fromDate;
                    fact.fromDate = fact.toDate;
                    fact.toDate = x;
                }
                break;
                default:
                fact.value = v[0];
                fact.toValue = v[1];
            }
        } else if(v[0]) {
            switch(fact.traitInstance.dataTypes) {
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
