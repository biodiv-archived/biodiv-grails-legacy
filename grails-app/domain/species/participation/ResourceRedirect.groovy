package species.participation

class ResourceRedirect {
	
	
	def utilsService

    static constraints = {
		sourceId(unique:['sourceType', 'targetType', 'targetId'])
    }
	
	static mapping = {
		version : false;
    }
	
	String sourceType;
	Long sourceId;
	String targetType;
	Long targetId;
	
	
	
	static ResourceRedirect addLink(source, target){
		def rr = new ResourceRedirect(sourceType:source.class.canonicalName, sourceId:source.id, targetType:target.class.canonicalName, targetId:target.id) 
		if(!rr.save(flush:true)){
			rr.errors.allErrors.each { log.error it }
			return null
		}
		return rr
	}
	
	def fetchTargetInstance(String sourceType, sourceId){
		def rr = ResourceRedirect.findBySourceTypeAndSourceId(sourceType, sourceId)
		return utilsService.getDomainObject(rr.targetType, '' + rr.targetId)
	}
		
}
