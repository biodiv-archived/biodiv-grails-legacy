package species

import grails.util.Holders

class Classification {

	String name;
    String citation;
    Language language;
	
	static constraints = {
		name(blank:false, unique:true);
		citation(nullable:true);
		language(nullable:false);
    }
	
	static mapping = {
		version : false;
		sort name:"asc"
	}
	
	public static Classification fetchIBPClassification(){
		return Classification.findByName(Holders.config.speciesPortal.fields.IBP_TAXONOMIC_HIERARCHY)
	}
}
