package content.eml

import java.util.Map;

import org.apache.commons.io.FileUtils;
import grails.util.GrailsNameUtils
import org.apache.solr.common.util.NamedList;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.apache.solr.common.SolrException;
import speciespage.search.DocumentSearchService

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

import content.eml.UFile;
class UFileService {

	static transactional = true
	
	DocumentSearchService uFileSearchService = new DocumentSearchService()
	

	public static String getFileSize(File file) {
		return FileUtils.byteCountToDisplaySize(file.length());
	}




}
