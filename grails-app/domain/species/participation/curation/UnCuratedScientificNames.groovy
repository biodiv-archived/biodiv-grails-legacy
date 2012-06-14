package species.participation.curation

import species.participation.Recommendation

/**
 * 
 * @author sandeept
 * This will hold mapping of SN to CN. Scientific name may or may not be standard one. * 
 */

class UnCuratedScientificNames {
	
	String name;
	Recommendation reco;
	boolean isAuthenticated = false;
	//on every reference will be incremented. if its 0 then row will be deleted
	int referanceCounter = 0;
	
	static hasMany = [commonNames:UnCuratedCommonNames]
	
	static constraints = {
		name(blank:false, nullable:false, unique:['reco']);
		reco(nullable:false);
	}
	
	static mapping = {
		version false;
	}
	//duplicate check will be done on reco
}
