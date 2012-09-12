<%@page import="species.participation.Comment"%>
<div class="feedActivityHolderContext yj-message-body">
	<pre>${feedText}</pre>
	<g:if test="${feedPermission != 'readOnly' && activityInstance}">
		<g:if test="${activityInstance.class.getCanonicalName() == Comment.class.getCanonicalName()}">
			<sUser:ifOwns model="['user':activityInstance.author]">
				<div class="reco-comment-close" value="close" title="delete comment"
					onclick="deleteComment(${activityInstance.id}, '${createLink(controller:'comment', action:'removeComment')}'); removeActivity(this);return false;">
					<i class="icon-remove"></i>
				</div>
			</sUser:ifOwns>
		</g:if>
	</g:if>
</div>
