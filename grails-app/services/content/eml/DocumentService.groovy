package content.eml

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import content.eml.Document
import species.groups.SpeciesGroup;
import species.Habitat

import org.springframework.transaction.annotation.Transactional;
import grails.util.GrailsNameUtils
import org.apache.solr.common.SolrException;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.solr.common.util.NamedList;

import species.participation.Observation;
import species.utils.Utils;
import species.License

import species.sourcehandler.XMLConverter


class DocumentService {

	static transactional = false
	def userGroupService;
	def documentSearchService
	def grailsApplication
	def userGroupsService


	Document createDocument(params) {

		
		def document = new Document()
		updateDocument(document, params)
		
		return document
	}

	
	def updateDocument(document, params) {
		
		document.properties = params
		document.coverage.location = 'POINT(' + params.coverage.longitude + ' ' + params.coverage.latitude + ')'
		document.coverage.reverseGeocodedName = params.coverage.reverse_geocoded_name
		document.coverage.locationAccuracy = params.coverage.location_accuracy
		
		
		
		document.license  = (new XMLConverter()).getLicenseByType(params.licenseName, false)
		//document.license = License.findByName(License.LicenseType(params.licenseName))
			
		if(params.description) {
			def description = params.description.trim()
			if(description)
				document.description =description
			else
				document.description = null
		}
		
		if(params.contributors) {
			def contributors = params.contributors.trim()
			if(contributors)
				document.contributors =contributors
			else
				document.contributors = null
		}
		
		if(params.attribution) {
			def attribution = params.attribution.trim()
			if(attribution)
				document.attribution = attribution
			else
				document.attribution = null
		}
					

		document.coverage.speciesGroups = []
		params.speciesGroup.each {key, value ->
			log.debug "Value: "+ value
			document.coverage.addToSpeciesGroups(SpeciesGroup.read(value.toLong()));
		}

		document.coverage.habitats  = []
		params.habitat.each {key, value ->
			document.coverage.addToHabitats(Habitat.read(value.toLong()));
		}
	}
	
	
	def setUserGroups(Document documentInstance, List userGroupIds) {
		if(!documentInstance) return

			def docInUserGroups = documentInstance.userGroups.collect { it.id + ""}
		println docInUserGroups;
		def toRemainInUserGroups =  docInUserGroups.intersect(userGroupIds);
		if(userGroupIds.size() == 0) {
			println 'removing document from usergroups'
			userGroupService.removeDocumentFromUserGroups(documentInstance, docInUserGroups);
		} else {
			userGroupIds.removeAll(toRemainInUserGroups)
			userGroupService.postDocumenttoUserGroups(documentInstance, userGroupIds);
			docInUserGroups.removeAll(toRemainInUserGroups)
			userGroupService.removeDocumentFromUserGroups(documentInstance, docInUserGroups);
		}
	}


	/**
	 * Update Documents with the values set in form. Document uploader component allows creation of multiple documents 
	 * in a form.
	 * This method is generic and updates the Documents included in any parent 
	 * object. The parent object can have multiple document objects.  	 
	 * 
	 * @param params
	 * @return List
	 */
	def updateDocuments(params) {

		def docs = []
		def docsList = (params.docs != null) ? Arrays.asList(params.docs) : new ArrayList()
		
		for(docId in docsList) {
			def documentInstance = Document.get(docId)
				
				if(params."${docId}.title") {
					documentInstance.title = params."${docId}.title"
				}

				if(params."${docId}.description") {
					documentInstance.description = params."${docId}.description"
				}

				if(params."${docId}.contributors") {
					documentInstance.contributors = params."${docId}.contributors"
				}

				if(params."${docId}.attribution") {
					documentInstance.attribution = params."${docId}.attribution"
				}

				if(params."${docId}.license") {
					documentInstance.license = params."${docId}.license"
				}
				
				if(params."${docId}.licenseName") {
					documentInstance.license  = (new XMLConverter()).getLicenseByType(params."${docId}.licenseName", false)
				}


				if(params."${docId}.tags") {
					def tags = (params."${docId}.tags" != null) ? Arrays.asList(params."${docId}.tags") : new ArrayList();
					documentInstance.setTags(tags);
				}
				
				if(params."${docId}.deleted") {
				//	TODO: Actual delete should be handled by parent form controllers.
					documentInstance.deleted = (params."${docId}.deleted").toBoolean()
				}

				if(params."sourceHolderId") {
					documentInstance.sourceHolderId = params.sourceHolderId
				}

				if(params."sourceHolderType") {
					documentInstance.sourceHolderType = params.sourceHolderType
				}


				if (documentInstance.save(flush: true)) {
					//flash.message = "${message(code: 'default.created.message', args: [message(code: 'UFile.label', default: 'UFile'), uFileInstance.id])}"
					log.info "documentInstance saved" + documentInstance.dump()
				}
				else {
					flash.message = "${message(code: 'error')}";
					documentInstance.errors.allErrors.each { log.error it }
					def errorMsg = "Errors in saving documentInstance"
					throw new GrailsTagException(errorMsg)
				}
	
			log.info "Document properties updated from form: "+ documentInstance.dump()
			docs.add(documentInstance)
		}

		return docs
	}


	/////SEARCH//////

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
		
		//filters
		if(params.tag) {
			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Document.class)
			activeFilters["tag"] = params.tag
		}
		
		if(params.uGroup) {
			if(params.uGroup == "THIS_GROUP") {
				String uGroup = params.webaddress
				if(uGroup) {
					paramsList.add('fq', searchFieldsConfig.USER_GROUP_WEBADDRESS+":"+uGroup);
				}
				queryParams["uGroup"] = params.uGroup
				activeFilters["uGroup"] = params.uGroup
			} else {
				queryParams["uGroup"] = "ALL"
				activeFilters["uGroup"] = "ALL"
			}
		}
		
		log.debug "Along with faceting params : "+paramsList;
		try {
			def queryResponse = documentSearchService.search(paramsList);
			List<Document> documentInstanceList = new ArrayList<Document>();
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				log.debug "doc : "+ doc
				def documentInstance = Document.get(doc.getFieldValue("id"));
				if(documentInstance)
					documentInstanceList.add(documentInstance);
			}
			
			result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), documentInstanceList:documentInstanceList, snippets:queryResponse.getHighlighting()]
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


	def nameTerms(params) {
		List result = new ArrayList();

		def queryResponse = documentSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Documents"]);
		}
		return result;
	}


	/**
	 * Handle the filtering on Documetns
	 * @param params
	 * @param max
	 * @param offset
	 * @return
	 */
	Map getFilteredDocuments(params, max, offset) {
		if(!params.aq){
			return getDocsFromDB(params, max, offset)
		}
		//returning docs from solr search
		return search(params)
	}
	
	private getDocsFromDB(params, max, offset){
		def queryParts = getDocumentsFilterQuery(params)
		String query = queryParts.query;


		query += queryParts.filterQuery + queryParts.orderByClause
		if(max != -1)
			queryParts.queryParams["max"] = max
		if(offset != -1)
			queryParts.queryParams["offset"] = offset


		log.debug "Document Query "+ query + "  params " + queryParts.queryParams
		def documentInstanceList = Document.executeQuery(query, queryParts.queryParams)

		return [documentInstanceList:documentInstanceList, queryParams:queryParts.queryParams, activeFilters:queryParts.activeFilters]
	}

	/**
	 * Prepare database wuery based on paramaters
	 * @param params
	 * @return
	 */
	def getDocumentsFilterQuery(params) {

		def query = "select document from Document document "
		def queryParams = [:]
		def activeFilters = [:]
		def filterQuery = "where document.id is not NULL "  //Dummy stmt

		if(params.tag){
			query = "select document from Document document,  TagLink tagLink "
			filterQuery += " and document.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "
			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Document.class)
			activeFilters["tag"] = params.tag
		}
		
		if(params.webaddress) {
			def userGroupInstance = userGroupService.get(params.webaddress)
			if(userGroupInstance){
				queryParams['userGroup'] = userGroupInstance
				//queryParams['isDeleted'] = false;
		
				query += " join document.userGroups userGroup "
				filterQuery += " and userGroup=:userGroup "
			}
		}
		
		
//
//		if(params.keywords) {
//			query = "select document from Document document,  TagLink tagLink "
//			//TODO - contains
//			filterQuery += " and document.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :keywords "
//			queryParams["keywords"] = params.tag
//			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Document.class)
//			activeFilters["keywords"] = params.tag
//		}
//
//
//		if(params.title) {
//			filterQuery += " and document.title like :title "
//			queryParams["title"] = '%'+params.title + '%'
//			activeFilters["title"] = params.title
//		}
//
//		if(params.description) {
//			filterQuery += " and document.description like :description "
//			queryParams["description"] = '%'+ params.description+'%'
//			activeFilters["description"] = params.description
//		}

		def sortBy = params.sort ? params.sort : "dateCreated "
		def orderByClause = " order by document." + sortBy +  " desc, document.id asc"
		return [query:query,filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]
	}





}
