package content

class DataLink {
	
	String description
	String url

    static constraints = {
		description(nullable:true)
		url(nullable:true)
    }
}
