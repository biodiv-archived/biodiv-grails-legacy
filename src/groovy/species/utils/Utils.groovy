package species.utils

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import groovyx.net.http.ContentType;
import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.Method;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import grails.plugin.springsecurity.ReflectionUtils;
import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;

import species.auth.SUser;
import species.NamesParser;
import java.util.regex.Pattern;

import org.apache.commons.lang.WordUtils
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

class Utils {

	private static final log = LogFactory.getLog(this);
	private static final NamesParser namesParser = new NamesParser();
	private static final Random FILE_NAME_GENEROTR = new Random();
	private static final MIN_FLOAT = 0.2;
	private static final MAX_FLOAT = 0.7;
	
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
        if(!name) return null;
		return name?.replaceAll(/<.*?>/, '').replaceAll("\u00A0|\u2007|\u202F", " ").replaceAll("\\n","").replaceAll("\\s+", " ").replaceAll("\\*", "").trim();
	}

    static String cleanSciName(String scientificName) {
        def cleanSciName = Utils.cleanName(scientificName);
        if(cleanSciName =~ /s\.\s*str\./) {
            cleanSciName = cleanSciName.replaceFirst(/s\.\s*str\./, cleanSciName.split()[0]);
        }

        if(cleanSciName.indexOf(' ') == -1) {
            cleanSciName = cleanSciName.toLowerCase().capitalize();
        }
        return cleanSciName;
    }

	static String generateSafeFileName(String name) {
		//returning random integer (between 1-1000) as file name along with original extension
        return "" + (FILE_NAME_GENEROTR.nextInt(1000-1+1)+1) + getCleanFileExtension(name) 
	}

	static String cleanFileName(String name){
		name = name?.replaceAll("\u00A0|\u2007|\u202F", " ").replaceAll("\\s+", "_").trim();
		//if name starting with .
		if(name.startsWith(".")){
			name = name.replaceFirst(".", "_")
		}
		
		int beginIndex = name.lastIndexOf(".")
		name =  (beginIndex > -1) ? name.substring(0, beginIndex) + getCleanFileExtension(name) : name
		return name;
	}
	
	static float getRandomFloat(){
		def f = FILE_NAME_GENEROTR.nextFloat()
		return (f < MIN_FLOAT) ? f + MIN_FLOAT : (f > MAX_FLOAT ? f - MAX_FLOAT + MIN_FLOAT : f)
	}
	  
	/**
	 * @param fileName
	 * @return after validation either return empty string or an extension starting with . and not more than 4 chars. 
	 * if more than 4 chars then return a string starting with _
	 */
	
	public static getCleanFileExtension(String fileName){
		String extension = ""
		fileName = fileName?.trim()
		if(!fileName || fileName == "")
			return extension
		
		int beginIndex = fileName.lastIndexOf(".")
		extension = (beginIndex > -1 && beginIndex+1 != fileName.size()) ? fileName.substring(beginIndex) : ""
        if(extension.size() > 5) extension = "";
		return extension
	}
	
	
	static String cleanSearchQuery(String name) {
		name = name?.replaceAll(/<.*?>/, '').replaceAll("\u00A0|\u2007|\u202F", " ").replaceAll("\\n","").replaceAll("\\s+", " ").trim();
		name = name.replaceAll("[^\\x20-\\x7e]", "");	//removing all non ascii characters
		return name;
	}

	static String getCanonicalForm(String name){
		try {
			def taxonDef = namesParser.parse([name])?.get(0)
			if(taxonDef){
				return taxonDef.canonicalForm ?:taxonDef.name
			}
		} catch (Exception e) {
			log.error e.printStackTrace();
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
		return "$request.scheme://$domain";
	}

	static String getDomainServerUrlWithContext(HttpServletRequest request) {
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

	static String getIBPServerCookieDomain() {
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config;
		return "$config.ibp.domain";
	}


	static String getDomainName(HttpServletRequest request) {
		if(!request){
			return ""
		}
		
		def domain = getDomain(request);
		def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config

		if(domain.startsWith(config.wgp.domain)) {
			return "The Westernghats Portal"
		} else {
			return config.speciesPortal.app.siteName
		}
		return "";
	}

	static boolean isURL(String str) {
		String defaultUrlPrefix = "http://";
		UrlValidator urlValidator = new UrlValidator();
		return urlValidator.isValid(str) || urlValidator.isValid(defaultUrlPrefix + str);
	}

    static boolean isAbsoluteURL(String str) {
        final URI u = new URI("str");
        return u.isAbsolute()
    }

	static List getUsersList(String userIdsAndEmailIds) {
        if(!userIdsAndEmailIds) return [];
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

	static String getTitleCase(String str){
		return WordUtils.capitalizeFully(str, ' (/'.toCharArray())
	}

	public static boolean isAjax(final DefaultSavedRequest savedRequest) {

		String ajaxHeaderName = (String)ReflectionUtils.getConfigProperty("ajaxHeader");
		return !savedRequest.getHeaderValues(ajaxHeaderName).isEmpty();
	}
	
	public static def getPremailer(baseUrl, html) {
		def parsedJSON;
		def http = new HTTPBuilder()
		http.request("http://premailer.dialect.ca/api/0.1/documents", Method.POST, ContentType.JSON) {
			body = [ 'html' : html, 'base_url':baseUrl ]

			response.success = { resp, json ->
				println resp.status
				println json
			}
			response.failure = { resp ->  log.error "Premailer request failed with response $resp" 
				println "Unexpected error: ${resp.status} : ${resp.statusLine.reasonPhrase}"
			}
		}
		return parsedJSON;
	}
	
	public static String getYouTubeVideoId(String url) {
		if(!url) return;
		else {
			String re = /https?:\/\/(?:[0-9A-Z-]+\.)?(?:www\.)?(?:youtu\.be\/|youtube\.com\S*[^\w\-\s])([\w\-]{11})(?=[^\w\-]|$)(?![?=&+%\w]*(?:['"][^<>]*>|<\/a>))[?=&+%\w-]*/;
			def matcher = (url =~ re)
			if(matcher.getCount() > 0) {
				return matcher.getAt(0)[1]
			} else {
				return null;
			}
		}
	}
	
	public static String getYouTubeEmbedUrl(String videoId, int height, int width) {
		return "<iframe width='${width}' height='${height}' src='http://www.youtube.com/embed/${videoId}' frameborder='0' allowfullscreen></iframe>";
	}

    /*
    * http://stackoverflow.com/questions/5830387/how-to-find-all-youtube-video-ids-in-a-string-using-a-regex/5831191#5831191
    https?://           # Required scheme. Either http or https.
    (?:[0-9A-Z-]+\.)?   # Optional subdomain.
    (?:                 # Group host alternatives.
        youtu\.be/      # Either youtu.be,
        | youtube\.com  # or youtube.com followed by
        \S*             # Allow anything up to VIDEO_ID,
        [^\w\-\s]       # but char before ID is non-ID char.
    )                   # End host alternatives.
    ([\w\-]{11})        # $1: VIDEO_ID is exactly 11 chars.
    (?=[^\w\-]|$)       # Assert next char is non-ID or EOS.
    (?!                 # Assert URL is not pre-linked.
        [?=&+%\w]*      # Allow URL (query) remainder.
        (?:             # Group pre-linked alternatives.
            [\'"][^<>]*># Either inside a start tag,
            | </a>      # or inside <a> element text contents.
        )               # End recognized pre-linked alts.
    )                   # End negative lookahead assertion.
    [?=&+%\w-]*         # Consume any URL (query) remainder.
    */
	public static String linkifyYoutubeLink(String text) {
		if(!text) return;
		else {
            //Pattern re = ~/(?ix)https?:\/\/(?:[0-9A-Z-]+\.)?(?:www\.)?(?:youtu\.be\/|youtube\.com\S*[^\w\-\s])([\w\-]{11})(?=[^\w\-]|$)(?![?=&+%\w]*(?:['"][^<>]*>))[?=&+%\w-]*/;          
            Pattern re = ~/(?ix)(?:<a.*>)?(?:https?:\/\/(?:[0-9A-Z-]+\.)?(?:www\.)?(?:youtu\.be\/|youtube\.com\S*[^\w\-\s])([\w\-]{11})(?=[^\w\-]|$))[?=&+%\w-;]*(?:<\/a>)?/;
			text = text.replaceAll(re, {
				//getYouTubeEmbedUrl(it[1], 295,480);
				"""
				<div class="youtube_container">
				
				<div class="preview">
					<span class="videoId" style="display:none;">${it[1]}</span>
				  <img class="thumb" src="http://img.youtube.com/vi/${it[1]}/default.jpg"/>
				  <img class="play" src="https://s-static.ak.fbcdn.net/rsrc.php/v2/yG/r/Gj2ad6O09TZ.png">
				</div>
				<div class="info">
				  
				</div>
				<div class="info-small">
				  www.youtube.com
				</div>
				<div style="clear: both;"></div>
			  </div>
			"""
			})
			
		}
		return text;
	}

    public static stripHTML(String text) {
        if(!text) return text;
        return text.replaceAll("<(.|\n)*?>", '');
    }

    public static GeometryAsWKT(Geometry geom) {
        if(!geom) return;
        WKTWriter wktWriter = new WKTWriter();
        return wktWriter.write(geom);
    }

    public static boolean isInteger(String s, int radix=10) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
	
	public static Map getQueryMap(URL url){
		def map = [:]
		if(!url.query){
			return new GrailsParameterMap(map, null)
		}
		url.query.split('&').each{kv ->
			def (key, value) = kv.split('=').toList()
		    if(value != null) {
				map[key] = URLDecoder.decode(value)
		    }
		}
		
		//converting a.b.c = 10 to a:[b:[c:10]] 
		def retMap = [:]
		map.each{k, v ->
			def arr = k.split("\\.")
			def lookupMap = retMap
			int count = 1
			arr.each { ele ->
				if(lookupMap.get(ele) == null){
					if(count < (arr.length)){
						lookupMap[ele] = [:]
						lookupMap = lookupMap.get(ele)
					}else{
						lookupMap[ele] = v
					}
				}else{
					lookupMap = lookupMap.get(ele)
				}
				count++
			}
		   
		}
		retMap = new GrailsParameterMap(retMap, null)
		return retMap
	}
	
	/**
	 * On browser if you are on list page and doing some advance search result came from search while url still shows 'list' action
	 * this leading to wrong result in ResourceFetcher (i.e download/export post/unpost) this function is take care of all such cases
	 * @param params
	 * @return
	 */
	public static boolean isSearchAction(params, String action = null){
		return ("search".equalsIgnoreCase(action) || "search".equalsIgnoreCase(params.action) ||  params.aq || params.query ) 
	}
	
}


