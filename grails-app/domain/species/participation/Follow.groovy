package species.participation

import grails.converters.JSON;
import species.auth.SUser;

class Follow extends AbstractAction {
	

		
	static constraints = {
		objectType(unique:['objectId', 'author'])
	}
		
    static boolean fetchIsFollowing(object, SUser author) {
		if(!author){
			return false
		}
		
//		if(object.author == author){
//			return true
//		}
		
		String objectType = object.class.getCanonicalName()
		Long objectId = object.id
				def follow = Follow.findWhere(objectType:objectType, objectId:objectId, author:author)
		return follow ? true : false
	}
	
	static SUser addFollower(object, SUser author,boolean flushImmidiatly=true){

		if(!fetchIsFollowing(object, author)){
            String objectType = object.class.getCanonicalName()
            Long objectId = object.id
            println objectType
            println objectId
            println author
            Follow follow = new Follow(objectType:objectType, objectId:objectId, author:author)
            if(!follow.save(flush:flushImmidiatly)){
                follow.errors.allErrors.each { log.error it }
                return null
            }
            
            return follow.author
	    }
    }
	
	static  SUser deleteFollower(object, SUser author){
		if(!fetchIsFollowing(object, author)){
			return author
		}
		
		String objectType = object.class.getCanonicalName()
		Long objectId = object.id
	
		Follow follow = Follow.findWhere(objectType:objectType, objectId:objectId, author:author)
		try{
			if(follow)
				follow.delete(flush:true, failOnError:true)
		}catch (Exception e) {
			e.printStackTrace()
			return null
		}
		
		return author
	}
	
	static List<SUser> getFollowers(object){
		String objectType = object.class.getCanonicalName()
		Long objectId = object.id
		return getFollowers(objectType, objectId)
		
	}
	
	static List<SUser> getFollowers(objectType, objectId){
		return Follow.findAllByObjectTypeAndObjectId(objectType, objectId).collect{ it.author }
	}

	static void deleteAll(SUser author) {
		executeUpdate 'DELETE FROM Follow WHERE author=:author', [author: author]
	}
}
