package species.participation

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria

import species.auth.SUser
import species.Resource

class Comment{

	//comment basic info
	String body;
	Date dateCreated;
	Date lastUpdated;

	//comment holder (i.e recoVote, image)
	Long commentHolderId;
	String commentHolderType;

	//root holder(i.e observation, group)
	Long rootHolderId;
	String rootHolderType;

	//comment parent (to handle reply)
	//Comment parentComment;

	static hasMany = [likes:SUser, attachments:Resource];
	static belongsTo = [author:SUser];

	static constraints() {
		body blank:false;
		//parentComment nullable:false;
	}

	static mapping = {
		version : false;
		body type:'text';
	}

	static int fetchCount(commentHolder, rootHolder, refTime, timeLine){
		timeLine = (timeLine)?:"older"
		refTime = getDate(refTime)
		if(commentHolder || rootHolder){
			return Comment.withCriteria(){
				projections {
					count('id')
				}
				and{
					if(commentHolder){
						eq('commentHolderId', commentHolder.id)
						eq('commentHolderType', getType(commentHolder))
					}
					if(rootHolder){
						eq('rootHolderId', rootHolder.id)
						eq('rootHolderType', getType(rootHolder))
					}
					if(refTime){
						(timeLine == "older") ? lt('lastUpdated', refTime) : gt('lastUpdated', refTime)
					}
				}
			}[0]
		}else{
			return 0;
		}
	}

	static int fetchSuperCount(rootHolder, refTime, timeLine){
		return fetchCount(null, rootHolder, refTime, timeLine)
	}
	

	static fetchComments(commentHolder, rootHolder, max, refTime, timeLine){
		timeLine = (timeLine)?:"older"
		refTime = getDate(refTime)
		if(!refTime){
			return Collections.EMPTY_LIST
		}
		return Comment.withCriteria(){
			and{
				if(commentHolder){
					eq('commentHolderId', commentHolder.id)
					eq('commentHolderType', getType(commentHolder))
				}
				if(rootHolder){
					eq('rootHolderId', rootHolder.id)
					eq('rootHolderType', getType(rootHolder))
				}
				(timeLine == "older") ? lt('lastUpdated', refTime) : gt('lastUpdated', refTime)
			}
			maxResults max
			order 'lastUpdated', 'desc'
		}
	}

	static fetchSuperComments(rootHolder, max, refTime, timeLine){
		return fetchComments(null, rootHolder, max, refTime, timeLine)
	}
	
	private static getType(obj){
		return Hibernate.getClass(obj).getName();
	}


	private static getDate(String timeIn){
		if(!timeIn){
			return null
		}
		
		try{
			return new Date(timeIn.toLong())
		}catch (Exception e) {
			e.printStackTrace()
		}
		return null
	}
	
	//	int compareTo(obj) {
	//		lastUpdated.compareTo(obj.lastUpdated);
	//	}
}
