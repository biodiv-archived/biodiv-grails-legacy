package species.sourcehandler.importer

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import species.Resource;
import species.Resource.ResourceType;
import grails.converters.JSON;
import java.io.InputStream;

class FileObservationImporter extends AbstractObservationImporter {

    protected static FileObservationImporter _instance;

    public static  FileObservationImporter getInstance() {
        if(!_instance) {
            _instance = new FileObservationImporter();
        }
        return _instance;
    }

    Map importData(File observationsFile, File multimediaFile, File mappingFile, File multimediaMappingFile, File uploadLog=null) {
        log.info "Import started from ${observationsFile} ${multimediaFile} using mappingFile ${mappingFile} and multimediaMappingFile ${multimediaMappingFile}";
        if(uploadLog) 
            uploadLog << "\nImport started from ${observationsFile} using mappingFile ${mappingFile} and multimediaMappingFile ${multimediaMappingFile}";

        if(!observationsFile)
            return;

        initReaders(observationsFile, multimediaFile);
        readMappingHeadersFromMappingFile(mappingFile, multimediaMappingFile, uploadLog); 
        Map mediaInfo = [:];
        if(multimediaFile && multimediaFile.exists() && mediaReader && multimediaMappingFile.exists())
            mediaInfo = readMedia();
        else 
            mediaReader = null;
        return ['observations':[], 'mediaInfo':mediaInfo];
        //closeReaders();
    }

}
