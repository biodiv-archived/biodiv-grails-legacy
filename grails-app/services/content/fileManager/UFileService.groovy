package content.fileManager

import java.util.Map;

import org.apache.commons.io.FileUtils;
import grails.util.GrailsNameUtils
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.solr.common.SolrException;


import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
class UFileService {

	static transactional = true
	
	def UFileSearchService
	

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

	
	
	
	def nameTerms(params) {
		List result = new ArrayList();

		def queryResponse = UFileSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Project Pages"]);
		}
		return result;
	}

	
	/**
	 *
	 * @param params
	 * @return
	 */
	def search(params) {
		def result;
		def searchFieldsConfig = grailsApplication.config.speciesPortal.searchFields
		def queryParams = [:]
		def activeFilters = [:]

		NamedList paramsList = new NamedList();
		queryParams["query"] = params.query
		activeFilters["query"] = params.query
		params.query = params.query ?: "";

		String aq = "";
		int i=0;
		if(params.aq instanceof GrailsParameterMap) {
			println '-----------------'
			params.aq.each { key, value ->
				queryParams["aq."+key] = value;
				activeFilters["aq."+key] = value;
				if(!(key ==~ /action|controller|sort|fl|start|rows|webaddress/) && value ) {
					if(i++ == 0) {
						aq = key + ': ('+value+')';
					} else {
						aq = aq + " AND " + key + ': ('+value+')';
					}
				}
			}
		}
		if(params.query && aq) {
			params.query = params.query + " AND "+aq
		} else if (aq) {
			params.query = aq;
		}

		def offset = params.offset ? params.long('offset') : 0

		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		paramsList.add('start', offset);
		def max = Math.min(params.max ? params.int('max') : 12, 100)
		paramsList.add('rows', max);
		params['sort'] = params['sort']?:"score"
		String sort = params['sort'].toLowerCase();
		if(isValidSortParam(sort)) {
			if(sort.indexOf(' desc') == -1 && sort.indexOf(' asc') == -1 ) {
				sort += " desc";
			}
			paramsList.add('sort', sort);
		}
		queryParams["max"] = max
		queryParams["offset"] = offset

		paramsList.add('fl', params['fl']?:"id");
/*
		if(params.sGroup) {
			params.sGroup = params.sGroup.toLong()
			def groupId = observationService.getSpeciesGroupIds(params.sGroup)
			if(!groupId){
				log.debug("No groups for id " + params.sGroup)
			} else{
				paramsList.add('fq', searchFieldsConfig.SGROUP+":"+groupId);
				queryParams["groupId"] = groupId
				activeFilters["sGroup"] = groupId
			}
		}
*/
		if(params.name) {
			paramsList.add('fq', searchFieldsConfig.NAME+":"+params.name);
			queryParams["name"] = params.name
			activeFilters["name"] = params.name
		}
/*
		if(params.uGroup) {
			if(params.uGroup == "THIS_GROUP") {
				String uGroup = params.webaddress
				if(uGroup) {
					//AS we dont have selecting species for group ... we are ignoring this filter
					//paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
				}
				queryParams["uGroup"] = params.uGroup
				activeFilters["uGroup"] = params.uGroup
			} else {
				queryParams["uGroup"] = "ALL"
				activeFilters["uGroup"] = "ALL"
			}
		}

		if(params.query && params.startsWith && params.startsWith != "A-Z"){
			params.query = params.query + " AND "+searchFieldsConfig.TITLE+":"+params.startsWith+"*"
			//paramsList.add('fq', searchFieldsConfig.TITLE+":"+params.startsWith+"*");
			queryParams["startsWith"] = params.startsWith
			activeFilters["startsWith"] = params.startsWith
		}
		*/
		log.debug "Along with faceting params : "+paramsList;
		try {
			def queryResponse = UFileSearchService.search(paramsList);
			List<UFile> projectInstanceList = new ArrayList<UFile>();
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				log.debug "doc : "+ doc
				def projectInstance = Project.get(doc.getFieldValue("id"));
				if(projectInstance)
					projectInstanceList.add(projectInstance);
			}

			result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), projectInstanceList:projectInstanceList, snippets:queryResponse.getHighlighting()]
			log.debug "result returned from search: "+ result
			return result;
		} catch(SolrException e) {
			e.printStackTrace();
		}

		result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:0, speciesInstanceList:[]];
		return result;
	}
	
	private boolean isValidSortParam(String sortParam) {
		if(sortParam.equalsIgnoreCase('dateCreated'))
			return true;
		return false;
	}



}
