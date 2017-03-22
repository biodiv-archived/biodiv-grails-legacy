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
		CC_BY_NC_ND("CC BY-NC-ND"),
        UNSPECIFIED("Unspecified");
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
				CC_BY_NC_ND,
                UNSPECIFIED]
		}

		public String toString() {
			return this.value();
		}
		
		public String getIconFilename(){
			return this.value().replaceAll(/ /, "_").replaceAll('.') { it.toLowerCase()}
		}

        public String getTooltip() {
            switch(this) {
                case CC_PUBLIC_DOMAIN : return "Public Domain"

        		case CC_BY : return "Attribution"

            	case CC_BY_SA : return "Attribution-ShareAlike"

        		case CC_BY_ND : return "Attribution-NoDerivs"

        		case CC_BY_NC : return "Attribution-NonCommercial"

        		case CC_BY_NC_SA : return "Attribution-NonCommercial-ShareAlike"

        		case CC_BY_NC_ND : return "Attribution-NonCommercial-NoDerivs"

        		case UNSPECIFIED : return "Unspecified"
            }
        }
	}

	LicenseType name;
	String url;

	static constraints = {
		name (blank:false, unique:true);
		url(nullable:true);
	}

	static mapping = { 
        version false; 
        cache usage: 'read-only', include: 'non-lazy'
    }
	
    static LicenseType fetchLicenseType(licenseType){
        if(!licenseType) return null;

        LicenseType type;
        if(licenseType instanceof LicenseType) {
            type = licenseType
        } else {
            licenseType = licenseType?.toString().trim();
            if(!licenseType.startsWith("CC") && !licenseType.equalsIgnoreCase(LicenseType.CC_PUBLIC_DOMAIN.value())) {
                licenseType = "CC "+licenseType.trim()
            }
            if(licenseType.startsWith('CC-')) {
                licenseType = licenseType.replaceFirst('CC-','CC ');
            }

            for(LicenseType l : LicenseType){
                if(l.value().equalsIgnoreCase(licenseType))
                    type = l
            }
        }
        return type
    }

    static List<License> list() { 
        println "License overridden fn for cache"
        return License.createCriteria().list {
            cache true
        }
    }

    static License findByName(LicenseType whatever) { 
        println "License overridden fn for cache"
        return License.createCriteria().get {
            eq 'name', whatever
            cache true
        }
    } 

}

