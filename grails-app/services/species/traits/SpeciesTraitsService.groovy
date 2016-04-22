package species.traits

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import org.apache.jena.atlas.lib.StrUtils;
import species.participation.DownloadLog;
import content.eml.UFile;
import species.sourcehandler.importer.AbstractImporter;
import species.sourcehandler.importer.CSVTraitsImporter;
import org.apache.commons.io.FilenameUtils;
import java.util.Date;

import species.sourcehandler.XMLConverter;
import species.TaxonomyDefinition;
import species.ScientificName.TaxonomyRank

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import species.traits.Trait;
import species.Species;

class SpeciesTraitsService {

    static transactional=false;

    static final BIODIV_NAMESPACE = "http://indiabiodiversity.org/schema/terms/";
    public static final int IMPORT_BATCH_SIZE = 10;

    protected Dataset dataset;
    private String resourceURI = "http://localhost.indiabiodiversity.org/species/show/" 

    private static Map propertyValueIcons;
    def springSecurityService;
    def utilsService;

    void init() {
        // Make a TDB-backed dataset
        String directory = "/home/sravanthi/git/biodiv/app-conf/traits" ;
        dataset = getDataset(directory);
        TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;

        //TODO: populating icons from file
        //TOBE removed
        CSVReader reader = getCSVReader(new File('/home/sravanthi/git/biodiv/icons.csv'))
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

    void close() {
        dataset.close();
    }

    /**
     * get a model for the given directory
     * 
     * @param directory
     * @return
     */
    protected Dataset getDataset(String directory) {
        // Make a TDB-backed dataset
        dataset = TDBFactory.createDataset(directory);
        return dataset;
    }

    protected Model getModel(String nameURI=null) {
        // open write transaction 
        // see http://jena.apache.org/documentation/tdb/tdb_transactions.html
        Model model;
        if(nameURI) {
            model = dataset.getNamedModel(nameURI);
        } else {
            model = dataset.getDefaultModel();
        }
        return model
    }

    /**
     * save the given model
     * @param model
     */
    protected void saveModel(Model model, boolean closeModel = true) {
        if (model != null && dataset != null) {
            model.commit();
            //dataset.end();
            if(closeModel)
                model.close();
            //dataset.close();
        }
    }

    protected Map uploadFactsFile(Map params) {
        def resultModel = [:]
        String file = params.path?:params.uFile?.path;
        def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
        file = config.speciesPortal.content.rootDir + file;

        File f = new File(file);
        File destDir = f.getParentFile();
        /*new File(f.getParentFile(),  f.getName())
        if(!destDir.exists()) {
        destDir.mkdir()
        }*/
        boolean isDwC = false;
        File directory = f.getParentFile();
        File metadataFile;
        if(FilenameUtils.getExtension(f.getName()).equals('zip')) {
            def ant = new AntBuilder().unzip( src: file,
            dest: destDir, overwrite:true)
            directory = new File(destDir, FilenameUtils.removeExtension(zipF.getName()));
            if(!directory.exists()) {
                directory = destDir;
            }
            isDwC = true;//validateDwCA(file);
            if(!isDwC) {
                return [success:false, msg:'Invalid DwC-A file']
            } else {
                metadataFile = new File(directory, "metadata.xml");
            }
        }


        File uploadLog = new File(destDir, 'upload.log');
        if(uploadLog.exists()) uploadLog.delete();

        Date startTime = new Date();
        if(directory) {
            params['author'] = springSecurityService.currentUser; 
            //params['type'] = DatasetType.OBSERVATIONS;
            //params['datasource'] = Datasource.read(params.long('datasource'));

            if(metadataFile) {
                uploadLog << "\nUploading traits in DwCA format present at : ${zipF.getAbsolutePath()}";
                uploadLog << "\nDataset upload start time : ${startTime}"
                /*String datasetMetadataStr = metadataFile.text;

                def datasetMetadata = new XmlParser().parseText(datasetMetadataStr);
                params['title'] = params.title?:datasetMetadata.dataset.title.text()
                params['description'] = params.description?:datasetMetadata.dataset.abstract.para.text();
                params['externalId'] = datasetMetadata.attributes().packageId;
                params['externalUrl'] = 'http://doi.org/'+params['externalId'];
                params['rights'] = datasetMetadata.dataset.intellectualRights.para.text();
                params['language'] = datasetMetadata.dataset.language.text();
                params['publicationDate'] = utilsService.parseDate(datasetMetadata.dataset.pubDate.text());
                 */
            } else {
                //params['externalUrl'] = params.externalUrl ?: params['datasource']?.website;
            }

            UFile f1 = new UFile()
            f1.size = f.length()
            f1.path = params.uFile?.path;//zipF.getAbsolutePath().replaceFirst(contentRootDir, "")
            if(f1.save()) {
                params['uFile'] = f1
                //params['uFile'] = params.uFile; 
                //        params['originalAuthor'] = createContact() 

                if(isDwC) {
                    //importDWCObservations(dataset, directory, uploadLog);
                } else {
                    resultModel = [success:true, file:f, type:DownloadLog.DownloadType.CSV, uploadLog:uploadLog];//new File(directory, 'occurence.txt');
                } 
            }
        } else {
            resultModel = [success:false, msg:'Invalid file']
        }
        uploadLog <<  "\nUpload facts file result ${resultModel}";
        return resultModel
    }

    Map saveFacts(params, File uploadLog = null) {
        if(!dataset) {
            init();
        }
        Map uploadResult = uploadFactsFile(params);
        uploadLog = uploadResult.uploadLog;
        Map saveResult = uploadResult;
        AbstractImporter importer;

        if(uploadResult.success) {
            switch(uploadResult.type) {
                case DownloadLog.DownloadType.CSV : 
                importer = CSVTraitsImporter.getInstance();
                break;
            }

            if(importer) {
                importer.importData(uploadResult.file, uploadLog);
                List factsParamsList = importer.next(IMPORT_BATCH_SIZE, uploadLog);
                int noOfUploaded = 0, noOfFailed = 0;
                boolean flushSingle = false;
                Date startTime = new Date();
                int i=0;
                while(factsParamsList) {
                    List result = [];
                    int tmpNoOfUploaded = 0, tmpNoOfFailed= 0;
                    try {
                        factsParamsList.each { factParams ->
                            if(flushSingle) {
                                log.info "Retrying batch with flushSingle"
                                if(uploadLog) uploadLog << "\n Retrying batch with flushSingle"
                            }
                            if(uploadLog) {
                                uploadLog << "\n\n----------------------------------------------------------------------";
                                uploadLog << "\nUploading fact with params ${factParams}"
                            }
                            try {
                                Long id = factParams['SpeciesId']?.toLong()
                                if(!id && factParams['Name']) {
                                    id = findSpeciesIdFromName(factParams['Name']);
                                }

                                if(id) {
                                    String subjectURI = resourceURI + id;
                                    log.debug "LOADING FACTS FOR ${subjectURI}"
                                    dataset.begin(ReadWrite.WRITE);
                                    try {
                                        Model model = getModel(subjectURI);
                                        model.removeAll();
                                        List<Statement> stmts = [];
                                        Map metadata = ['uploader':springSecurityService.currentUser.id, 'date':(new Date()).toString()];
                                        Map filteredFactParams = [:];
                                        factParams.each { p ->
                                            println p
                                            switch(p.key.toLowerCase()) {
                                                case 'contributor' : metadata[p.key] = p.value; break;
                                                case 'attribution' : metadata[p.key] = p.value; break;
                                                case 'license' : metadata[p.key] = p.value; break;
                                                default : filteredFactParams[p.key] = p.value;
                                            }
                                        }

                                        filteredFactParams.each { p->
                                            def s = createStatement(model, id, p.key, p.value, metadata);
                                            if(s) stmts << s; 
                                        }

                                        stmts << createStatement(model, id, RDF.type, 'Species');

                                        log.debug "Adding ${stmts} to model";
                                        if(model.add(stmts)) {
                                            tmpNoOfUploaded++;
                                        } else {
                                            tmpNoOfFailed++;
                                        }

                                        log.debug "Saving model for species ${id}";
                                        //saved for each species
                                        dataset.commit();
                                    } finally {
                                        dataset.end();
                                    }
                                } else {
                                    tmpNoOfFailed++;
                                    log.error "No species id : ${factParams}";
                                }
                            } catch(Exception e) {
                                tmpNoOfFailed++;
                                if(flushSingle) { 
                                    utilsService.cleanUpGorm(true)
                                    uploadLog << "\n"+e.getMessage()
                                }
                                else
                                    throw e;
                            }
                        }

                        noOfUploaded += tmpNoOfUploaded;
                        noOfFailed += tmpNoOfFailed;
                        log.debug "Saved facts : noOfUploaded : ${noOfUploaded} noOfFailed : ${noOfFailed}";
                        factsParamsList = importer.next(IMPORT_BATCH_SIZE, uploadLog)
                        flushSingle = false;
                    } catch (Exception e) {
                        log.error "error in creating fact."
                        if(uploadLog) uploadLog << "\nerror in creating fact ${e.getMessage()}." 
                            e.printStackTrace();
                        flushSingle = true;
                    }
                    utilsService.cleanUpGorm(true)
                    result.clear();
                }
                log.debug "Total number of facts saved are : ${noOfUploaded}";

                if(uploadLog) {
                    uploadLog << "\n\n----------------------------------------------------------------------";
                    uploadLog << "\nTotal number of facts saved are : ${noOfUploaded}";
                    uploadLog << "\nTotal number of facts failed in loading are : ${noOfFailed}";
                    uploadLog << "\nTotal time taken for upload ${((new Date()).getTime() - startTime.getTime())/1000} sec"
                }
                importer.closeReader();
                saveResult = [success:'true', msg:"Total number of facts saved are : ${noOfUploaded}"];
            } else {
                saveResult = [success:'false', msg:"No importer defined for this file type ${uploadResult.type}"];
            }
        } else {
            return uploadResult;
        }
        return saveResult;
    }

    void saveFact(Long id, String traitName, String traitValue) {
        String subjectURI = resourceURI + id;
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = getModel(subjectURI);
            addFactToModel(model, id, traitName, traitValue);
            log.debug "SAVING FACTS MODEL FOR SPECIES ${id}"
            model.write(System.out);
            saveModel(model);
        } finally {
            dataset.end();
        }
    }

    private boolean addFact(Long id, String predicate, String value) {
        String subjectURI = resourceURI + id;
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = getModel(subjectURI);
            addFactToModel(model, id, property, value);
            saveModel(model);
        } finally {
            dataset.end();
        }
    }

    private boolean addFactToModel(Model model, Long id, String predicate, String value) {
        Statement stmt = createStatement(model, id, predicate, value);
        if(stmt)
            model.add(stmt);
    }

    private Statement createStatement(Model model, Long id, String predicate, String value, Map metadata=null) {
        if(!id || !predicate || !value) return null;
        predicate = predicate.replaceAll('\\s+',"_");
        Property property = model.getProperty(BIODIV_NAMESPACE, predicate) ?: model.createProperty(BIODIV_NAMESPACE, predicate);
        return createStatement(model, id, property, value, metadata); 
    }

    private Statement createStatement(Model model, Long id, Property property, String value, Map metadata=null) {
        if(!id || !property || !value) return null;
        log.debug "Creating statement ${id} -> ${property} -> ${value}";
        Resource subject = model.getResource(resourceURI + id) ?: model.createResource(resourceURI + id);
        def val;
        if (isValidTrait(property.getLocalName(), value, id)) {
            if(metadata) {
                Resource valueResource = model.createResource();
                valueResource.addProperty(DC.title, value.trim());
                valueResource.addProperty(RDF.type, "Trait");
                def definedKey;
                metadata.each {
                    switch(it.key.toLowerCase()) {
                        case 'contributor' : definedKey = DC.contributor; break;
                        case 'attribution' : definedKey = DC.publisher; break;
                        case ['rights','license'] : definedKey = DC.rights; break;
                        case 'date' : definedKey = DC.date; break;
                        default : definedKey = model.getProperty(BIODIV_NAMESPACE, it.key) ?: model.createProperty(BIODIV_NAMESPACE, it.key);
                    }                
                    valueResource.addProperty(definedKey, it.value);
                }
                val = valueResource;
            } else {
                val = value
            }
        }
        return model.createStatement(subject, property, val);
    }

    private Long findSpeciesIdFromName(String name) {
        XMLConverter converter = new XMLConverter();
        TaxonomyDefinition taxon = converter.getTaxonConceptFromName(name, TaxonomyRank.SPECIES.ordinal(), false, null);
        return taxon ? taxon.findSpeciesId() : null;
    }

    List listFacts(Long id=null, String trait = null) {
        if(!dataset) {
            init();
        }
 
        def result = [];
        dataset.begin(ReadWrite.READ);
        try {
            String subjectURI;
            if(id) {
                subjectURI = resourceURI + id;
            } else {
                subjectURI = 'urn:x-arq:UnionGraph';
            }
            Model model = getModel(subjectURI);
            log.debug "LOADING FACTS MODEL FOR ${subjectURI}"
            model.write(System.out, "RDF/XML-ABBREV");

            String pre = StrUtils.strjoinNL(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
                "PREFIX j.0: <http://indiabiodiversity.org/schema/terms/>",
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>");

            String queryString = '';
            if(trait) {
                trait = trait.replaceAll("\\s+",'_');
                queryString =  StrUtils.strjoinNL( pre+"\nSELECT ?species ?metadata ?value", 
                'WHERE { ',
                '    ?species rdf:type "Species" .',
                '    ?species j.0:'+trait+' [dc:title ?value] ',
                '}') ; 
            } else {
                queryString =  StrUtils.strjoinNL( pre+"\nSELECT ?species", 
                'WHERE { ',
                '    ?species rdf:type "Species" ',
                '}') ; 
            }

            println queryString
            Query query = QueryFactory.create(queryString) ;
            QueryExecution qe = QueryExecutionFactory.create(query, model);
            for (ResultSet rs = qe.execSelect(); rs.hasNext() ; ) {
                QuerySolution soln = rs.nextSolution();
                if(trait) {
                    Map<String, Object> row = [:]
                    for (Iterator<String> varNames = soln.varNames(); varNames.hasNext(); ) {
                        String varName = varNames.next()
                        RDFNode varNode = soln.get(varName)
                        row.put(varName, (varNode.isLiteral() ? varNode.asLiteral().value : varNode.toString()))
                    }
                    row['predicate'] = trait;
                    println "-------------------------------------______"
                    println row
                    String iconURL = getIconURLForPropertyValue(row['predicate'], row['value']);
                    if(iconURL) 
                        row['icon'] = iconURL;

                    result << row
                } else {
                    //print all properties of species
                    Resource subject = soln.getResource("species") ; // Get a result variable - must be a resource
                    //while (iter.hasNext()) {
                    //  Resource subject = iter.nextResource();
                    StmtIterator stmtIter = subject.listProperties();
                    while(stmtIter.hasNext()) {
                        Statement stmt      = stmtIter.nextStatement();  // get next statement
                        Property  predicate = stmt.getPredicate();   // get the predicate
                        RDFNode   object    = stmt.getObject();      // get the object
                        def objectValue;
                        List metadata;
                        if(object.isResource()) {
                            objectValue = object.asResource().getProperty(DC.title).getObject();
                            metadata = getResourceAsList(object.asResource());
                        } else {
                            objectValue = object;
                        }

                        Map r = ['species':subject.toString(), 'predicate':predicate.getLocalName(), 'value':objectValue.toString()];
                        if(metadata) {
                            r['metadata'] = metadata;
                        }
                        String iconURL = getIconURLForPropertyValue(predicate.getLocalName(), objectValue.toString());
                        if(iconURL) 
                            r['icon'] = iconURL;
                        result << r;
                    }
                }
            }
        } finally {
            dataset.end();
        }
        return result;
    } 

    private List getResourceAsList(Resource resource) {
        List result = []
        StmtIterator iter = resource.listProperties();
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();  // get next statement
            Resource  subject   = stmt.getSubject();     // get the subject
            Property  predicate = stmt.getPredicate();   // get the predicate
            RDFNode   object    = stmt.getObject();      // get the object
            def objectValue;
            Map metadata = [:];
            if(object.isResource()) {
                objectValue = object.asResource().getProperty(DC.title).getObject();
                metadata = getAsMap(object.asResource());
            } else {
                objectValue = object;
            }
            Map r = ['subject':subject.getLocalName(), 'predicate':predicate.getLocalName(), 'value':objectValue.toString()];
            if(metadata) {
                r['metadata'] = metadata;
            }
            result << r;
        }
        return result;
    }

    private String getIconURLForPropertyValue(String predicateName, String value) {
        return propertyValueIcons.get(predicateName.trim().toLowerCase())?.get(value.trim().toLowerCase()); 
    }

    List listTraits(Long id=null) {
        if(!dataset) {
            init();
        }
 
        def result = [];
        dataset.begin(ReadWrite.READ);
        try {
            String subjectURI;
            if(id) {
                subjectURI = resourceURI + id;
            } else {
                subjectURI = 'urn:x-arq:UnionGraph';
            }
            Model model = getModel(subjectURI);
            log.debug "LOADING FACTS MODEL FOR ${subjectURI}"

            String pre = StrUtils.strjoinNL(
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
                "PREFIX j.0: <http://indiabiodiversity.org/schema/terms/>",
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>");

            String queryString = StrUtils.strjoinNL( pre+"\nSELECT DISTINCT ?trait ", 
                " WHERE ",
                "{ ?subject ?trait [rdf:type 'Trait'] } ");

            println queryString
            Query query = QueryFactory.create(queryString) ;
            QueryExecution qe = QueryExecutionFactory.create(query, model);
            for (ResultSet rs = qe.execSelect(); rs.hasNext() ; ) {
                QuerySolution soln = rs.nextSolution();
                /*Map<String, Object> row = [:]
                for (Iterator<String> varNames = soln.varNames(); varNames.hasNext(); ) {
                    String varName = varNames.next()
                    RDFNode varNode = soln.get(varName)
                    row.put(varName, (varNode.isLiteral() ? varNode.asLiteral().value : varNode.toString()))
                }
                println "-------------------------------------______"
                println row
                */
                result << soln.get('trait').getLocalName().replace('_', ' ');
            }
        } finally {
            dataset.end();
        }
        return result;
    } 
    
    private boolean isValidTrait(String traitName, traitValue, Long id) {
        Trait trait = Trait.findByName(traitName);
        if(!trait) return;
        boolean isValid = false;
        trait.valueContraints.each { constraint ->
            isValid = isValid && constraint.validate(trait, traitValue);
        }
        return isValid && isValidPropertyAsPerTaxon(trait, id);
    }

    private boolean isValidPropertyAsPerTaxon(Trait trait, Long id) {
        Species speciesInstance = Species.read(id);
        if(!speciesInstance || !trait) return false;
        List<TaxonomyDefinition> parentTaxon = speciesInstance.taxonConcept.parentTaxon();
        return parentTaxon.intersect(trait.taxon).size() > 0
    }
}
