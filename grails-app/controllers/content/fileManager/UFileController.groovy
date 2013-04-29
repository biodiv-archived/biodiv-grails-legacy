package content.fileManager

import grails.converters.JSON

import org.grails.taggable.Tag

import java.io.File;
import java.io.InputStream;
import java.util.List

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
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
import grails.plugins.springsecurity.Secured


import speciespage.ObservationService
import species.utils.Utils
import content.eml.Document

class UFileController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def observationService
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

	


	// for uploading a file.
	// File is uploaded to a temporary location. No UFile object is created in controller
	@Secured(['ROLE_USER'])
	def fileUpload = {
		log.debug params
		try {

			File uploaded = createFile(params.qqfile, params.uploadDir)
			InputStream inputStream = selectInputStream(request)

			ajaxUploaderService.upload(inputStream, uploaded)


			//def url = uGroup.createLink(uri:uploaded.getPath() , 'userGroup':params.userGroupInstance, 'userGroupWebaddress':params.webaddress)
			def url = [uri:uploaded.getPath()]
			//log.debug "url for uploaded file >>>>>>>>>>>>>>>>>>>>>>>>"+ url

			return render(text: [success:true, filePath:uploaded.getPath(), fileURL: url, fileSize:UFileService.getFileSize(uploaded)] as JSON, contentType:'text/json')
		} catch (FileUploadException e) {

			log.error("Failed to upload file.", e)
			return render(text: [success:false] as JSON, contentType:'text/json')
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

			File uploaded = createFile(params.qqfile, params.uploadDir)
			InputStream inputStream = selectInputStream(request)
			//check for file size and file type

			ajaxUploaderService.upload(inputStream, uploaded)

			UFile uFileInstance = new UFile()
			uFileInstance.path = uploaded.getPath()
			uFileInstance.size = UFileService.getFileSize(uploaded)
			uFileInstance.downloads = 0
			
			Document documentInstance = new Document()
			documentInstance.title  = uploaded.getName()
			
			documentInstance.uFile = uFileInstance
			
			
			documentInstance.save(flush:true)
			

			return render(text: [success:true, filePath:uFileInstance.path, docId:documentInstance.id, fileSize:uFileInstance.size, docName:documentInstance.title] as JSON, contentType:'text/json')
		} catch (FileUploadException e) {

			log.error("Failed to upload file.", e)
			return render(text: [success:false] as JSON, contentType:'text/json')
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
			File fileDir = new File(uploadDir)
			if(!fileDir.exists())
				fileDir.mkdir()
			uploaded = observationService.getUniqueFile(fileDir, Utils.cleanFileName(fileName));

		} else {

			File fileDir = new File(grailsApplication.config.speciesPortal.content.fileUploadDir)
			if(!fileDir.exists())
				fileDir.mkdir()
			uploaded = observationService.getUniqueFile(fileDir, Utils.cleanFileName(fileName));
			//uploaded = File.createTempFile('grails', 'ajaxupload')
		}
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

	def tagcloud = { render (view:"tagcloud") }



	/**
	 *
	 */
	def search = {
		log.debug params;
		def model = uFileService.search(params)
		model['isSearch'] = true;

		if(params.loadMore?.toBoolean()){
			params.remove('isGalleryUpdate');
			render(template:"/UFile/searchResultsTemplate", model:model);
			return;

		} else {
			params.remove('isGalleryUpdate');
			def obvListHtml =  g.render(template:"/UFile/searchResultsTemplate", model:model);
			model.resultType = "ufile"
			def obvFilterMsgHtml = g.render(template:"/common/observation/showObservationFilterMsgTemplate", model:model);

			def result = [obvListHtml:obvListHtml, obvFilterMsgHtml:obvFilterMsgHtml]

			render (result as JSON)
			return;
		}
	}

	def terms = {
		log.debug params;
		params.field = params.field?params.field.replace('aq.',''):"autocomplete";
		List result = uFileService.nameTerms(params)
		render result.value as JSON;
	}



}
