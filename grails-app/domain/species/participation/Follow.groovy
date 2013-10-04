package species.participation

import grails.converters.JSON;
import species.auth.SUser;

class Follow {

	String objectType
	Long objectId;
	
	static belongsTo = [user:SUser];
	
	static constraints = {
		objectType(unique:['objectType', 'objectId', 'user'])
	}
	static mapping = {
		version : false;
    }
	
	static boolean fetchIsFollowing(object, SUser user) {
		if(!user){
			return false
		}
		
//		if(object.author == user){
//			return true
//		}
		
		String objectType = object.class.getCanonicalName()
		Long objectId = object.id
		
		def follow = Follow.findWhere(objectType:objectType, objectId:objectId, user:user)
		return follow ? true : false
	}
	
	static SUser addFollower(object, SUser user, boolean flushImmidiatly=true){
		if(fetchIsFollowing(object, user)){
			return user
		}
		
		String objectType = object.class.getCanonicalName()
		Long objectId = object.id
		
		Follow follow = new Follow(objectType:objectType, objectId:objectId, user:user)
		if(!follow.save(flush:flushImmidiatly)){
			follow.errors.allErrors.each { log.error it }
			return null
		}
		
		return follow.user
	}	
	
	static  SUser deleteFollower(object, SUser user){
		if(!fetchIsFollowing(object, user)){
			return user
		}
		
		String objectType = object.class.getCanonicalName()
		Long objectId = object.id
		
		Follow follow = Follow.findWhere(objectType:objectType, objectId:objectId, user:user)
		try{
			if(follow)
				follow.delete(flush:true, failOnError:true)
		}catch (Exception e) {
			e.printStackTrace()
			return null
		}
		
		return user
	}
	
	static List<SUser> getFollowers(object){
		String objectType = object.class.getCanonicalName()
		Long objectId = object.id
		return getFollowers(objectType, objectId)
		
	}
	
	static List<SUser> getFollowers(objectType, objectId){
		return Follow.findAllByObjectTypeAndObjectId(objectType, objectId).collect{ it.user }
	}

	static void deleteAll(SUser user) {
		executeUpdate 'DELETE FROM Follow WHERE user=:user', [user: user]
	}
}
