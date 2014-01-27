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

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;

import static org.codehaus.groovy.grails.commons.ConfigurationHolder.config as Config
import org.springframework.http.HttpStatus
import uk.co.desirableobjects.ajaxuploader.exception.FileUploadException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest
import uk.co.desirableobjects.ajaxuploader.AjaxUploaderService
import grails.util.GrailsNameUtils
import grails.plugins.springsecurity.Secured
import species.formatReader.SpreadsheetReader;
import species.formatReader.SpreadsheetWriter;

 
import speciespage.ObservationService
import species.utils.Utils
import content.eml.Document
import content.eml.Document.DocumentType
import content.eml.UFile;

class UFileController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def observationService
	def springSecurityService;
	def grailsApplication

	def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config

	String contentRootDir = config.speciesPortal.content.rootDir
    
    static String outputCSVFile = "output.csv" 

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
	def fileUpload = {
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
			File uploaded = createFile(originalFilename, params.uploadDir)
			InputStream inputStream = selectInputStream(request)

			ajaxUploaderService.upload(inputStream, uploaded)

			String relPath = uploaded.absolutePath.replace(contentRootDir, "")
			//def url = uGroup.createLink(uri:uploaded.getPath() , 'userGroup':params.userGroupInstance, 'userGroupWebaddress':params.webaddress)
			def url = g.createLinkTo(base:config.speciesPortal.content.serverURL, file: relPath)
            String fileExt = fileExtension(originalFilename);
            def res;
            def xlsxFileUrl = null;
            //Conversion of excel to csv 
            //FIND out a proper method to detect excel
            def headerMetadata;
            if(params.fileConvert == "true" && (fileExt == "xls" || fileExt == "xlsx") ) {
                xlsxFileUrl = url;
                if(params.fromChecklist == "false") {
                    headerMetadata = getHeaderMetaData(uploaded);
                    println "======HEADER METADATA READ FROM FILE ===== " + headerMetadata;
                }
                res = convertExcelToCSV(uploaded, params)
                if(res != null) {
                    relPath = res.get("relPath")
                    url = res.get("url")
                    File temp = uploaded
                    uploaded = res.get("outCSVFile")
                    if(params.fromChecklist == "true"){
                        temp.delete();
                    }
                }
            }
            println "uploaded " + uploaded.absolutePath + " rel path " + relPath + " URL " + url
            //log.debug "url for uploaded file >>>>>>>>>>>>>>>>>>>>>>>>"+ url
			return render(text: [success:true, filePath:relPath, fileURL: url, fileSize:UFileService.getFileSize(uploaded), xlsxFileUrl: xlsxFileUrl, headerMetadata: headerMetadata] as JSON, contentType:'text/html')
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
	def upload = {
		log.debug "&&&&&&&&&&&&&&&&&&& <><><<>>params in upload of file" +  params
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
			File uploaded = createFile(originalFilename, params.uploadDir)
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


			documentInstance.save(flush:true)


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

	//Create file with given filename
    private File createFile(String fileName, String uploadDir) {
        File uploaded
        if (uploadDir) {
            File fileDir = new File(contentRootDir + "/"+ uploadDir)
            if(!fileDir.exists())
                fileDir.mkdirs()
                uploaded = observationService.getUniqueFile(fileDir, Utils.generateSafeFileName(fileName));

        } else {

            File fileDir = new File(contentRootDir)
            if(!fileDir.exists())
                fileDir.mkdirs()
                uploaded = observationService.getUniqueFile(fileDir, Utils.generateSafeFileName(fileName));
            //uploaded = File.createTempFile('grails', 'ajaxupload')
        }

        log.debug "New file created : "+ uploaded.getPath()
        return uploaded
    }


    def download = {

        UFile ufile = UFile.get(params.id)
        if (!ufile) {
            def msg = messageSource.getMessage("fileupload.download.nofile", [params.id] as Object[], request.locale)
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
            def msg = messageSource.getMessage("fileupload.download.filenotfound", [ufile.name] as Object[], request.locale)
            log.error msg
            flash.message = msg
            redirect controller: params.errorController, action: params.errorAction
            return
        }
    }

    def saveModifiedSpeciesFile = {
        //log.debug params
        println "======PARAMS ========= " + params.gridData + "----------- " + params.headerMarkers; 
        def gData = JSON.parse(params.gridData)
        def headerMarkers = JSON.parse(params.headerMarkers)
        println "=====AFTER JSON PARSE ======= " + gData + "--------== " + headerMarkers
        String fileName = "speciesSpreadsheet.xlsx"
        String uploadDir = "species"
        //URL url = new URL(data.xlsxFileUrl);
        File file = createFile(fileName , uploadDir);
        //FileUtils.copyURLToFile(url, f);
        println "===NEW MODIFIED SPECIES FILE=== " + file
        String xlsxFileUrl = params.xlsxFileUrl.replace("\"", "").trim();
        println "===XLSX FILE URL ======= " + xlsxFileUrl
        InputStream input = new URL(xlsxFileUrl).openStream();
        SpreadsheetWriter.writeSpreadsheet(file, input, gData, headerMarkers);
        return render(text: [success:true, downloadFile: file.getAbsolutePath()] as JSON, contentType:'text/html')
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
            def msg = messageSource.getMessage("fileupload.download.filenotfound", [ufile.name] as Object[], request.locale)
            log.error msg
            flash.message = msg
            redirect controller: params.errorController, action: params.errorAction
            return
        }
        */
    }

    def downloadSpeciesFile = {
        println "====FILE NAME =======" + params
        File f = new File(params.downloadFile);
        if (f.exists()) {
            println "here here===================="
            //log.debug "Serving file id=[${ufile.id}] for the ${ufile.downloads} to ${request.remoteAddr}"
            response.setContentType("application/octet-stream")
            response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${f.name}")
            response.outputStream << f.readBytes()
            response.outputStream.flush()
            println "==YAHAN HUN == " 
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

    public Map getHeaderMetaData(File uploaded) {
        def completeContent = SpreadsheetReader.readSpreadSheet(uploaded.absolutePath)
        def sheetContent
        int numAttributes = 4
        def res = [:]
        if(completeContent.size() == 3 ){
            sheetContent = completeContent.get(2)
            println "==SHEET CONTENT ======== " + sheetContent
        }
        else{
            println " ======NO HEADER METADATA=== "
            return res
        }
        int contentSize = sheetContent.size();
        int index = 0;
        while(index < contentSize){
            String mapKey = sheetContent.get(index).get("column_name");
            def mapValue = [:]
            println "=====MAP KEY === " + mapKey
            for(int k = index; k < (index + numAttributes) ; k++){
                mapValue[sheetContent.get(k).get("type")] = sheetContent.get(k).get("value")
            }
            println "======MAP VALUE ==== " + mapValue
            index = index + numAttributes
            res.put(mapKey, mapValue)
            println "=======RES IN MIDDLE ==== " + res
        }
        println "===RESULT ======= " + res
        return res
    }


    private Map convertExcelToCSV(File uploaded, params ) {
        def spread = SpreadsheetReader.readSpreadSheet(uploaded.absolutePath).get(0)
        File outCSVFile = createFile(outputCSVFile, params.uploadDir)

        FileWriter fw = new FileWriter(outCSVFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        if(spread.size() != 0){
            int  size = spread.get(0).size()
            int count = 0;
            //println  "============ "  + spread.class + "  ---- " + spread; 
            def headerNameList = spread.get(0).collect {
                StringEscapeUtils.escapeCsv(it.getKey());
            }
            //println "=====ROW HEADER SIZE==== " + headerNameList.size()
            def  joinedHeader = headerNameList.join(",")

            //println "=========JOINED HEADER ===== " + joinedHeader;
            bw.write(joinedHeader + "\n")

            spread.each { rowMap->  
                //println "====SIZE ROW MAP== " + rowMap.size();
                //println "========%%%%%%%%% " + rowMap.class + " --- " + rowMap;
                List rowValues = []
                rowMap.each{
                    rowValues << StringEscapeUtils.escapeCsv(it.getValue());
                    //'"' + it.getValue() + '"';
                    //sanitizeForCsv(it.getValue()) ;
                }
                //println "====ROW VALUES === "+ rowValues;
                def joinedContent = rowValues.join(",")
                //println "=========JOINED CONTENT === " + joinedContent;
                bw.write(joinedContent + "\n")
            }
        }
        bw.close();
        String relPath = outCSVFile.absolutePath.replace(contentRootDir, "")
        def url = g.createLinkTo(base:config.speciesPortal.content.serverURL, file: relPath)
        Map res = new HashMap();
        res.put("outCSVFile" , outCSVFile)
        res.put("relPath" , relPath)
        res.put("url" , url)
        return res


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
