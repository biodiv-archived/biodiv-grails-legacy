package species.utils

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.LogFactory;

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
}
