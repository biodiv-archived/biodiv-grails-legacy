package species.dataset;

import species.License;

class Access {
	
	String rights;
    Long licenseId;

	static constraints = {
		rights nullable:true;
	}
	
	static mapping = {
		rights type:'text'
	}
   /* 
    License getLicense() {
        return License.get(this.licenseId);
    }

    void setLicense(License license) {
        this.licenseId = license.id;
    }
*/
}

