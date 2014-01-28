	<%
		boolean isFollowing = sourceObject.fetchIsFollowing()
		def followButtonTitle = isFollowing  ? 'Unfollow' : 'Follow'
	%>
        <button id="followButton" class="btn btn-link" onclick="followObject('${sourceObject.class.getCanonicalName()}', ${sourceObject.id}, this, '${uGroup.createLink(controller:'activityFeed', action:'follow')}');" title="Get notifications on any further participation on this page."><i class="icon-play-circle"></i>${followButtonTitle}</button>
 
