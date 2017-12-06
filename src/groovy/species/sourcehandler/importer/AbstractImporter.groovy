package species.sourcehandler.importer

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import species.Resource;
import species.Resource.ResourceType;
import grails.converters.JSON;
import java.io.InputStream;

abstract class AbstractImporter {

    public char separator = '\t';
    
    public CSVReader getCSVReader(String directory, String fileName) {
        char separator = '\t'
        File f = new File("$directory/$fileName");
        return getCSVReader(f);
    }

    public CSVReader getCSVReader(File file, char separator = separator) {
        //char separator = '\t'
        if(file.exists()) {
            CSVReader reader = new CSVReader(new FileReader(file), separator, CSVWriter.NO_QUOTE_CHARACTER);
            return reader
        }
        return null;
    }

    public CSVReader initReader(File file) {
        if(!file) return;
        return getCSVReader(file);//targetDir, 'occurrence.txt')
    }

    public void closeReader(reader) {
        if(reader) reader.close();
    }

    protected String[] readHeaders(File uploadLog=null) {
        
    }

    def CSVWriter getCSVWriter(String directory, String fileName) {
        char separator = '\t'
        File dir =  new File(directory)
        if(!dir.exists()){
            dir.mkdirs()
        }
        return new CSVWriter(new FileWriter("$directory/$fileName"), separator, CSVWriter.NO_QUOTE_CHARACTER );
    }
//    abstract Map importData(String directory, File uploadLog=null);
}


