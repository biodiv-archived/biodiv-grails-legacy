package species.participation

import java.util.Date;
import grails.converters.JSON;
import grails.util.Holders;

import species.auth.SUser;
import speciespage.ObvUtilService;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log;
import org.apache.log4j.Level;

class UploadLog {
    private static Log log = LogFactory.getLog(this);

    def utilsService

    public enum Status {
        VALIDATION("VALIDATION"),
        SCHEDULED("SCHEDULED"),
        RUNNING("RUNNING"),
        ABORTED("ABORTED"),
        FAILED("FAILED"),
        UPLOADED("UPLOADED"),
        ROLLBACK("ROLLBACK"),
        SUCCESS("SUCCESS"),
        VALIDATION_FAILED("VALIDATION FAILED")

        private String value;

        Status(String value) {
            this.value = value;
        }

        String value() {
            return this.value;
        }
    }

    Date startDate;
    Date endDate;
    String notes;
    Status status;
    String filePath;
    String errorFilePath;
    String uploadType;
    String logFilePath;
    String paramsMapAsText

    File logFile
    static belongsTo = [author:SUser];

    static constraints = {
        filePath nullable:true
        errorFilePath nullable:true
        logFile nullable:true
        logFilePath nullable:true
        endDate nullable:true
        uploadType nullable:true
        notes nullable:true, blank: true, size:0..400
        paramsMapAsText nullable:true, blank: true
    }

    static mapping = {
version : false;
notes type:'text';
paramsMapAsText type:'text';
    }

    static UploadLog create(SUser author, Date startDate, Date endDate, String filePath, String notes=null, String uploadType=null, params, Status status = Status.SCHEDULED) {

        def paramsMapAsText = getTextFromMap(params);

        UploadLog sbu = new UploadLog (author:author, filePath:filePath, startDate:startDate, endDate:endDate, status:status, notes:notes, uploadType:uploadType, paramsMapAsText:paramsMapAsText);

        if(!sbu.save(flush:true)) {
            sbu.errors.allErrors.each { log.error it; }
            return null;
        }else{
            log.debug "Created roll back hook ${sbu}";
            return sbu;
        }
    }

    private static String getTextFromMap(params){
        Map newMap = new HashMap(params)

        newMap.remove("action")
        newMap.remove("controller")
        newMap.remove("max")
        newMap.remove("offset")
        newMap.remove("filterUrl")
        newMap.remove("notes")
        newMap.remove("uploadType")
        newMap.remove("format")

        return newMap as JSON
    }

    def fetchMapFromText(){
        return JSON.parse(paramsMapAsText)
    }

    def updateStatus(Status status){
        //refresh()

        this.status = status;

        if((status == Status.ABORTED) ||(status == Status.FAILED) || (status == Status.UPLOADED)){
            this.endDate = new Date()
            //updateSpeciesCount()
        }

        if(!this.save(flush:true)){
            this.errors.allErrors.each { log.error it }
        }
    }

    def writeLog(String content, Level level = Level.DEBUG) {
        if(!logFilePath){
            String contentRootDir = Holders.config.speciesPortal.content.rootDir;
            String tmpFileName = (new File(filePath)).getName()+".log";
            logFile = utilsService.createFile(tmpFileName, uploadType, contentRootDir)
            logFilePath = logFile.getAbsolutePath();
            if(!this.save(flush:true)){ 
                this.errors.allErrors.each { log.error it }
            }
            println "----------------------- logFile path " + logFilePath
        }

        def ln = System.getProperty('line.separator');
        logFile << "$ln${level.toString()} : $content";
        switch(level) { 
            case Level.INFO : 
            log.info content;
            break;
            case Level.WARN :
            log.warn content;
            break;
            case Level.ERROR :
            log.error content;
            break;
            default : 
            log.debug content;
        }
    }
}
