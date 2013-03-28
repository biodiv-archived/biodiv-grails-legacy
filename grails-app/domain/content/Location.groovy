package content

class Location {
    
    String siteName;
    String corridor;
    static constraints = {
		corridor(nullable:true)
		siteName(nullable:true)
    }
}
