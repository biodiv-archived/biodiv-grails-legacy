package species.sourcehandler.importer

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import species.Resource;
import species.Resource.ResourceType;
import grails.converters.JSON;
import java.io.InputStream;

class DwCObservationImporter extends AbstractObservationImporter {

    protected static DwCObservationImporter _instance;

    public static  DwCObservationImporter getInstance() {
        if(!_instance) {
            _instance = new DwCObservationImporter();
        }
        return _instance;
    }

    Map importData(String directory, File uploadLog=null) {
        log.info "Darwin Core import started"
        if(uploadLog) uploadLog << "\nDarwin Core import started"

        if(!directory)
            return;

        initReaders(new File(directory, 'occurrence.txt'), new File(directory, 'multimedia.txt'));
        readMappingHeadersFromMeta(directory, uploadLog); 
        Map mediaInfo = [:];
        if(mediaReader)
            mediaInfo = readMedia();
        return ['observations':[], 'mediaInfo':mediaInfo];
        //closeReaders();
    }

}
