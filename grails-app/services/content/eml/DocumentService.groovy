package content.eml

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.grails.tagcloud.TagCloudUtil
import groovy.sql.Sql

import content.eml.Document
import species.groups.SpeciesGroup;
import species.Habitat

import org.springframework.transaction.annotation.Transactional;
import grails.util.GrailsNameUtils
import org.apache.solr.common.SolrException;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.solr.common.util.NamedList;

import species.participation.Observation;
import content.eml.Document.DocumentType;
import species.utils.Utils;
import species.License
import species.License.LicenseType
import content.Project

import species.sourcehandler.XMLConverter
import species.AbstractObjectService;

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import static speciespage.ObvUtilService.*;
import java.nio.file.Files;
import species.formatReader.SpreadsheetReader;
import species.auth.SUser;
import species.groups.UserGroup;
import static java.nio.file.StandardCopyOption.*
import java.nio.file.Paths;
import species.participation.Discussion;

class DocumentService extends AbstractObjectService {

	static transactional = false
	
	private static final int BATCH_SIZE = 1
	
	def documentSearchService
	def userGroupService
    def sessionFactory
	def activityFeedService
	
	Document createDocument(params) {


		def document = new Document()
		updateDocument(document, params)

		return document
	}


	def updateDocument(document, params) {
		params.remove('latitude')
		params.remove('longitude')
		document.properties = params
		document.group = null
		document.habitat = null
		//document.latitude = document.latitude ?:0.0
		//document.longitude = document.longitude ?:0.0
		document.placeName = params.placeName
		document.reverseGeocodedName = params.reverse_geocoded_name
		document.locationAccuracy = params.location_accuracy
		document.language = params.locale_language 
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
		if(params.areas) {
			WKTReader wkt = new WKTReader(geometryFactory);
			try {
				Geometry geom = wkt.read(params.areas);
				document.topology = geom;
			} catch(ParseException e) {
				log.error "Error parsing polygon wkt : ${params.areas}"
			}
		}

        if(params.licenseName) 
    		document.license  = (new XMLConverter()).getLicenseByType(params.licenseName, false)
		    //document.license = License.findByName(License.LicenseType(params.licenseName))
        else {
		    document.license =  License.findByName(LicenseType.CC_BY)
        }

		document.notes = params.description? params.description.trim() :null
		document.contributors = params.contributors? params.contributors.trim() :null
		document.attribution = params.attribution? params.attribution.trim() :null
		
		document.speciesGroups = []
		params.speciesGroup.each {key, value ->
			log.debug "Value: "+ value
			document.addToSpeciesGroups(SpeciesGroup.read(value.toLong()));
		}

		document.habitats  = []
		params.habitat.each {key, value ->
			document.addToHabitats(Habitat.read(value.toLong()));
		}
	}


	def setUserGroups(Document documentInstance, List userGroupIds, boolean sendMail = true) {
		if(!documentInstance) return

		def docInUserGroups = documentInstance.userGroups.collect { it.id + ""}
		def toRemainInUserGroups =  docInUserGroups.intersect(userGroupIds);
		if(userGroupIds.size() == 0) {
			log.debug 'removing document from usergroups'
			userGroupService.removeDocumentFromUserGroups(documentInstance, docInUserGroups, sendMail);
		} else {
			userGroupIds.removeAll(toRemainInUserGroups)
			userGroupService.postDocumenttoUserGroups(documentInstance, userGroupIds, sendMail);
			docInUserGroups.removeAll(toRemainInUserGroups)
			userGroupService.removeDocumentFromUserGroups(documentInstance, docInUserGroups, sendMail);
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
            //TODO - UI sending null id
            if(!documentInstance)continue;

			if(params."${docId}.title") {
				documentInstance.title = params."${docId}.title"
			}

			if(params."${docId}.description") {
				documentInstance.notes = params."${docId}.description"
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

			//set usergroup from parent
			
			if(params.userGroup) {
				
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
		if(params.aq instanceof GrailsParameterMap  || params.aq instanceof Map){
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

		def offset = params.offset ? params.offset.toLong().longValue() : 0

		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		paramsList.add('start', offset);
		def max = Math.min(params.max ? params.max.toInteger().intValue() : 12, 100)
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
            if(queryResponse) {
			List<Document> documentInstanceList = new ArrayList<Document>();
                Iterator iter = queryResponse.getResults().listIterator();
                while(iter.hasNext()) {
                    def doc = iter.next();
                    Long id = (doc.getFieldValue("id").tokenize("_")[1]).toLong()
                    def documentInstance = Document.get(id);
                    if(documentInstance)
                        documentInstanceList.add(documentInstance);
                }
                
                result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), documentInstanceList:documentInstanceList, snippets:queryResponse.getHighlighting()]
            }
			log.debug "result returned from search: "+ result
			return result;
		} catch(SolrException e) {
			e.printStackTrace();
		}
		
		result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:0, speciesInstanceList:[]];
		return result;
	}

	private boolean isValidSortParam(String sortParam) {
		if(sortParam.equalsIgnoreCase('createdOn'))
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
		def res = [canPullResource:userGroupService.getResourcePullPermission(params)]
		if(Utils.isSearchAction(params)){
			//returning docs from solr search
			res.putAll(search(params))
		}else{
			res.putAll(getDocsFromDB(params, max, offset))
		}
		return res
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

        def hqlQuery = sessionFactory.currentSession.createQuery(query)
        /*if(params.bounds && boundGeometry) {
            hqlQuery.setParameter("boundGeometry", boundGeometry, new org.hibernate.type.CustomType(org.hibernatespatial.GeometryUserType, null))
        }*/ 

            if(max > -1){
                hqlQuery.setMaxResults(max);
                queryParts.queryParams["max"] = max
            }
            if(offset > -1) {
                hqlQuery.setFirstResult(offset);
                queryParts.queryParams["offset"] = offset
            }

        hqlQuery.setProperties(queryParts.queryParams);
		def documentInstanceList = hqlQuery.list();

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
        def userGroup = utilsService.getUserGroup(params);
 
        if(params.featureBy == "true"){
			query = "select document from Document document "
		 	if(!userGroup) {
                filterQuery += " and document.featureCount > 0 "                
            }
             else {
                query += ", Featured feat "
                filterQuery += " and document.id = feat.objectId and feat.objectType =:featType and feat.userGroup.id = :userGroupId "
                queryParams["userGroupId"] = userGroup?.id

            }
            //params.userGroup = utilsService.getUserGroup(params);
            // if(params.userGroup == null) {
                //filterQuery += "and feat.userGroup is null"
            //}
            //else {
                //filterQuery += "and feat.userGroup.id =:userGroupId"
              //  queryParams["userGroupId"] = params.userGroup?.id
            //}
            queryParams["featureBy"] = params.featureBy
            queryParams["featType"] = Document.class.getCanonicalName();
            activeFilters["featureBy"] = params.featureBy
		}

		if(params.tag){
			query = "select document from Document document,  TagLink tagLink "
			filterQuery += " and document.id = tagLink.tagRef and tagLink.type = :tagType and tagLink.tag.name = :tag "
			queryParams["tag"] = params.tag
			queryParams["tagType"] = GrailsNameUtils.getPropertyName(Document.class)
			activeFilters["tag"] = params.tag
		}
		
		if(userGroup) {
			//def userGroupInstance = userGroupService.get(params.webaddress)
			//if(userGroupInstance){
				queryParams['userGroup'] = userGroup
				//queryParams['isDeleted'] = false;
		
				query += " join document.userGroups userGroup "
				filterQuery += " and userGroup=:userGroup "
			//}
		}
		
		def sortBy = params.sort ? params.sort : "lastRevised "
		def orderByClause = " order by document." + sortBy +  " desc, document.id asc"
		return [query:query,filterQuery:filterQuery, orderByClause:orderByClause, queryParams:queryParams, activeFilters:activeFilters]
	}

	
	def getFilteredTagsByUserGroup(groupWebAddress, tagType){
		def tags = [:]
		def userGroupInstance = groupWebAddress ? userGroupService.get(groupWebAddress) : null
		switch(tagType){
			case GrailsNameUtils.getPropertyName(Document.class).toLowerCase():
				if(!userGroupInstance){
					tags = TagCloudUtil.tags(Document)
				}else{
					tags = getTags(userGroupInstance.documents?.collect{it.id}, tagType)
				}
				break
			case GrailsNameUtils.getPropertyName(Project.class).toLowerCase():
				if(!userGroupInstance){
					tags = TagCloudUtil.tags(Project)
				}else{
					tags = getTags(userGroupInstance.projects?.collect{it.id}, tagType)
				}
				break
			
			case GrailsNameUtils.getPropertyName(Discussion.class).toLowerCase():
				if(!userGroupInstance){
					tags = TagCloudUtil.tags(Discussion)
				}else{
					tags = getTags(userGroupInstance.discussions?.collect{it.id}, tagType)
				}
				break

			default:
				break
		}
		
		return tags
	}
	
	private Map getTags(ids, tagType){
		int tagsLimit = 1000;
		LinkedHashMap tags = [:]
		if(!ids){
			return tags
		}

		def sql =  Sql.newInstance(dataSource);
		String query = "select t.name as name, count(t.name) as obv_count from tag_links as tl, tags as t, " + tagType + " obv where tl.tag_ref in " + getSqlInCluase(ids)  + " and  tl.type = '" + tagType +"' and t.id = tl.tag_id group by t.name order by count(t.name) desc, t.name asc limit " + tagsLimit;

		sql.rows(query, ids).each{
			tags[it.getProperty("name")] = it.getProperty("obv_count");
		};
		return tags;
	}
	
	private String getSqlInCluase(list){
		return "(" + list.collect {'?'}.join(", ") + ")"
	}

	
	///////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// BULK UPLOAD//////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	
	def processBatch(params){
		File fileDir = new File(params.fileDir)
		if(!fileDir.exists()){
			log.error "File dir not found. Aborting upload"
			return
		}
		
		File spreadSheet = new File(params.batchFileName)
		if(!spreadSheet.exists()){
			log.error "Main batch file not found. Aborting upload"
			return
		}
		
		def resultObv = []
		int i = 0;
		SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).each{ m ->
			log.debug "================" + m
			
			if(m['file path'].trim() != "" || m['uri'].trim() != '' ){
				uploadDoc(fileDir, m, resultObv)
				i++
				if(i > BATCH_SIZE){
					utilsService.cleanUpGorm(true)
					def obvs = resultObv.collect { Document.read(it) }
					try{
						documentSearchService.publishSearchIndex(obvs, true);
					}catch (Exception e) {
						log.error e.printStackTrace();
					}
					resultObv.clear();
					i = 0;
				}
			}
		}
	}
	
	private uploadDoc(fileDir, Map m, resultObv){
		Document document = new Document()
		
		//setting ufile and uri
		File sourceFile = new File(fileDir, m['file path'])
		if(sourceFile.exists()){
			def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
			String contentRootDir = config.speciesPortal.content.rootDir
			File destinationFile = utilsService.createFile(sourceFile.getName(), 'documents',contentRootDir)
			try{
				Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(destinationFile.getAbsolutePath()), REPLACE_EXISTING);
				UFile f = new UFile()
				f.size = destinationFile.length()
				f.path = destinationFile.getAbsolutePath().replaceFirst(contentRootDir, "")
				document.uFile = f
				log.debug "============== " + f.path
			}catch(e){
				e.printStackTrace()
			}
		}
		
		document.uri = m['uri']
		document.title = m['title']
		
		if(!document.title){
			log.error 'title cant be null'
			return 
		}
		
		if(!document.uFile  && !document.uri){
			log.error "Either ufile or uri is null so ignoring this document"
			return
		}
		//other params
		document.author = SUser.findByEmail(m['user email'].trim())
		document.type = Document.fetchDocumentType(m['type'])
		document.license =  License.findByName(License.fetchLicenseType(("cc " + m[LICENSE]).toUpperCase()))
	
        if(!document.license) {
		    document.license =  License.findByName(LicenseType.CC_BY)
        }

		document.contributors =  m['contributors']
		document.attribution =  m['attribution']
		document.notes =  m['description']
		
		document.speciesGroups = []
		m['species groups'].split(",").each {
			def s = SpeciesGroup.findByName(it.trim())
			if(s)
				document.addToSpeciesGroups(s);
		}

		document.habitats  = []
		m['habitat'].split(",").each {
			def h = Habitat.findByName(it.trim())
			document.addToHabitats(h);
		}
		
		document.longitude = (m['longitude'] ?:76.658279)
		document.latitude = (m['lattitude'] ?: 12.32112)
		document.geoPrivacy = m["geoprivacy"]
		
		
//		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), grailsApplication.config.speciesPortal.maps.SRID);
//		if(document.latitude && document.longitude) {
//			document.topology = Utils.GeometryAsWKT(geometryFactory.createPoint(new Coordinate(document.longitude?.toFloat(), document.latitude?.toFloat())));
//		}
//		
		saveDoc(document, m)
		
		if(document.id){
			resultObv << document.id
		}
	}
	
	private saveDoc(documentInstance, Map m) {
		log.debug( "document instance with params assigned >>>>>>>>>>>>>>>>: "+ documentInstance)
		if (documentInstance.save(flush: true) && !documentInstance.hasErrors()) {
			
			activityFeedService.addActivityFeed(documentInstance, null, documentInstance.author, activityFeedService.DOCUMENT_CREATED);
			
			def tags = (m['tags'] && (m['tags'].trim() != "")) ? m['tags'].trim().split(",").collect{it.trim()} : new ArrayList();
			documentInstance.setTags(tags)
			
			def userGroupIds = m['post to user groups'] ?   m['post to user groups'].split(",").collect { UserGroup.findByName(it.trim())?.id } : new ArrayList()
			userGroupIds = userGroupIds.collect { "" + it }
			setUserGroups(documentInstance, userGroupIds);
		}
		else {
			documentInstance.errors.allErrors.each { log.error it }
		}
	}

}
