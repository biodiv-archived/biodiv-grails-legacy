package content

class DataLink {
	
	String description
	String url
	
	boolean deleted

	static transients = ['deleted']
	
    static constraints = {
		description(nullable:true)
		url(nullable:true)
    }
	
	static belongsTo = [Project]
}
