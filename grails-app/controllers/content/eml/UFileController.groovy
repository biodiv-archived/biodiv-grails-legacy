package content.eml

import grails.converters.JSON

import org.grails.taggable.Tag

import java.io.File;
import java.io.InputStream;
import java.util.List
import java.util.ArrayList;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.io.FileUtils;
import java.net.URL;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.DataFormatter;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import grails.plugin.springsecurity.SpringSecurityUtils;
import grails.converters.JSON
import static org.codehaus.groovy.grails.commons.ConfigurationHolder.config as Config
import org.springframework.http.HttpStatus
import uk.co.desirableobjects.ajaxuploader.exception.FileUploadException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest
import uk.co.desirableobjects.ajaxuploader.AjaxUploaderService
import grails.util.GrailsNameUtils
import grails.plugin.springsecurity.annotation.Secured
import species.formatReader.SpreadsheetReader;
import species.formatReader.SpreadsheetWriter;
import speciespage.ObservationService
import species.utils.Utils
import content.eml.Document
import content.eml.Document.DocumentType
import content.eml.UFile;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import species.License
import species.License.LicenseType

class UFileController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def utilsService
    def springSecurityService;
    def grailsApplication
    def speciesUploadService;
    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
    def messageSource
	String contentRootDir = config.speciesPortal.content.rootDir
    
    static String outputCSVFile = "output.csv" 
    static String columnSep = SpreadsheetWriter.COLUMN_SEP
    static String keyValueSep = SpreadsheetWriter.KEYVALUE_SEP
    static String fieldSep = SpreadsheetWriter.FIELD_SEP
    static String headerSheetName = "headerMetadata"

	AjaxUploaderService ajaxUploaderService
	UFileService uFileService = new UFileService()

	def index = {
		redirect(action: "list", params: params)
	}



	def browser = {
		log.debug params

		def model = getUFileList(params)
		render (view:"browser", model:model)
		return;
	}


	/**
	 *  For uploading a file.
	 *  File is uploaded to a temporary location. No UFile object is created in this method
	 *  params takes the relative path. if not given, uploads to default content root directory
	 */

	@Secured(['ROLE_USER'])
	def fileUpload() {
		try {

			//IE handling: for IE qqfile sends the whole file
			String originalFilename = ""
			if (params.qqfile instanceof org.springframework.web.multipart.commons.CommonsMultipartFile){
				//content = params.qqfile.getBytes()
				originalFilename = params.qqfile.originalFilename
			}
			else{
				//content = request.inputStream.getBytes()
				originalFilename = params.qqfile
			}
			File uploaded = utilsService.createFile(originalFilename, params.uploadDir,contentRootDir)
			InputStream inputStream = selectInputStream(request)

			ajaxUploaderService.upload(inputStream, uploaded)

			String relPath = uploaded.absolutePath.replace(contentRootDir, "")
			//def url = uGroup.createLink(uri:uploaded.getPath() , 'userGroup':params.userGroupInstance, 'userGroupWebaddress':params.webaddress)
			def url = g.createLinkTo(base:config.speciesPortal.content.serverURL, file: relPath)
            String fileExt = fileExtension(originalFilename);
            def res;
            def xlsxFileUrl = null;
            def isSimpleSheet;
            //Conversion of excel to csv 
            //FIND out a proper method to detect excel
            def headerMetadata;
            if(params.fileConvert == "true" && (fileExt == "xls" || fileExt == "xlsx") ) {
                xlsxFileUrl = url;
                if(params.fromChecklist == "false") {
                    headerMetadata = getHeaderMetaDataInFormat(uploaded);
                    //println "======HEADER METADATA READ FROM FILE ===== " + headerMetadata;
                }
                res = convertExcelToCSV(uploaded, params)
                if(res != null) {
                    isSimpleSheet = res.get("isSimpleSheet")
                    relPath = res.get("relPath")
                    url = res.get("url")
                    File temp = uploaded
                    uploaded = res.get("outCSVFile")
                    if(params.fromChecklist == "true"){
                        temp.delete();
                    }
                }
            }
            log.debug "uploaded " + uploaded.absolutePath + " rel path " + relPath + " URL " + url
            //log.debug "url for uploaded file >>>>>>>>>>>>>>>>>>>>>>>>"+ url
			return render(text: [success:true, filePath:relPath, fileURL: url, fileSize:UFileService.getFileSize(uploaded), xlsxFileUrl: xlsxFileUrl, headerMetadata: headerMetadata, isSimpleSheet: isSimpleSheet ] as JSON, contentType:'text/html')
		} catch (FileUploadException e) {

			log.error("Failed to upload file.", e)
			return render(text: [success:false] as JSON, contentType:'text/html')
		}
	}

	/**
	 * upload of file in project.
	 * Document is created after uploading of file. THe document id is passed to form and for further tracking.
	 */
	@Secured(['ROLE_USER'])
	def upload() {
		log.debug params
		try {

			//IE handling: for IE qqfile sends the whole file
			String originalFilename = ""
			if (params.qqfile instanceof org.springframework.web.multipart.commons.CommonsMultipartFile){
				log.debug "Multipart"
				//content = params.qqfile.getBytes()
				originalFilename = params.qqfile.originalFilename
			}
			else{
				log.debug "normal"
				//content = request.inputStream.getBytes()
				originalFilename = params.qqfile
			}
			File uploaded = utilsService.createFile(originalFilename, params.uploadDir, contentRootDir)
			InputStream inputStream = selectInputStream(request)
			//check for file size and file type

			ajaxUploaderService.upload(inputStream, uploaded)


			String relPath = uploaded.absolutePath.replace(contentRootDir, "")

			UFile uFileInstance = new UFile()
			uFileInstance.path = relPath
			uFileInstance.size = UFileService.getFileSize(uploaded)
			uFileInstance.downloads = 0

			Document documentInstance = new Document()
			documentInstance.title  = uploaded.getName()
            documentInstance.language = utilsService.getCurrentLanguage(request);
            //For creating document using default license
            //this will be overwritten later.
			documentInstance.license =  License.findByName(LicenseType.CC_BY);

            if(params.type) {
				switch(params.type) {
					case "Proposal":
						documentInstance.type = DocumentType.Proposal
						break
					case "Report":
						documentInstance.type = DocumentType.Report
						break
					case "Poster":
						documentInstance.type = DocumentType.Poster
						break
					case "Miscellaneous":
					default:
						documentInstance.type = DocumentType.Miscellaneous
						break
				}
			} else {
				documentInstance.type = DocumentType.Miscellaneous
			}
			documentInstance.author = springSecurityService.currentUser

			documentInstance.uFile = uFileInstance

			if(!documentInstance.save(flush:true)) {
                documentInstance.errors.allErrors.each { log.error it }
            }

			log.debug " parameters to projectDoc block >>>> Path - "+ uFileInstance.path + " ,  Id: "+ documentInstance.id + ", fileSize:"+uFileInstance.size+", docName:"+documentInstance.title

			return render(text: [success:true, filePath:relPath, docId:documentInstance.id, fileSize:uFileInstance.size, docName:documentInstance.title] as JSON, contentType:'text/html')
		} catch (FileUploadException e) {

			log.error("Failed to upload file.", e)
			return render(text: [success:false] as JSON, contentType:'text/html')
		}
	}

	private InputStream selectInputStream(HttpServletRequest request) {
		if (request instanceof MultipartHttpServletRequest) {
			MultipartFile uploadedFile = ((MultipartHttpServletRequest) request).getFile('qqfile')
			return uploadedFile.inputStream
		}
		return request.inputStream
	}




    def download = {

        UFile ufile = UFile.get(params.id)
        if (!ufile) {
            def msg = messageSource.getMessage("fileupload.download.nofile", [params.id] as Object[], RCU.getLocale(request))
            log.debug msg
            flash.message = msg
            redirect controller: params.errorController, action: params.errorAction
            return
        }

        def file = new File(ufile.path)
        if (file.exists()) {
            log.debug "Serving file id=[${ufile.id}] for the ${ufile.downloads} to ${request.remoteAddr}"
            ufile.downloads++
            ufile.save()
            response.setContentType("application/octet-stream")
            response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${file.name}")
            response.outputStream << file.readBytes()
            return
        } else {
            def msg = messageSource.getMessage("fileupload.download.filenotfound", [ufile.name] as Object[], RCU.getLocale(request))
            log.error msg
            flash.message = msg
            redirect controller: params.errorController, action: params.errorAction
            return
        }
    }

    def saveModifiedSpeciesFile = {
        //log.debug params
        File file = speciesUploadService.saveModifiedSpeciesFile(params);
        if(file) {
            return render(text: [success:true, downloadFile: file.getAbsolutePath()] as JSON, contentType:'text/html') 
        } else {
            println "======SAVE MODIFIED SPECIES FILE CALLED BUT NO FILE ======= " + params
            return
        }
        /*
        if (f.exists()) {
            println "here here===================="
            //log.debug "Serving file id=[${ufile.id}] for the ${ufile.downloads} to ${request.remoteAddr}"
            response.setContentType("application/octet-stream")
            response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${f.name}")
            response.outputStream << f.readBytes()
            response.outputStream.flush()
            println "==YAHAN HUN == " 
            return render(text: [success:true] as JSON, contentType:'text/html')
        } else {
            println "in else================"
            def msg = messageSource.getMessage("fileupload.download.filenotfound", [ufile.name] as Object[], RCU.getLocale(request))
            log.error msg
            flash.message = msg
            redirect controller: params.errorController, action: params.errorAction
            return
        }
        */
    }

    def downloadSpeciesFile = {
        //println "====FILE NAME =======" + params
        if(params.downloadFile) {
            File f = new File(params.downloadFile);
            if (f.exists()) {
                //println "here here===================="
                //log.debug "Serving file id=[${ufile.id}] for the ${ufile.downloads} to ${request.remoteAddr}"
                response.setContentType("application/octet-stream")
                response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${f.name}")
                response.outputStream << f.readBytes()
                response.outputStream.flush()
                //println "==YAHAN HUN == " 
            }
        } 
    } 

    protected def getUFileList(params) {

        def max = Math.min(params.max ? params.int('max') : 12, 100)
        def offset = params.offset ? params.int('offset') : 0
        def filteredUFile = uFileService.getFilteredUFiles(params, max, offset)
        def UFileInstanceList = filteredUFile.UFileInstanceList
        def queryParams = filteredUFile.queryParams
        def activeFilters = filteredUFile.activeFilters

        def totalUFileInstanceList = uFileService.getFilteredUFiles(params, -1, -1).UFileInstanceList
        def count = totalUFileInstanceList.size()

        return [totalUFileInstanceList:totalUFileInstanceList, UFileInstanceList: UFileInstanceList, UFileInstanceTotal: count, queryParams: queryParams, activeFilters:activeFilters, total:count]

    }

    public Map getHeaderMetaDataInFormat(File uploaded) {
		def completeContent = SpreadsheetReader.readSpreadSheet(uploaded.absolutePath)
        def sheetContent
        def res = [:]
        InputStream inp = new FileInputStream(uploaded);
		Workbook wb = WorkbookFactory.create(inp);
        if(wb.getSheet(headerSheetName)){
            sheetContent = completeContent.get(2)
        }
        else{
            println " ======NO HEADER METADATA=== "
            return res
        }
		sheetContent.each{ sc ->
			String s1,s2
			if(sc["category"] == ""){
				s1 = ""
	 		}
	 		else{
				s1 = "|"
			}
			if(sc["subcategory"] == ""){
				s2 = ""
	 		}
	 		else{
				s2 = "|"
			}
			String dataColumn = sc["concept"] + s1 + sc["category"] + s2 + sc["subcategory"]
			String fieldNames = sc["field name(s)"].toLowerCase()
            String conDel = sc["content delimiter"]
            String conFor = sc["content format"]
            String imagesCol = sc["images"]
            String contributorCol = sc["contributor"]
            String attributionsCol = sc["attributions"]
            String referencesCol = sc["references"]
            String licenseCol = sc["license"]
            String audienceCol = sc["audience"]


            if(fieldNames != ""){
                List fnList = fieldNames.split(fieldSep)
                def cdMap = [:]
                def gMap = [:]
                def hMap = [:]
                def aMap = [:]
                def imgMap = [:]
                def contMap = [:]
                def attrMap = [:]
                def refMap = [:]
                def licMap = [:]
                def audMap = [:]
                if(conDel != ""){
                    //println conDel
                    List conDelList = conDel.split(columnSep)
                    //println "===CDLIST = " + conDelList
                    conDelList.each { cdl ->
                        def z = cdl.split(keyValueSep)
                        if(z.size()==2){
                            cdMap[z[0]] = z[1]
                        }
                        else{
                            cdMap[z[0]] = ""
                        }
                    }
                }
                if(imagesCol != ""){
                    List imgList = imagesCol.split(columnSep)
                    imgList.each { il ->
                        def z = il.split(keyValueSep)
                        if(z.size()==2){
                            imgMap[z[0]] = z[1]
                        }
                        else{
                            imgMap[z[0]] = ""
                        }
                    }
                }
                if(contributorCol != ""){
                    List contList = contributorCol.split(columnSep)
                    contList.each { cl ->
                        def z = cl.split(keyValueSep)
                        if(z.size()==2){
                            contMap[z[0]] = z[1]
                        }
                        else{
                            contMap[z[0]] = ""
                        }
                    }
                }
                if(attributionsCol != ""){
                    List attrList = attributionsCol.split(columnSep)
                    attrList.each { al ->
                        def z = al.split(keyValueSep)
                        if(z.size()==2){
                            attrMap[z[0]] = z[1]
                        }
                        else{
                            attrMap[z[0]] = ""
                        }
                    }
                }
                if(referencesCol != ""){
                    List refList = referencesCol.split(columnSep)
                    refList.each { rl ->
                        def z = rl.split(keyValueSep)
                        if(z.size()==2){
                            refMap[z[0]] = z[1]
                        }
                        else{
                            refMap[z[0]] = ""
                        }
                    }
                }
                if(licenseCol != ""){
                    List licList = licenseCol.split(columnSep)
                    licList.each { ll ->
                        def z = ll.split(keyValueSep)
                        if(z.size()==2){
                            licMap[z[0]] = z[1]
                        }
                        else{
                            licMap[z[0]] = ""
                        }
                    }
                }
                if(audienceCol != ""){
                    List audList = audienceCol.split(columnSep)
                    audList.each { al ->
                        def z = al.split(keyValueSep)
                        if(z.size()==2){
                            audMap[z[0]] = z[1]
                        }
                        else{
                            audMap[z[0]] = ""
                        }
                    }
                }


                if(conFor != ""){
                    List conForList = conFor.split(columnSep)
                    conForList.each { cfl ->
                        def z = cfl.split(keyValueSep)
                        def q = z[1].split(";")
                        if(q[0].split("=").size() == 2){
                            gMap[z[0]] = q[0].split("=")[1]
                        }else{
                            gMap[z[0]] = "" 
                        }
                        if(q[1].split("=").size() == 2){

                            hMap[z[0]] = q[1].split("=")[1]	
                        }
                        else{
                            hMap[z[0]] = ""
                        }
                        if(q[2].split("=").size() == 2){

                            aMap[z[0]] = q[2].split("=")[1]	
                        }
                        else{
                            aMap[z[0]] = ""
                        }
                    }
                }
                fnList.each{ fn ->
                    fn = fn.trim()
                    def val = res[fn]
                    if(val){
                        val["dataColumns"] = val["dataColumns"] + "," + dataColumn
                    }
                    else{
                        def m =[:]
                        m["dataColumns"] = dataColumn
                        m["delimiter"] = cdMap[fn]
                        m["group"] = gMap[fn]
                        m["header"] = hMap[fn]
                        m["append"] = aMap[fn]
                        m["images"] = imgMap[fn]
                        m["contributor"] = contMap[fn]
                        m["attributions"] = attrMap[fn]
                        m["references"] = refMap[fn]
                        m["license"] = licMap[fn]
                        m["audience"] = audMap[fn]
                        res[fn] = m
                    }
                }
            }
        }
        return res
	}

    public Map getHeaderMetaData(File uploaded) {
        def completeContent = SpreadsheetReader.readSpreadSheet(uploaded.absolutePath)
        def sheetContent
        int numAttributes = 5
        def res = [:]
        if(completeContent.size() == 3 ){
            sheetContent = completeContent.get(2)
            //println "==SHEET CONTENT ======== " + sheetContent
        }
        else{
            //println " ======NO HEADER METADATA=== "
            return res
        }
        int contentSize = sheetContent.size();
        int index = 0;
        while(index < contentSize){
            String mapKey = sheetContent.get(index).get("column_name");
            def mapValue = [:]
            //println "=====MAP KEY === " + mapKey
            for(int k = index; k < (index + numAttributes) ; k++){
                mapValue[sheetContent.get(k).get("type")] = sheetContent.get(k).get("value")
            }
            //println "======MAP VALUE ==== " + mapValue
            index = index + numAttributes
            res.put(mapKey, mapValue)
            //println "=======RES IN MIDDLE ==== " + res
        }
        //println "===RESULT ======= " + res
        return res
    }

    private Map convertExcelToCSV(File uploaded, params ) {
        def compContent
        def spread
        File outCSVFile = utilsService.createFile(outputCSVFile, params.uploadDir,contentRootDir)
        boolean isSimpleSheet = detectSheetType(uploaded)
        FileWriter fw = new FileWriter(outCSVFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        if(isSimpleSheet == false){
            compContent = SpreadsheetReader.readSpreadSheet(uploaded.absolutePath)
            spread = compContent.get(0)
            def headerNameList = spread.get(0).collect {
                StringEscapeUtils.escapeCsv(it.getKey());
            }
            def  joinedHeader = headerNameList.join(",")
            bw.write(joinedHeader + "\r\r\n\n")

            spread.each { rowMap->  
                List rowValues = []
                rowMap.each{
                    rowValues << StringEscapeUtils.escapeCsv(it.getValue());
                }
                def joinedContent = rowValues.join(",")
                bw.write(joinedContent + "\r\r\n\n")
            }
        }else{
            compContent = SpreadsheetReader.readSpreadSheet(uploaded.absolutePath, 0)
            def conceptRow = compContent.get(0)
            def categoryRow = compContent.get(1)
            def subcategoryRow = compContent.get(2)
            def headerRow = []
            def index = 0
            conceptRow.each{
                def hName
                if(index != 0){
                    hName = conceptRow.get(index).toLowerCase()
                    def val1 = categoryRow.get(index).toLowerCase()
                    if(val1 != ""){
                        hName = hName + "|" + val1
                    }
                    def val2 = subcategoryRow.get(index).toLowerCase()
                    if(val2 != ""){
                        hName = hName + "|" + val2
                    }
                    headerRow << StringEscapeUtils.escapeCsv(hName)

                }
                index = index + 1
            }
            def  joinedHeader = headerRow.join(",")
            bw.write(joinedHeader + "\r\r\n\n")
            def counter = 0
            compContent.each{ stringRow ->
                if(counter >= 4){
                    List rowValues = []
                    def k = 0;
                    stringRow.each{
                        if(k != 0){
                            rowValues << StringEscapeUtils.escapeCsv(it);
                        }
                        k++;
                    }
                    def joinedContent = rowValues.join(",")
                    bw.write(joinedContent + "\r\r\n\n")
                }
                counter = counter + 1
            }
            
        }

        bw.close();
        String relPath = outCSVFile.absolutePath.replace(contentRootDir, "")
        def url = g.createLinkTo(base:config.speciesPortal.content.serverURL, file: relPath)
        Map res = new HashMap();
        res.put("outCSVFile" , outCSVFile)
        res.put("relPath" , relPath)
        res.put("url" , url)
        res.put("isSimpleSheet", isSimpleSheet)
        return res
    }

    private boolean detectSheetType(File uploaded){
        def compContent = SpreadsheetReader.readSpreadSheet(uploaded.absolutePath)
        boolean isSimpleSheet = true
        InputStream inp = new FileInputStream(uploaded);
		Workbook wb = WorkbookFactory.create(inp);
        if(wb.getSheet(headerSheetName)){
            isSimpleSheet = false
            return isSimpleSheet
		}
        else{
            def spread = SpreadsheetReader.readSpreadSheet(uploaded.absolutePath, 0)
            if(spread.get(0).get(0).toLowerCase() == "concept" && spread.get(1).get(0).toLowerCase() == "category" && spread.get(2).get(0).toLowerCase() == "subcategory" && spread.get(3).get(0).toLowerCase() == "description"){
                isSimpleSheet = true
                return isSimpleSheet
            }
            else{
                isSimpleSheet = false
                return isSimpleSheet
            }
        }

    }

    private String fileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        return extension.toLowerCase();
    }

    private String sanitizeForCsv(String cellData) {
        if (cellData == ""){
            return "";
        }
        StringBuilder resultBuilder = new StringBuilder(cellData);

        // Look for doublequotes, escape as necessary.
        int lastIndex = 0;
        while (resultBuilder.indexOf("\"", lastIndex) >= 0) {
            int quoteIndex = resultBuilder.indexOf("\"", lastIndex);
            resultBuilder.replace(quoteIndex, quoteIndex + 1, "\"\"");
            lastIndex = quoteIndex + 2;
        }
        ///println "=========== " + cellData;
        char firstChar = cellData.charAt(0);
        char lastChar = cellData.charAt(cellData.length() - 1);

        if (cellData.contains(",") || // Check for commas
                cellData.contains("\n") ||  // Check for line breaks
                Character.isWhitespace(firstChar) || // Check for leading whitespace.
                Character.isWhitespace(lastChar)) { // Check for trailing whitespace
            resultBuilder.insert(0, "\"").append("\""); // Wrap in doublequotes.
                }
        return resultBuilder.toString();
    }
}
