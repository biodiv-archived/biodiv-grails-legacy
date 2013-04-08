package content

class Location {
    
    String siteName;
    String corridor;
	
	boolean deleted;
	
	static transients = ['deleted']
	
    static constraints = {
		corridor(nullable:true)
		siteName(nullable:true)
    }
	
	static belongsTo = [Project]
}
