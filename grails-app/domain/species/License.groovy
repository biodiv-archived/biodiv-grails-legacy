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
		CC_BY("CC BY"),
		CC_BY_SA("CC BY-SA"),
		CC_BY_ND("CC BY-ND"),
		CC_BY_NC("CC BY-NC"),		
		CC_BY_NC_SA("CC BY-NC-SA"),
		CC_BY_NC_ND("CC BY-NC-ND");
		//COPYRIGHT("Copyright");

		private String value;

		LicenseType(String value) {
			this.value = value;
		}

		String value() {
			return this.value;
		}

		static def toList() {
			return [
				CC_PUBLIC_DOMAIN,
				CC_BY,
				CC_BY_SA,
				CC_BY_ND,
				CC_BY_NC,				
				CC_BY_NC_SA,
				CC_BY_NC_ND ]
		}

		public String toString() {
			return this.value();
		}
		
		public String getIconFilename(){
			return this.value().replaceAll(/ /, "_").replaceAll('.') { it.toLowerCase()}
		}
	}

	LicenseType name;
	String url;

	static constraints = {
		name (blank:false, unique:true);
		url(nullable:true);
	}

	static mapping = { version false; }
}

