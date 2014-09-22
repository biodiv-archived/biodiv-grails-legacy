<%@page import="species.participation.Comment"%>
<%@page import="species.participation.ActivityFeedService"%>
<div class="comment">
	<comment:postComment model="['commentHolder':commentHolder, 'rootHolder':rootHolder, 'commentType':commentType, 'newerTimeRef': newerTimeRef]" />
	<g:if test="${showCommentList == true}">
		<ul>
			<comment:showCommentList model="['comments':comments]" />
   		</ul>
		<input type="hidden" name='olderTimeRef' value="${olderTimeRef}"/>
		<g:if test="${(totalCount - comments.size()) > 0}" >
			<a class="yj-thread-replies-container yj-show-older-replies" href="#" title="${g.message(code:'showallcomments.show.replies')}" onclick='loadOlderComment($(this).closest(".comment"), "${commentType}", "${commentHolder.id}", "${ActivityFeedService.getType(commentHolder)}", "${rootHolder.id}", "${rootHolder.class.getCanonicalName()}", "${createLink(controller:'comment',  action:'getComments')}");return false;'><g:message code="link.show" /> ${totalCount - comments.size()} <g:message code="showallcommentstemplate.older.comments" /> >></a>
		</g:if>
	</g:if>
</div>
