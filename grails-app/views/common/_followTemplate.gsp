	<%
		boolean isFollowing = sourceObject.fetchIsFollowing()
	def unfollow=g.message(code:"followtemp.unfollow")
    def follow=g.message(code:"followtemp.follow")

    def followButtonTitle = isFollowing  ? unfollow : follow
	%>
        <button id="followButton" class="btn btn-link" onclick="followObject('${sourceObject.class.getCanonicalName()}', ${sourceObject.id}, this, '${uGroup.createLink(controller:'activityFeed', action:'follow')}');" title="${g.message(code:'followtemplate.title.notifications')}"><i class="icon-play-circle"></i>${followButtonTitle}</button>
 
