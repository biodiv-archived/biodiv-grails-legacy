package species.sourcehandler.importer

import species.Resource;
import species.Resource.ResourceType;
import grails.converters.JSON;
import java.io.InputStream;
import species.dataset.DataTable;
import species.participation.Observation;

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.text.SimpleDateFormat;

import speciespage.ObvUtilService;

class DataTableObservationImporter extends AbstractObservationImporter {

    protected static DataTableObservationImporter _instance;

    DtObservationReader dtObservationReader;
    private Map changedCols;

    public  static  DataTableObservationImporter getInstance() {
        if( !_instance) {
            _instance = new DataTableObservationImporter();
        }
        return _instance;
    }

    List next(Map mediaInfo, int limit, File uploadLog=null) {
        return importObservations(mediaInfo, limit, uploadLog);
    }

    void initReaders(DataTable dataTable) {
        if(!dataTable){
            return;
        }

        log.debug "Initializing readers to observation and multimedia files"
        dtObservationReader = new DtObservationReader(dataTable);
    }

    void closeReaders() {
        log.debug "Closing readers to observation and multimedi files"
//        closeReader(dtObservationReader)
    }

    Map importData(DataTable dataTable, File mappingFile, File multimediaMappingFile, File uploadLog=null, Map changedCols = null) {
        log.info "Import started from ${dataTable} from mappingFile ${mappingFile}";
        if(uploadLog) 
            uploadLog << "\nImport started from ${dataTable} from mappingFile ${mappingFile}";

        if(!dataTable)
            return;

        initReaders(dataTable);
        readMappingHeadersFromMappingFile(dataTable.columns, mappingFile, multimediaMappingFile, uploadLog); 
        Map mediaInfo = [:];
        //if(multimediaFile && multimediaFile.exists() && mediaReader && multimediaMappingFile.exists())
        //    mediaInfo = readMedia();
        //else
        mediaReader = null;
        this.changedCols = changedCols;
        return ['observations':[], 'mediaInfo':mediaInfo];
        //closeReaders();
    }

    protected void readMappingHeadersFromMappingFile(String columns, File mappingFile, File multimediaMappingFile, File uploadLog=null) {
        readHeaders(uploadLog);
    
        def mappingFileReader = getCSVReader(mappingFile, (char)'\t');
        Map metaFields = [:]//new String[metaXML.core.field.size()];
        String[] row = mappingFileReader.readNext();
        while(row) {
            println row;
            metaFields[row[0]] = ['columnName':row[1]];
            row = mappingFileReader.readNext();
        }

        println "\nRead headers mapping from meta ${metaFields}"

        Map multimediaMetaFields = [:];
        if(multimediaMappingFile) {
            def multimediaMappingFileReader = getCSVReader(multimediaMappingFile);
            row = multimediaMappingFileReader.readNext()
            while(row) {
                multimediaMetaFields[row[0]] = ['columnName':row[1]];
                row = multimediaMappingFileReader.readNext();
            }

            println "\nRead multimedia headers mapping from meta ${multimediaMetaFields}"
        }
        readMappingHeaders(columns, metaFields, multimediaMetaFields, uploadLog);
    }

    protected void readMappingHeaders(String columns, Map metaFields, Map multiMediaMetaFields, File uploadLog=null) { 
        def ipCol = JSON.parse(columns);
        observationHeader = new ArrayList();//new Map[dwcObvMapping.size()];
        ipCol.eachWithIndex { h, i ->
            if(h) {
                println "----------------------------_______"
                if(!observationHeader[i]) observationHeader[i] = [];
                observationHeader[i] << h;
            }
        } 
        //observationHeader.sort {it?it.order:10000000}
        log.debug "Observation Headers ${observationHeader}"
        if(uploadLog) uploadLog << "\n\n ObservationHeader : ${observationHeader}"
       
/*      if(mediaReader) {
            dwcMediaHeader = mediaReader.readNext();
            mediaHeader = new ArrayList();
            dwcMediaHeader.eachWithIndex { h, i ->
                if(h) {
                    List<String> urls = findMappedColumnInIp(multiMediaMetaFields, h, i);
                    urls.each { url ->
                        def mappedMediaHeader = getMappedMediaHeader(url, dwcMultimediaMapping);
                        println mappedMediaHeader
                        if(!mediaHeader[i]) mediaHeader[i] = [];
                        mediaHeader[i] << mappedMediaHeader;
                    }
                }
             }
         }
        println "Multimedia Headers ${mediaHeader}"
*/    
    } 

    private List importObservations(Map mediaInfo, int limit, File uploadLog=null) {
        log.debug "Reading observations"
        List obvParams = [];
        int no=1;

        List observations = dtObservationReader.iterator().next();
        observations.each { obv ->
            println "from row ${obv}";
            if(uploadLog) "\nReading observation from row ${obv}";
            try {
                def p = importObservation(obv);
                obvParams << p;
            } catch(Exception e) {
                e.printStackTrace();
                if(uploadLog) uploadLog << e.printStackTrace();
                if(uploadLog) uploadLog << "\n${e.getMessage()}"
            }
        }
        log.debug "from params ${obvParams}"
        return obvParams;
    }

    private Map importObservation(Observation obv) {
        log.debug "Importing observation ${obv}"
        Map obvParams = ObvUtilService.getObservationProperties(obv);
        log.debug "Got existing obv params as : ${obvParams}"
        log.debug "Changed columns are : ${changedCols}"
        Map m = obvParams.clone();
        observationHeader.eachWithIndex { headers, i ->

            headers.each { header ->
                println "Header : "+header

                if(header) {
                    Map changedColMarking = changedCols[header[1]];
                    String newColUrl,oldColUrl;
                    if(changedColMarking) {
                        log.debug "This column marking got changed ${changedColMarking}"
                        newColUrl = changedColMarking.newMarking;
                        oldColUrl = changedColMarking.oldMarking;
                    } else {
                        //to force parsing values as per current marking
                        newColUrl = header[0];
                        //oldColUrl = header[0];
                    }

                    if(oldColUrl || newColUrl) {
                        //delete old marking value
                        if(oldColUrl) {
                            if(dwcObvMapping[oldColUrl]) {
                                m.remove(dwcObvMapping[oldColUrl].field);
                            } else if(oldColUrl.startsWith("http://ibp.org/terms/observation/")) {
                                m.remove(oldColUrl.replace("http://ibp.org/terms/observation/",""));
                                //TODO:post2Usergroups check
                            } else if(oldColUrl.startsWith("http://ibp.org/terms/trait/")) {
                                m[TRAIT_HEADER][oldColUrl.replace("http://ibp.org/terms/trait/","")]=null;
                            } else {
                                log.debug "Could not remove ${oldColUrl}"
                            }
                        }

                        //create new marking value
                        if(newColUrl =~ /http:\/\/rs.tdwg.org\/dwc\/terms\/*/) {
                            m[dwcObvMapping[newColUrl].field] =  getValue(obvParams, header[1], oldColUrl);
                        } else if(newColUrl =~ /http:\/\/purl.org\/dc\/terms\/*/) {
                            m[dwcObvMapping[newColUrl].field] =  getValue(obvParams, header[1], oldColUrl);
                        } else if(newColUrl.startsWith("http://ibp.org/terms/observation/")) {
                            m[newColUrl.replace("http://ibp.org/terms/observation/","")] = getValue(obvParams, header[1], oldColUrl);
                        } else if(newColUrl.startsWith("http://ibp.org/terms/trait/")) {
                            if(!m[TRAIT_HEADER]) m[TRAIT_HEADER] =  new java.util.LinkedHashMap();
                            m[TRAIT_HEADER][newColUrl.replace("http://ibp.org/terms/trait/","")] = getValue(obvParams, header[1], oldColUrl);
                        } else {
                            log.debug "Ignoring changed column marking ${changedColMarking}"
                        }
                    }
                }
            }
        }
        println "888888888888888888888"
        log.debug "Changed obv parameters : ${m}";
        println "888888888888888888888"
        return m;
    }

    private String getValue(Map obvParams, String column, String oldColUrl) {
    println column
    println oldColUrl
        if(!oldColUrl) 
            return obvParams[AbstractObservationImporter.ANNOTATION_HEADER][column];
        if(dwcObvMapping[oldColUrl]) {
            return obvParams.get(dwcObvMapping[oldColUrl].field);
        } else if(oldColUrl.startsWith("http://ibp.org/terms/observation/")) {
            return obvParams.get(oldColUrl.replace("http://ibp.org/terms/observation/",""));
            //TODO:post2Usergroups check
        } else if(oldColUrl.startsWith("http://ibp.org/terms/trait/")) {
            return obvParams[TRAIT_HEADER][oldColUrl.replace("http://ibp.org/terms/trait/","")];
        } else {
            log.debug "Could not gete ${oldColUrl}"
        }
    }
}

class DtObservationReader implements Iterable {
    DataTable dataTable;
    private int offset = 0
    private int limit = 10
    private int total;
    
    DtObservationReader(DataTable dataTable) {
        this.dataTable = dataTable;
        total = dataTable.getDataObjectsCount();
    }

    @Override
    Iterator iterator() {
        [
            hasNext: { offset <= total },
            next: { 
                def t = dataTable.getObservationData(dataTable.id, [max:limit, offset:offset])
                offset = offset + limit;
                println t
                return t;
            }
        ] as Iterator
    }
}

