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

    Map upload(String file, Map params, UploadLog dl) {
        int noOfFactsLoaded = 0;

        File spreadSheet = new File(file);
        dl.writeLog("Loading facts from ${file}", Level.INFO);

        SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).each { m ->
            Trait.withSession { session ->
                //def session =  sessionFactory.currentSession;
                session.setFlushMode(FlushMode.MANUAL);

                dl.writeLog("Loading facts ${m}");

                String attribution = m['attribution'];
                String speciesName = m['name']?.trim();
                SUser contributor = m['contributor'] ? SUser.findByEmail(m['contributor']?.trim()) : null;
                if(!contributor) {
                    dl.writeLog("Not a valid contributor email address ${m['contributor']}", Level.ERROR);
                    return;
                }

                License license = m['license'] ? License.findByName(License.fetchLicenseType("cc " + m['license'].trim())) : null;
                if(!license) {
                    dl.writeLog("Not a valid license ${m['license']}", Level.ERROR);
                    return;
                }

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

                dl.writeLog("Loading facts for taxon ${pageTaxon} and page ${species}");
                m.each { key, value ->
                    try {
                        if(!value) {
                            return;
                        }
                        key = key.trim();
                        value = value ? value.trim() : null ;

                        switch(key) {
                            case ['name', 'taxonid', 'attribution','contributor', 'license'] : break;
                            default : 
                            dl.writeLog("Loading trait ${key} : ${value}");

                            //TODO: validate if this trait can be given to this pageTaxon as per traits taxon scope
                            Trait trait = Trait.getValidTrait(key, pageTaxon);
                            println "Got trait ${trait}";

                            if(!trait) {
                                dl.writeLog("Cannot find trait ${key} for taxon scope ${pageTaxon}", Level.ERROR);
                                //log.warn "Ignoring fact ${key}:${value} for ${pageTaxon}";
                                //logMsgs << "Ignoring fact ${key}:${value} for ${pageTaxon}\n";
                            } else {
                                List<Fact> facts = SpeciesFact.findAllByTraitAndPageTaxonAndObjectId(trait, pageTaxon, species.id);
                                println "GOt existing facts ===================="
                                println facts;
                                println "Value"
                                println value;

                                List<TraitValue> traitValues = [];
                                if(trait.traitTypes == Trait.TraitTypes.MULTIPLE_CATEGORICAL) {
                                    value.split(',').each { v ->
                                        println v;
                                        dl.writeLog("Loading trait ${trait} with value ${v.trim()}");
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
                                    dl.writeLog("Cannot find [trait:value] [${trait} : ${value}]", Level.ERROR);
                                    return;
                                }

                                if(!facts) {
//                                    dl.writeLog("Creating new fact");
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
                                                fact.pageTaxon = pageTaxon;
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

                                    default : 
                                        dl.writeLog("Invalid trait type ${trait.traitTypes}", Level.ERROR);
                                }

                                facts.each { fact ->
                                    if(fact.id) {
                                        dl.writeLog("Updating fact ${fact}");
                                    } else {
                                        dl.writeLog("Creating new fact ${fact}");
                                    }
                                    fact.attribution = attribution;
                                    fact.contributor = contributor;
                                    fact.license = license;
                                    if(!fact.hasErrors() && !fact.save()) { 
                                        dl.writeLog("Error saving fact ${fact.id} ${fact.trait.name} : ${fact.traitValue} ${fact.pageTaxon}", Level.ERROR);
                                        fact.errors.allErrors.each { dl.writeLog(it.toString(), Level.ERROR) }
                                    } else {
                                        noOfFactsLoaded++;
                                        dl.writeLog("Successfully added fact");
                                    }
                                }
                            }
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                        dl.writeLog("Error saving fact ${key} : ${value} for ${pageTaxon}", Level.ERROR);
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
}
