package species.sourcehandler.importer

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import species.Resource;
import species.Resource.ResourceType;
import grails.converters.JSON;
import java.io.InputStream;

class DwCObservationImporter {

    public static String ANNOTATION_HEADER = 'Annotations';
    public static String MEDIA_ANNOTATION_HEADER = 'media_annotations';
    protected static DwCObservationImporter _instance;

    private CSVReader observationReader
    private CSVReader mediaReader
    private String[] observationHeader;
    private String[] dwcObvHeader;
    private String[] dwcMediaHeader;
    private String[] mediaHeader;

    public static  DwCObservationImporter getInstance() {
        if(!_instance) {
            _instance = new DwCObservationImporter();
        }
        return _instance;
    }

    List importObservationData(String directory) {
        log.info "Darwin Core import started"

        if(!directory)
            return;

        initReaders(directory);
        readHeaders(directory); 
        Map mediaInfo = [:];
        if(mediaReader)
            mediaInfo = readMedia();
        List obvParamsList = importObservations(mediaInfo);
        closeReaders();
        return obvParamsList;
    }

    protected void initReaders(String targetDir) {
        File target = new File(targetDir)
        if(!target.exists()){
            return;
        }

        observationReader = getCSVReader(targetDir, 'occurrence.txt')
        mediaReader = getCSVReader(targetDir, 'multimedia.txt')
    }

    protected void closeReaders() {
        observationReader.close()
        mediaReader?.close()
    }

    public CSVReader getCSVReader(String directory, String fileName) {
        char separator = '\t'
        File f = new File("$directory/$fileName");
        if(f.exists()) {
            CSVReader reader = new CSVReader(new FileReader("$directory/$fileName"), separator, CSVWriter.NO_QUOTE_CHARACTER);
            return reader
        }
        return null;
    }

    protected void readHeaders(String directory) {
        //read dwcObvMapping
        InputStream dwcObvMappingFile = this.class.classLoader.getResourceAsStream('species/dwcObservationMapping.tsv')
        Map dwcObvMapping = [:];
        dwcObvMappingFile.eachLine { line ->
            String[] parts = line.split(/\t/)
            if(parts.size()>6 && parts[6])
                dwcObvMapping[parts[1]] = parts[6]
        }

        InputStream dwcMultimediaMappingFile = this.class.classLoader.getResourceAsStream('species/dwcMultimediaMapping.tsv')
//        File dwcMultimediaMappingFile = new File("/home/sravanthi/git/biodiv/grails-app/conf/species/dwcMultimediaMapping.tsv");
        Map dwcMultimediaMapping = [:];
        dwcMultimediaMappingFile.eachLine { line ->
            String[] parts = line.split(/\t/)
            if(parts.size()>4 && parts[4])
                dwcMultimediaMapping[parts[1]] = parts[4]
        }

        //read meta.xml
        String metaXMLStr = new File("$directory/meta.xml").text;
        def metaXML = new XmlParser().parseText(metaXMLStr);
        String[] metaFields = new String[metaXML.core.field.size()];
        metaXML.core.field.each {
            metaFields[Integer.parseInt(it.attribute('index'))] = it.attribute('term');
        }

        String[] multiMediaMetaFields = new String[metaXML.extension.files.location.findAll{it.text() == 'multimedia.txt'}[0].parent().parent().field.size()];
        metaXML.extension.files.location.findAll{it.text() == 'multimedia.txt'}[0].parent().parent().field.each {
            multiMediaMetaFields[Integer.parseInt(it.attribute('index'))] = it.attribute('term');
        }

        dwcObvHeader = observationReader.readNext();
        observationHeader = new String[dwcObvHeader.size()];
        dwcObvHeader.eachWithIndex { h, i ->
            String mappedObvHeader = getMappedObvHeader(metaFields[i], dwcObvMapping);
            observationHeader[i] = mappedObvHeader;
        }
       
        if(mediaReader) {
            dwcMediaHeader = mediaReader.readNext();
            mediaHeader = new String[dwcMediaHeader.size()];
            dwcMediaHeader.eachWithIndex { h, i ->
                String mappedMediaHeader = getMappedMediaHeader(multiMediaMetaFields[i], dwcMultimediaMapping);
                mediaHeader[i] = mappedMediaHeader;
            }
        }
        println mediaHeader;
    }

    protected String getMappedObvHeader(String header, Map dwcObvMapping) {
        return dwcObvMapping[header];
    }
    
    protected String getMappedMediaHeader(String header, Map dwcMediaMapping) {
        return dwcMediaMapping[header];
    }

    protected Map readMedia() {
        Map mediaParams = [:];
        String[] row = mediaReader.readNext()
        while(row) {
            Map x = importMedia(row);
            if(!mediaParams[x.obvExternalId])
                mediaParams[x.obvExternalId] = [];
            mediaParams[x.obvExternalId] << x;            
            row = mediaReader.readNext()
        }
        println "====================+++++++"
        println mediaParams
        println "====================+++++++"
        return mediaParams;
    }

    private Map importMedia(String[] row) {
        Map m = [:];
        mediaHeader.eachWithIndex { header, i ->
            if(header && row.size()>i && row[i]) {
                m[header] = row[i]
            } else if(row[i]) {
                if(!m[MEDIA_ANNOTATION_HEADER]) m[MEDIA_ANNOTATION_HEADER] =  new java.util.LinkedHashMap();
                m[MEDIA_ANNOTATION_HEADER][dwcMediaHeader[i]] = row[i];  
            }
        }
        return m;
    }

    private List importObservations(Map mediaInfo) {
        List obvParams = [];
        String[] row = observationReader.readNext()
        while(row) {
            def p = importObservation(row);
            println mediaInfo[p['externalId']];
            println "-----------____"
            Map m = [:];
            if(mediaInfo[p['externalId']]) {
                mediaInfo[p['externalId']].eachWithIndex { media, i ->
                    media.eachWithIndex { mInfo ->
                        def v = mInfo.value;
                        println "************************"
                        println mInfo
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
                }
                m['resourceListType'] = 'ofObv';
            }
            p['mediaInfo'] = m;
            obvParams << p 
            row = observationReader.readNext()
        }
        println obvParams;
        return obvParams;
    }

    private Map importObservation(String[] row) {
        Map m = [:];
        observationHeader.eachWithIndex { header, i ->
            if(header && row.size()>i && row[i]) {
                m[header] = row[i]
            } 
            
            if(row[i]) {
                if(!m[ANNOTATION_HEADER]) m[ANNOTATION_HEADER] =  new java.util.LinkedHashMap();
                m[ANNOTATION_HEADER][dwcObvHeader[i]] = row[i];  
            }
        }
        println "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
        println m[ANNOTATION_HEADER]
        return m;
    }

}
