package species.participation

import java.io.File;
import java.text.SimpleDateFormat
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.springframework.transaction.annotation.Transactional;


import species.Reference;
import species.Species
import species.utils.Utils;
import species.formatReader.SpreadsheetReader;

//pdf related
import au.com.bytecode.opencsv.CSVWriter
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image

import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class ChecklistService {

	static transactional = false

	static final String SN_NAME_KEY = "scientific_name"
	static final String SN_NAME = "scientific_name" //"scientific_names" //"Scientific Name" //"scientific_name"
	static final String CN_NAME = "common_name"
	
	def grailsApplication
	def observationService
	def checklistSearchService;
	def obvUtilService;
	
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Search realted /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	def nameTerms(params) {
		List result = new ArrayList();

		def queryResponse = checklistSearchService.terms(params.term, params.field, params.max);
		NamedList tags = (NamedList) ((NamedList)queryResponse.getResponse().terms)[params.field];
		for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
			Map.Entry tag = (Map.Entry) iterator.next();
			result.add([value:tag.getKey().toString(), label:tag.getKey().toString(),  "category":"Checklists"]);
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
		//		if(params.daterangepicker_start && params.daterangepicker_end) {
		//			if(i > 0) aq += " AND";
		//			String lastRevisedStartDate = dateFormatter.format(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']));
		//			String lastRevisedEndDate = dateFormatter.format(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']));
		//			aq += " fromdate:["+lastRevisedStartDate+" TO *] AND todate:[* TO "+lastRevisedEndDate+"]";
		//			queryParams['daterangepicker_start'] = params.daterangepicker_start;
		//			queryParams['daterangepicker_end'] = params.daterangepicker_end;
		//			activeFilters['daterangepicker_start'] = params.daterangepicker_start;
		//			activeFilters['daterangepicker_end'] = params.daterangepicker_end;
		//
		//		} else if(params.daterangepicker_start) {
		//			if(i > 0) aq += " AND";
		//			String lastRevisedStartDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_start, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
		//			aq += " fromdate:["+lastRevisedStartDate+" TO NOW]";
		//			queryParams['daterangepicker_start'] = params.daterangepicker_start;
		//			activeFilters['daterangepicker_start'] = params.daterangepicker_endparams.daterangepicker_end;
		//		} else if (params.daterangepicker_end) {
		//			if(i > 0) aq += " AND";
		//			String lastRevisedEndDate = dateFormatter.format(DateTools.dateToString(DateUtil.parseDate(params.daterangepicker_end, ['dd/MM/yyyy']), DateTools.Resolution.DAY));
		//			aq += " todate:[NOW TO "+lastRevisedEndDate+"]";
		//			queryParams['daterangepicker_end'] = params.daterangepicker_end;
		//			activeFilters['daterangepicker_end'] = params.daterangepicker_end;
		//		}
		//
		if(params.query && aq) {
			params.query = params.query + " AND "+aq
		} else if (aq) {
			params.query = aq;
		}

		def max = Math.min(params.max ? params.int('max') : 12, 100)
		def offset = params.offset ? params.long('offset') : 0

		paramsList.add('q', Utils.cleanSearchQuery(params.query));
		paramsList.add('start', offset);
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

		if(params.tag) {
			paramsList.add('fq', searchFieldsConfig.TAG+":"+params.tag);
			queryParams["tag"] = params.tag
			queryParams["tagType"] = 'species'
			activeFilters["tag"] = params.tag
		}
		if(params.user){
			paramsList.add('fq', searchFieldsConfig.USER+":"+params.user);
			queryParams["user"] = params.user.toLong()
			activeFilters["user"] = params.user.toLong()
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

		log.debug "Along with faceting params : "+paramsList;
		try {
			def queryResponse = checklistSearchService.search(paramsList);
			List<Species> checklistInstanceList = new ArrayList<Species>();
			Iterator iter = queryResponse.getResults().listIterator();
			while(iter.hasNext()) {
				def doc = iter.next();
				def checklistInstance = Checklist.get(doc.getFieldValue("id"));
				if(checklistInstance)
					checklistInstanceList.add(checklistInstance);
			}

			//queryParams = queryResponse.responseHeader.params
			result = [queryParams:queryParams, activeFilters:activeFilters, instanceTotal:queryResponse.getResults().getNumFound(), checklistInstanceList:checklistInstanceList, snippets:queryResponse.getHighlighting()]
			return result;
		} catch(SolrException e) {
			e.printStackTrace();
		}

		result = [queryParams:queryParams, instanceTotal:0, speciesInstanceList:[]];
		return result;
	}

	private boolean isValidSortParam(String sortParam) {
		if(sortParam.equalsIgnoreCase("score"))
			return true
		else
			return false;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////  Export ////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	def export(params, DownloadLog dlog){
		log.debug(params)
		def cl = Checklist.read(params.downloadObjectId.toLong())
		if(!cl){
			return null
		}

		File downloadDir = new File(grailsApplication.config.speciesPortal.checklist.checklistDownloadDir)
		if(!downloadDir.exists()){
			downloadDir.mkdirs()
		}
		if(dlog.type == DownloadLog.DownloadType.CSV){
			return exportAsCSV(cl, downloadDir)
		}else{
			return exportAsPDF(cl, downloadDir)
		}
	}

	private File exportAsPDF(Checklist cl, downloadDir){
		log.debug "Writing pdf checklist" + cl
		File pdfFile = new File(downloadDir, "checklist_" + new Date().getTime() + ".pdf")
		Document document = new Document()
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile))

		document.open()
		Map m = cl.fetchExportableValue()


		//adding site banner
		Image image2 = Image.getInstance(grailsApplication.config.speciesPortal.app.rootDir + "/sites/all/themes/ibp/images/map-logo.gif");
		//image2.scaleToFit(120f, 120f);
		document.add(image2);

		//writing meta data
		com.itextpdf.text.List list = new com.itextpdf.text.List(10);
		for(item in m[cl.META_DATA]){
			list.add(new ListItem(item.join("  ")));
		}
		document.add(list)

		//writing data
		def columnNames = cl.fetchColumnNames()
		PdfPTable t = new PdfPTable(columnNames.length)
		t.setSpacingBefore(25);
		t.setSpacingAfter(25);

		//writing header
		for(c in columnNames){
			t.addCell(new PdfPCell(new Phrase(c)))
		}
		//t.setHeaderRows(1)
		//writing actual data
		for(item in m[cl.DATA]){
			for(obj in item){
				t.addCell(new PdfPCell(new Phrase(obj?:"")))
			}
		}
		document.add(t)

		document.close()
		return pdfFile
	}

	private File exportAsCSV(Checklist cl, downloadDir){
		File csvFile = new File(downloadDir, "checklist_" + new Date().getTime() + ".csv")
		CSVWriter writer = obvUtilService.getCSVWriter(csvFile.getParent(), csvFile.getName())
		log.debug "Writing csv checklist" + cl

		Map m = cl.fetchExportableValue()

		for(item in m[cl.META_DATA]){
			writer.writeNext(item.toArray(new String[0]))
		}

		writer.writeNext("## Checklist Data:")
		writer.writeNext(cl.fetchColumnNames())
		for(item in m[cl.DATA]){
			writer.writeNext(item.toArray(new String[0]))
		}

		writer.flush()
		writer.close()

		return csvFile
	}
}
