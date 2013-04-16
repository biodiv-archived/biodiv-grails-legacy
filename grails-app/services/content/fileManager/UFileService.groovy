package content.fileManager

import java.util.Map;

import org.apache.commons.io.FileUtils;
import grails.util.GrailsNameUtils

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
class UFileService {

	static transactional = true

	/**
	 * Update UFiles with the values set in form.
	 *  
	 * @param params
	 * @return List
	 */
	def updateUFiles(params) {
		log.info "Updating UFiles from params: "+ params

		def uFiles = []
		def filesList = (params.files != null) ? Arrays.asList(params.files) : new ArrayList()
		for(fileId in filesList) {
			def uFileInstance = UFile.get(fileId)
			if(!params."${fileId}.deleted") {
				if(params."${fileId}.name") {
					uFileInstance.name = params."${fileId}.name"
				}

				if(params."${fileId}.description") {
					uFileInstance.description = params."${fileId}.description"
				}

				if(params."${fileId}.contributors") {
					uFileInstance.contributors = params."${fileId}.contributors"
				}

				if(params."${fileId}.attribution") {
					uFileInstance.attribution = params."${fileId}.attribution"
				}

				if(params."${fileId}.license") {
					uFileInstance.license = params."${fileId}.license"
				}

				if(params."${fileId}.tags") {
					def tags = (params."${fileId}.tags" != null) ? Arrays.asList(params."${fileId}.tags") : new ArrayList();
					uFileInstance.setTags(tags);
				}

				if(params."sourceHolderId") {
					uFileInstance.sourceHolderId = params.sourceHolderId
				}

				if(params."sourceHolderType") {
					uFileInstance.sourceHolderType = params.sourceHolderType
				}


				if (uFileInstance.save(flush: true)) {
					//flash.message = "${message(code: 'default.created.message', args: [message(code: 'UFile.label', default: 'UFile'), uFileInstance.id])}"
					log.info "ufile saved" + uFileInstance.dump()
				}
				else {
					flash.message = "${message(code: 'error')}";
					uFileInstance.errors.allErrors.each { log.error it }
					def errorMsg = "Errors in saving files"
					throw new GrailsTagException(errorMsg)
				}
			} else {
				//TODO: Actual delete should be handled by parent form controllers. 
				uFileInstance.deleted = params."${fileId}.deleted"
				
			}
			uFiles.add(uFileInstance)
		}

		return uFiles
	}


	public static String getFileSize(File file) {
		return FileUtils.byteCountToDisplaySize(file.length());
	}
	
	
	/**
	 * Handle the filtering on uFiles
	 * @param params
	 * @param max
	 * @param offset
	 * @return
	 */
	Map getFilteredUFiles(params, max, offset) {

		def queryParts = getUFilesFilterQuery(params)
		String query = queryParts.query;


		query += queryParts.filterQuery + queryParts.orderByClause
		if(max != -1)
			queryParts.queryParams["max"] = max
		if(offset != -1)
			queryParts.queryParams["offset"] = offset


		log.debug "UFile Query >>>>>>>>>"+ query + " >>>>>params " + queryParts.queryParams
		def UFileInstanceList = UFile.executeQuery(query, queryParts.queryParams)
		
		return [UFileInstanceList:UFileInstanceList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
	}

	/**
	 * Prepare database wuery based on paramaters
	 * @param params
	 * @return
	 */
	def getUFilesFilterQuery(params) {

		def query = "select ufile from UFile ufile "
		def queryParams = [:]
		def activeFilters = [:]
		def filterQuery = "where ufile.id is not NULL "  //Dummy stmt


		if(params.tag){
			query = "select ufile from UFile ufile,  TagLink tagLink "
			//TODO -
			filterQuery += " and ufile.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "
			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(UFile.class)
			activeFilters["tag"] = params.tag
		}

		if(params.keywords) {
			query = "select ufile from UFile ufile,  TagLink tagLink "
			//TODO - contains
			filterQuery += " and ufile.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :keywords "
			queryParams["keywords"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(UFile.class)
			activeFilters["keywords"] = params.tag
		}
		

		if(params.title) {
			filterQuery += " and ufile.name like :title "
			queryParams["title"] = '%'+params.title + '%'
			activeFilters["title"] = params.title
		}
		
		if(params.description) {
			filterQuery += " and ufile.description like :description "
			queryParams["description"] = '%'+ params.description+'%'
			activeFilters["description"] = params.description
		}
		
		

		def sortBy = params.sort ? params.sort : "dateCreated "

		def orderByClause = " order by ufile." + sortBy +  " desc, ufile.id asc"

		return [query:query,filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]

	}


}
