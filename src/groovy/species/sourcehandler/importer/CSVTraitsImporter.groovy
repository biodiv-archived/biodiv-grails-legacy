package species.sourcehandler.importer

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import grails.converters.JSON;
import java.io.InputStream;

class CSVTraitsImporter extends AbstractImporter {

    protected static CSVTraitsImporter _instance;

    protected CSVReader reader;
    protected String[] headers;
    protected String prefix = '';

    public static  CSVTraitsImporter getInstance() {
        if(!_instance) {
            _instance = new CSVTraitsImporter();
        }
        return _instance;
     }

    protected String[] readHeaders(File uploadLog = null) {
        //assuming first line to be headers
        String[] h = reader.readNext();
        println h;
        println h[0];
        return h;
    }
    
    Map importData(File file, File uploadLog=null) {

        log.info "Import started from ${file}"
        if(uploadLog) uploadLog << "\nImport started from ${file}"

        if(!file)
            return;

        reader = initReader(file);
        headers = readHeaders(file);
        //call next to begin importing
        return [:];
    }

    protected List _importData(int limit, File uploadLog=null) {
        log.debug "Reading traits"
        List params = [];
        int no=1;

        String[] row = reader.readNext()
        while(row) {
            println "from row ${row}"
            if(uploadLog) "\nReading data from row ${row}"
                try {
                    def p = _importData(row);
                    params << p;
                } catch(Exception e) {
                    e.printStackTrace();
                    if(uploadLog) uploadLog << e.printStackTrace();
                    if(uploadLog) uploadLog << "\n${e.getMessage()}"
                }
            if(no++ >= limit) break;
            row = reader.readNext()
        }
        log.debug "from params ${params}"
        println params
        return params;
    }

    protected Map _importData(String[] row) {
        Map m = new LinkedHashMap();
        println "headers"
        println headers
        headers.eachWithIndex { header, i ->
            println header
            m[header] = row[i];
        }
        println "888888888888888888888"
        println m
        println "888888888888888888888"
        return m;
    }

    List next(int limit, File uploadLog=null) {
        return _importData(limit, uploadLog);
    }

    void closeReader() {
        println reader;
        closeReader(reader);
    }
}

