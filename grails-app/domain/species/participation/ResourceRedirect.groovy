package species.participation

class ResourceRedirect {

    static constraints = {
		sourceId(unique:['soruceType', 'targetType', 'targetId'])
    }
	
	static mapping = {
		version : false;
    }
	
	String soruceType;
	Long sourceId;
	String targetType;
	Long targetId;
	
	
	
	static ResourceRedirect addLink(source, target){
		def rr = new ResourceRedirect(soruceType:source.class.canonicalName, sourceId:source.id, targetType:target.class.canonicalName, targetId:target.id) 
		if(!rr.save(flush:true)){
			rr.errors.allErrors.each { log.error it }
			return null
		}
		return rr
	}
	
		
}
