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

    static List<Classification> list() { 
        println "Classification overridden fn for cache"
        return Classification.createCriteria().list {
            cache true
        }
    }

    static Classification findByName(String whatever) { 
        println "Classification overridden fn for cache"
        return Classification.createCriteria().get {
            eq 'name', whatever
            cache true
        }
    } 
}
