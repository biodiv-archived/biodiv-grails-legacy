package species.sourcehandler.importer

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import species.Resource;
import species.Resource.ResourceType;
import grails.converters.JSON;
import java.io.InputStream;

abstract class AbstractObservationImporter extends AbstractImporter {

    public static String ANNOTATION_HEADER = 'Annotations';
    public static String MEDIA_ANNOTATION_HEADER = 'media_annotations';
    protected static DwCObservationImporter _instance;

    protected CSVReader observationReader
    protected CSVReader mediaReader
    protected def observationHeader;
    protected String[] dwcObvHeader;
    protected String[] dwcMediaHeader;
    protected def mediaHeader;
    protected Map dwcObvMapping;
    protected Map dwcMultimediaMapping;


    List next(Map mediaInfo, int limit, File uploadLog=null) {
        return importObservations(mediaInfo, limit, uploadLog);
    }

    void initReaders(File observationsFile, File multimediaFile) {
        if(!observationsFile.exists()){
            return;
        }

        log.debug "Initializing readers to observation and multimedi files"
        observationReader = initReader(observationsFile);
        if(multimediaFile)
            mediaReader = initReader(multimediaFile);//, 'multimedia.txt')
    }

    void closeReaders() {
        log.debug "Closing readers to observation and multimedi files"
        closeReader(observationReader)
        closeReader(mediaReader)
    }

    //dwcObvMapping[url for the field from standard] = [field:IBP field name, order: display order for the field on obv show]
    protected String[] readHeaders(File uploadLog=null) {
        //read dwcObvMapping
        InputStream dwcObvMappingFile = this.class.classLoader.getResourceAsStream('species/dwcObservationMapping.tsv')
        dwcObvMapping = [:];
        int l=0;
        dwcObvMappingFile.eachLine { line ->
            if(l++>0) { 
                String[] parts = line.split(/\t/)
                if(parts.size()>8 && (parts[6] || parts[7])) {
                    dwcObvMapping[parts[1]] = ['field':parts[7], 'order':Float.parseFloat(parts[6])];
                }
                else if(parts.size()>7 && parts[6]) {
                    dwcObvMapping[parts[1]] = ['field':'', 'order':Float.parseFloat(parts[6])];
                }
            }
        }

        println "dwcObvMapping : ${dwcObvMapping}"

        InputStream dwcMultimediaMappingFile = this.class.classLoader.getResourceAsStream('species/dwcMultimediaMapping.tsv')
//        File dwcMultimediaMappingFile = new File("/home/sravanthi/git/biodiv/grails-app/conf/species/dwcMultimediaMapping.tsv");
        dwcMultimediaMapping = [:];
        dwcMultimediaMappingFile.eachLine { line ->
            String[] parts = line.split(/\t/)
            if(parts.size()>4 && parts[4])
                dwcMultimediaMapping[parts[1]] = parts[4]
        }
        println "dwcMultimediaMapping : ${dwcMultimediaMapping}"

    }

    protected readMappingHeadersFromMeta(String directory, File uploadLog=null) {
        
        readHeaders(uploadLog);

        //read meta.xml
        String metaXMLStr = new File("$directory/meta.xml").text;
        def metaXML = new XmlParser().parseText(metaXMLStr);
        //String[] metaFields = new String[metaXML.core.field.size()];
        Map metaFields = [:]//new String[metaXML.core.field.size()];
        metaXML.core.field.each {
            metaFields[it.attribute('term')] = ['index':Integer.parseInt(it.attribute('index'))];
        }

        log.debug "Read headers mapping from meta ${metaFields}"

        //String[] multiMediaMetaFields = new String[metaXML.extension.files.location.findAll{it.text() == 'multimedia.txt'}[0].parent().parent().field.size()];
        Map multimediaMetaFields = [:];
        metaXML.extension.files.location.findAll{it.text() == 'multimedia.txt'}[0].parent().parent().field.each {
            multimediaMetaFields[it.attribute('term')] = ['index':Integer.parseInt(it.attribute('index'))];
        }

        log.debug "Read multimedia headers mapping from meta ${multimediaMetaFields}"

        readMappingHeaders(metaFields, multimediaMetaFields, uploadLog);
    }

    protected void readMappingHeadersFromMappingFile(File mappingFile, File multimediaMappingFile, File uploadLog=null) {
        readHeaders(uploadLog);
    
        def mappingFileReader = getCSVReader(mappingFile);
        Map metaFields = [:]//new String[metaXML.core.field.size()];
        String[] row = mappingFileReader.readNext();
        while(row) {
            metaFields[row[0]] = ['columnName':row[1]];
            row = mappingFileReader.readNext();
        }

        println "\nRead headers mapping from meta ${metaFields}"

        def multimediaMappingFileReader = getCSVReader(multimediaMappingFile);
        Map multimediaMetaFields = [:];
        row = multimediaMappingFileReader.readNext()
        while(row) {
            multimediaMetaFields[row[0]] = ['columnName':row[1]];
            row = multimediaMappingFileReader.readNext();
        }

        println "\nRead multimedia headers mapping from meta ${multimediaMetaFields}"

        readMappingHeaders(metaFields, multimediaMetaFields, uploadLog);
    }

    protected void readMappingHeaders(Map metaFields, Map multiMediaMetaFields, File uploadLog=null) { 
        dwcObvHeader = observationReader.readNext();
        observationHeader = new ArrayList();//new Map[dwcObvHeader.size()];
        dwcObvHeader.eachWithIndex { h, i ->
            if(h) {
            println "----------------------------_______"
            List<String> urls = findMappedColumnInIp(metaFields, h, i);
            urls.each { url ->
                println url
                Map mappedObvHeader = getMappedObvHeader(url, dwcObvMapping);
                if(!mappedObvHeader) mappedObvHeader = [:];
                mappedObvHeader['url'] = url;
                mappedObvHeader['column'] = i;
                println mappedObvHeader
                if(!observationHeader[i]) observationHeader[i] = [];
                observationHeader[i] << mappedObvHeader;
             }
            }
        } 
        //observationHeader.sort {it?it.order:10000000}
        log.debug "Observation Headers ${observationHeader}"
        if(uploadLog) uploadLog << "\n\n ObservationHeader : ${observationHeader}"
       
        if(mediaReader) {
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
    } 

    private List<String> findMappedColumnInIp(Map metaFields, String columnName, int index) {
        String f;
        println columnName+"   "+index
        List mappedCol = [];
        metaFields.each { key, value ->
            f = null;
            if(value.columnName == columnName) f = key;
            else if(value.index == index) f = key;
            if(f) mappedCol << f
        }
        if(!mappedCol) mappedCol << null;
        println mappedCol;
        return mappedCol;
    }

    protected Map getMappedObvHeader(String header, Map dwcObvMapping) {
        println header
        return dwcObvMapping[header];
    }
    
    protected String getMappedMediaHeader(String header, Map dwcMediaMapping) {
        return dwcMediaMapping[header];
     }

    protected Map readMedia() {
        println "Reading media"
        Map mediaParams = [:];
        String[] row = mediaReader.readNext()
        while(row) {
            println "from row ${row}"
            Map x = importMedia(row);
            if(!mediaParams[x.obvExternalId])
                mediaParams[x.obvExternalId] = [];
            mediaParams[x.obvExternalId] << x;            
            row = mediaReader.readNext()
        }
        println "got media params ${mediaParams}"
        return mediaParams;
    }

    private Map importMedia(String[] row) {
        Map m = [:];
        mediaHeader.eachWithIndex { headers, i ->
            headers.each { header ->
                println header
                if(header && row.size()>i && row[i]) {
                    m[header] = row[i]
                } else if(row[i]) {
                    if(!m[MEDIA_ANNOTATION_HEADER]) m[MEDIA_ANNOTATION_HEADER] =  new java.util.LinkedHashMap();
                    m[MEDIA_ANNOTATION_HEADER][dwcMediaHeader[i]] = row[i];  
                }
            }
        }
        return m;
    }

    private List importObservations(Map mediaInfo, int limit, File uploadLog=null) {
        log.debug "Reading observations"
        List obvParams = [];
        int no=1;

        String[] row = observationReader.readNext()
        while(row) {
            println "from row ${row}"
            if(uploadLog) "\nReading observation from row ${row}"
            try {
            def p = importObservation(row);
            Map m = [:];
            if(mediaInfo[p['externalId']]) {
                mediaInfo[p['externalId']].eachWithIndex { media, i ->
                    println "media "+media
                    media.eachWithIndex { mInfo ->
                        def v = mInfo.value;
                        switch(mInfo.value) {
                            case 'StillImage' :
                            v = ResourceType.IMAGE.value();
                            break;
                            case 'MovingImage':
                            v = ResourceType.VIDEO.value();
                            break;
                            case 'Sound':
                            v = ResourceType.AUDIO.value();
                            break;
                        }
                         if(mInfo.key.equals(MEDIA_ANNOTATION_HEADER)) {
                            m[mInfo.key+"_"+i] = v as JSON;
                        } else {
                            m[mInfo.key+"_"+i] = v;
                        }
                    }
                    if(!m['type'+"_"+i])
                        m['type'+'_'+i] = ResourceType.IMAGE.value();
                }
                m['resourceListType'] = 'ofObv';
            }
            p['mediaInfo'] = m;
            obvParams << p;
            } catch(Exception e) {
                e.printStackTrace();
                if(uploadLog) uploadLog << e.printStackTrace();
                if(uploadLog) uploadLog << "\n${e.getMessage()}"
            }
            if(no++ >= limit) break;
            row = observationReader.readNext()
        }
        log.debug "from params ${obvParams}"
        return obvParams;
    }

    private Map importObservation(String[] row) {
        Map m = new LinkedHashMap();
        observationHeader.eachWithIndex { headers, i ->
            
            headers.each { header ->
                println header

                if(header) {
                    if(header.field && row.size()>header.column && row[header.column]) {
                        if(m[header.field]) {
                            m[header.field] += ', '+row[header.column]
                        } else {
                            m[header.field] = row[header.column]
                        }
                    } 

                    if(row[header.column]) {
                        if(!m[ANNOTATION_HEADER]) m[ANNOTATION_HEADER] =  new java.util.LinkedHashMap();
                        String value = row[header.column];
                        switch(dwcObvHeader[header.column].toLowerCase()) {
                            case 'gbifid' :
                                value = "http://www.gbif.org/occurrence/"+value; break;
                            case 'datasetkey' :
                            value = 'http://www.gbif.org/dataset/'+value; break;
                        } 
                        m[ANNOTATION_HEADER][dwcObvHeader[header.column]] = value;  
                    }
                }
            }
        }
        println "888888888888888888888"
        println m
        println "888888888888888888888"
        return m;
    }

}
