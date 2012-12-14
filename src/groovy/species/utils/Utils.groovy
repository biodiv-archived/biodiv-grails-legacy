package species.utils

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import species.auth.SUser;
import species.NamesParser;

class Utils {

	private static final log = LogFactory.getLog(this);
	private static final NamesParser namesParser = new NamesParser();

	def grailsApplication;

	static boolean copy(File src, File dst) throws IOException {
		try {
			InputStream inS = new FileInputStream(src);
			OutputStream outS = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = inS.read(buf)) > 0) {
				outS.write(buf, 0, len);
			}
			inS.close();
			outS.close();
			return true;
		} catch(FileNotFoundException e) {
			log.error "ERROR : "+e.getMessage();
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * TODO: cleaning names.
	 * 1) html cleanup 
	 * 2) replacing . , ; with space. 
	 * 3) Padding - ( ) [ ] { } : & ? * × with space on either sides
	 * 4) Transform "and" and "et" to ampersand (&)
	 * 5) Lowercase including unicode case conversion
	 * 6) trim all extra spaces
	 * @param name
	 * @return
	 */
	static String cleanName(String name) {
		return name?.replaceAll(/<.*?>/, '').replaceAll("\u00A0|\u2007|\u202F", " ").replaceAll("\\n","").replaceAll("\\s+", " ").trim();
	}

	static String cleanFileName(String name) {
		name = name?.replaceAll("\u00A0|\u2007|\u202F", " ").replaceAll("\\s+", "_").trim();
		return name;
	}

	static String cleanSearchQuery(String name) {
		name = cleanName(name);
		name = name.replaceAll("[^\\x20-\\x7e]", "");	//removing all non ascii characters
		return name;
	}

	static String getCanonicalForm(String name){
		def taxonDef = namesParser.parse([name])?.get(0)
		if(taxonDef){
			return taxonDef.canonicalForm ?:taxonDef.name
		}
		return cleanName(name)
	}

	static void populateHttpServletRequestParams(ServletRequest request, Map params) {
		try {
			if (ServletFileUpload.isMultipartContent(request)) {
				//TODO
				/*
				 FileItemFactory factory = new DiskFileItemFactory();
				 ServletFileUpload upload = new ServletFileUpload(factory);
				 Iterator items = upload.parseRequest(request).iterator();
				 while (items.hasNext()) {
				 FileItem thisItem = (FileItem) items.next();
				 if (thisItem.isFormField()) {
				 params[thisItem.getFieldName()] = thisItem.getString();
				 }
				 }*/
				//				println request.multiFileMap;
				//				CommonsMultipartResolver res = new CommonsMultipartResolver();
				//				request = res.resolveMultipart(request);
				//				request.getParameterMap().each { fieldName, files ->
				//					if (files.size() == 1) {
				//						params.put(fieldName, files.first())
				//					} else {
				//						params.put(fieldName, files)
				//					}
				//				}
			} else {
				request.getParameterNames().each {
					params[it] = request.getParameter(it)
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}

	static String getDomain(HttpServletRequest httpServletRequest) {
		// maybe we are behind a proxy
		String header = httpServletRequest.getHeader("X-Forwarded-Host");
		if(header != null) {
			// we are only interested in the first header entry
			header = new StringTokenizer(header,",").nextToken().trim();
		}
		if(header == null) {
			header = httpServletRequest.getHeader("Host");
		}

		if(header.startsWith("http://")) {
			header = header.replace("http://", "");
		}

		if(header.startsWith("www.")) {
			header = header.replace("www.", "");
		}

		header = header.replace(":8080", "");
		return header;
	}

	static String getDomainServerUrl(HttpServletRequest request) {
		def domain = getDomain(request);
		return "$request.scheme://$domain$request.contextPath";
	}
	
//	static String getIBPServerUrl() {
//		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config;
//		return getIBPServerDomain() + "/$config.appName";
//	}
	
	static String getIBPServerDomain() {
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config;
		return "http://$config.ibp.domain";
	}


	static String getDomainName(HttpServletRequest request) {
		def domain = getDomain(request);
		if(domain.startsWith("thewesternghats.in")) {
			return "The Westernghats Portal"
		} else {
			return "India Biodiversity Portal"
		}
		return "";
	}

	static boolean isURL(String str) {
		String defaultUrlPrefix = "http://";
		UrlValidator urlValidator = new UrlValidator();
		return urlValidator.isValid(str) || urlValidator.isValid(defaultUrlPrefix + str);
	}

	static List getUsersList(String userIdsAndEmailIds) {
		List result = [];
		def emailValidator = EmailValidator.getInstance()
		userIdsAndEmailIds.trim().split(",").each{
			String candidateEmail = it.trim();
			if(candidateEmail) {
				if(candidateEmail.isNumber()){
					result.add(SUser.get(candidateEmail.toLong()));
				} else {
					if(emailValidator.isValid(candidateEmail)) {
						if(SUser.findByEmail(candidateEmail)){
							log.debug "Found a user with the email address ${candidateEmail}"
							result.add(SUser.findByEmail(candidateEmail));
						} else {
							result.add(candidateEmail);
						}
					} else {
						log.error "Not a valid email address ${candidateEmail}"
					}
				}
			}
		}
		return result;
	}

}

