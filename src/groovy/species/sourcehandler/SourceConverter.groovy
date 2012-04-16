package species.sourcehandler

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import species.License.LicenseType;

class SourceConverter {
	protected Map licenseUrlMap;
	private static final log = LogFactory.getLog(this);
	
	protected SourceConverter() {
		licenseUrlMap = new HashMap();
		licenseUrlMap.put(LicenseType.CC_PUBLIC_DOMAIN, "http://creativecommons.org/licenses/publicdomain/");
		licenseUrlMap.put(LicenseType.CC_BY, "http://creativecommons.org/licenses/by/3.0/");
		licenseUrlMap.put(LicenseType.CC_BY_NC, "http://creativecommons.org/licenses/by-nc/3.0/");
		licenseUrlMap.put(LicenseType.CC_BY_ND, "http://creativecommons.org/licenses/by-nd/3.0/");
		licenseUrlMap.put(LicenseType.CC_BY_NC_ND, "http://creativecommons.org/licenses/by-nc-nd/3.0/");
		licenseUrlMap.put(LicenseType.CC_BY_NC_SA, "http://creativecommons.org/licenses/by-nc-sa/3.0/");
		licenseUrlMap.put(LicenseType.CC_BY_SA, "http://creativecommons.org/licenses/by-sa/3.0/ ");
	}

}
