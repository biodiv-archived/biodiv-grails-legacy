package species.participation.curation

import species.Language
import species.participation.Recommendation

class UnCuratedCommonNames {
	
	String name;
	Language language;
	Recommendation reco;
	//on every reference will be incremented. if its 0 then row will be deleted 
	int referanceCounter = 0;
	
	static constraints = {
		name(blank:false, nullable:false, unique:['name', 'language', 'reco']);
		language(nullable:true);
		reco(nullable:true);
	}

	static mapping = {
		version false;
	}
	
	//duplicate check will be done on name lang and reco
}
