package species.utils

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

class Utils {

	private static final log = LogFactory.getLog(this);

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
		header.replace("http://", "");
		return header;
	}
}
