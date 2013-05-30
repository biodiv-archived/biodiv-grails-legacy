package speciespage

import java.util.List;

import java.util.List

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList

import org.apache.solr.common.util.DateUtil;
import org.apache.solr.common.util.NamedList;

import org.apache.commons.logging.LogFactory
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.hibernate.exception.ConstraintViolationException;

import species.Contributor;
import species.Field
import species.Resource;
import species.Species
import species.SpeciesField;
import species.TaxonomyDefinition;
import species.formatReader.SpreadsheetReader
import species.sourcehandler.KeyStoneDataConverter
import species.sourcehandler.MappedSpreadsheetConverter
import species.sourcehandler.NewSpreadsheetConverter
import species.sourcehandler.NewSimpleSpreadsheetConverter
import species.sourcehandler.SourceConverter;
import species.sourcehandler.SpreadsheetConverter
import species.sourcehandler.XMLConverter
import species.utils.Utils;
import java.text.SimpleDateFormat;
import species.sourcehandler.exporter.DwCAExporter
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.FileAppender;
import species.participation.DownloadLog;

class SpeciesService {

	private static log = LogFactory.getLog(this);

	static transactional = false

	def grailsApplication;
	def groupHandlerService;
	def namesLoaderService;
	def sessionFactory;
	def externalLinksService;
	def speciesSearchService;
	def namesIndexerService;
	def observationService;
	def springSecurityService

	static int BATCH_SIZE = 10;
	int noOfFields = Field.count();

	def nameTerms(params) {
		List result = new ArrayList();

		def queryResponse = speciesSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Species Pages"]);
		}
		return result;
	}

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

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String lastRevisedStartDate = '';
		String lastRevisedEndDate = '';
		if(params.daterangepicker_start) {
			Date s = DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']);
			Calendar cal = Calendar.getInstance(); // locale-specific
			cal.setTime(s)
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.MINUTE, 0);
			s = new Date(cal.getTimeInMillis())
			//StringWriter str1 = new StringWriter();
			lastRevisedStartDate = dateFormatter.format(s)
			//DateUtil.formatDate(s, cal, str1)
			//println str1
			//lastRevisedStartDate = str1;

		}

		if(params.daterangepicker_end) {
			Calendar cal = Calendar.getInstance(); // locale-specific
			Date e = DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']);
			cal.setTime(e)
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.MINUTE, 59);
			e = new Date(cal.getTimeInMillis())
			//			StringWriter str2 = new StringWriter();
			//			DateUtil.formatDate(e, cal, str2)
			//			println str2
			lastRevisedEndDate = dateFormatter.format(e);
		}

		if(lastRevisedStartDate && lastRevisedEndDate) {
			if(i > 0) aq += " AND";
			aq += " lastrevised:["+lastRevisedStartDate+" TO "+lastRevisedEndDate+"]";
			queryParams['daterangepicker_start'] = params.daterangepicker_start;
			queryParams['daterangepicker_end'] = params.daterangepicker_end;
			activeFilters['daterangepicker_start'] = params.daterangepicker_start;
			activeFilters['daterangepicker_end'] = params.daterangepicker_end;
		} else if(lastRevisedStartDate) {
			if(i > 0) aq += " AND";
			//String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
			aq += " lastrevised:["+lastRevisedStartDate+" TO NOW]";
			queryParams['daterangepicker_start'] = params.daterangepicker_start;
			activeFilters['daterangepicker_start'] = params.daterangepicker_endparams.daterangepicker_end;
		} else if (lastRevisedEndDate) {
			if(i > 0) aq += " AND";
			//String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
			aq += " lastrevised:[ * "+lastRevisedEndDate+"]";
			queryParams['daterangepicker_end'] = params.daterangepicker_end;
			activeFilters['daterangepicker_end'] = params.daterangepicker_end;
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

		//		if(params.habitat && (params.habitat != Habitat.findByName(grailsApplication.config.speciesPortal.group.ALL).id)){
		//			paramsList.add('fq', searchFieldsConfig.HABITAT+":"+params.habitat);
		//			queryParams["habitat"] = params.habitat
		//			activeFilters["habitat"] = params.habitat
		//		}
		//		if(params.tag) {
		//			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
		//			queryParams["tag"] = params.tag
		//			queryParams["tagType"] = 'species'
		//			activeFilters["tag"] = params.tag
		//		}
		//		if(params.user){
		//			paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
		//			queryParams["user"] = params.user.toLong()
		//			activeFilters["user"] = params.user.toLong()
		//		}
		if(params.name) {
			paramsList.add('fq', searchFieldsConfig.NAME+":"+params.name);
			queryParams["name"] = params.name
			activeFilters["name"] = params.name
		}

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
		log.debug "Along with faceting params : "+paramsList;
		try {
			def queryResponse = speciesSearchService.search(paramsList);
			List<Species> speciesInstanceList = new ArrayList<Species>();
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				def speciesInstance = Species.get(doc.getFieldValue("id"));
				if(speciesInstance)
					speciesInstanceList.add(speciesInstance);
			}

			//queryParams = queryResponse.responseHeader.params
			result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), speciesInstanceList:speciesInstanceList, snippets:queryResponse.getHighlighting()]
			return result;
		} catch(SolrException e) {
			e.printStackTrace();
		}

		result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:0, speciesInstanceList:[]];
		return result;
	}

	private boolean isValidSortParam(String sortParam) {
		if(sortParam.equalsIgnoreCase("score") || sortParam.equalsIgnoreCase('lastrevised'))
			return true;
		return false;
	}


	def updateContributor(long contributorId, long speciesFieldId, def value, String type) {
		if(!value) {
			return [success:false, msg:"Field content cannot be empty"]
		}

		Contributor oldContrib = Contributor.read(contributorId);
		if(!oldContrib) {
			return [success:false, msg:"${type.capitalize()} with id ${contributorId} is not found"]
		} else if(oldContrib.name == value) {
			return [success:true, msg:"Nothing to change"]
		}

		SpeciesField speciesField = SpeciesField.get(speciesFieldId);
		if(!speciesField) {
			return [success:false, msg:"SpeciesFeild with id ${speciesFieldId} is not found"]
		}

		SpeciesField.withTransaction { status ->
			Contributor c = (new XMLConverter()).getContributorByName(value, true);
			if(!c) {
				return [success:false, msg:"Error while updating ${type}"]
			} else {
				if(type == 'contributor') {
					speciesField.removeFromContributors(oldContrib);
					speciesField.addToContributors(c);
				} else if (type == 'attributor') {
					speciesField.removeFromAttributors(oldContrib);
					speciesField.addToAttributors(c);
				}
				if(!speciesField.save()) {
					speciesField.errors.each { log.error it }
					return [success:false, msg:"Error while updating ${type}"]
				}
				return [success:true, msg:""]
			}
		}
	}

	def updateDescription(long id, def value) {
		if(!value) {
			return [success:false, msg:"Field content cannot be empty"]
		}

		SpeciesField c = SpeciesField.get(id)
		if(!c) {
			return [success:false, msg:"SpeciesField with id ${id} is not found"]
		} else {
			SpeciesField.withTransaction {
				c.description = value.trim()
				if (!c.save()) {
					c.errors.each { log.error it }
					return [success:false, msg:"Error while updating species field name"]
				}
			}
			return [success:true, msg:""]
		}
	}

	private def createImagesXML(params) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();
		def resources = builder.createNode("resources");
		Node images = new Node(resources, "images");
		List files = [];
		List titles = [];
		List licenses = [];
		params.each { key, val ->
			int index = -1;
			if(key.startsWith('file_')) {
				index = Integer.parseInt(key.substring(key.lastIndexOf('_')+1));

			}
			if(index != -1) {
				files.add(val);
				titles.add(params.get('title_'+index));
				licenses.add(params.get('license_'+index));
			}
		}
		files.eachWithIndex { file, key ->
			Node image = new Node(images, "image");
			if(file) {
				File f = new File(uploadDir, file);
				new Node(image, "fileName", f.absolutePath);
				//new Node(image, "source", imageData.get("source"));
				new Node(image, "caption", titles.getAt(key));
				new Node(image, "contributor", params.author.username);
				new Node(image, "license", licenses.getAt(key));
			} else {
				log.warn("No reference key for image : "+key);
			}
		}
		return resources;
	}

	private def createVideoXML(params) {
		NodeBuilder builder = NodeBuilder.newInstance();
		XMLConverter converter = new XMLConverter();
		def resources = builder.createNode("resources");
		Node videos = new Node(resources, "videos");

		Node video = new Node(videos, "video");
		new Node(video, 'fileName', 'video')
		new Node(video, "source", params.video);
		new Node(video, "caption", params.description);
		new Node(video, "contributor", springSecurityService.currentUser.name);
		new Node(video, "attributor", params.attributor);
		new Node(video, "license", "CC BY");

		return resources;
	}

	/**
	 * 
	 */
	private List<Resource> saveResources(Node resourcesXML, String relImagesContext) {
		XMLConverter converter = new XMLConverter();
		converter.setResourcesRootDir(grailsApplication.config.speciesPortal.resources.rootDir);
		return converter.createMedia(resourcesXML, relImagesContext);
	}

	///////////////////////////////////////////////////////////////////////
	/////////////////////////////// Export ////////////////////////////////
	///////////////////////////////////////////////////////////////////////


	def requestExport(params){
		log.debug(params)
		log.debug "creating species download request"
		DownloadLog.createLog(springSecurityService.currentUser, params.filterUrl, params.downloadType, params.notes, params.source, params)
	}



	def export(params, dl){
		log.debug(params)
		def speciesInstanceList = getSpeciesList(params, dl)
		log.debug " Species total $speciesInstanceList.size "
		return exportSpeciesData(speciesInstanceList, null, dl.type)
	}



	private getSpeciesList(params, dl){
		String action = new URL(dl.filterUrl).getPath().split("/")[2]
		//getting result from solr
		def speciesInstanceList = search(params).speciesInstanceList

		return speciesInstanceList
	}
	
	/**
	 * export species data
	 */
	def exportSpeciesData(String directory) {
		DwCAExporter.getInstance().exportSpeciesData(directory)
	}

	/**
	 * export species data
	 */
	def exportSpeciesData(List<Species> species, String directory, String archiveType) {
		DwCAExporter.getInstance().exportSpeciesData(species, directory)
	}

}
