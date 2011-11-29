package species

/**
 * http://creativecommons.org/licenses/publicdomain/
http://creativecommons.org/licenses/by/3.0/
http://creativecommons.org/licenses/by-nc/3.0/
http://creativecommons.org/licenses/by-sa/3.0/
http://creativecommons.org/licenses/by-nc-sa/3.0/  
 * @author sravanthi
 *
 */
class License {
	
	public enum LicenseType {
		CC_PUBLIC_DOMAIN("Public Domain"),
		CC_BY("BY"), 
		CC_BY_NC("BY-NC"), 
		CC_BY_SA("BY-SA"),
		CC_BY_NC_SA("BY-NC-SA"),
		CC_BY_NC_ND("BY-NC-ND");
		
		private String value;
		
		LicenseType(String value) {
			this.value = value;
		}
		
		String value() {
			return this.value;
		}
		
		public String toString() {
			return this.value();
		}
	}
	
	LicenseType name;
	URL url;
	
    static constraints = {
		
    }
	
	static mapping = {
		version false;
	}
}

