import species.participation.RecommendationVote
import species.participation.Comment

RecommendationVote.findAllByCommentIsNotNull().each { RecommendationVote rv ->
	
	def commentHolderId = rv.recommendation.id
	def commentHolderType = rv.recommendation.class.getName()
	
	def rootHolderId = rv.observation.id
	def rootHolderType = rv.observation.class.getName()
	
	def dateCreated = rv.votedOn
	def lastUpdated = dateCreated
	
	Comment c = new Comment(author:rv.author, body:rv.comment, commentHolderId:commentHolderId, \
						commentHolderType:commentHolderType, rootHolderId:rootHolderId, \
						rootHolderType:rootHolderType, dateCreated:dateCreated, lastUpdated:lastUpdated)
	
	if(!c.save(flush:true)){
		c.errors.allErrors.each { println  it }
	}
	
	println "=========== voted on " + dateCreated
	c.dateCreated = dateCreated
	c.lastUpdated = lastUpdated
	
	if(!c.save(flush:true)){
		c.errors.allErrors.each { println  it }
	}
}

//please run following command on database
//biodiv=#  update comment set last_updated = date_created;
//becasue last updated is managed by grails so manully setting its time to same as date_created