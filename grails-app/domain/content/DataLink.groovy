package content

class DataLink {
	
	String description
	String url

    static constraints = {
		description(nullable:false)
		url(nullable:false)
    }
}
