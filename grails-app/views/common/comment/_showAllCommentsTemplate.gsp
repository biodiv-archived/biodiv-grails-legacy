<%@page import="species.participation.Comment"%>
<div class="comment">
	<comment:postComment model="['commentHolder':commentHolder, 'rootHolder':rootHolder, 'commentType':commentType, 'newerTimeRef': newerTimeRef]" />
	<g:if test="${showCommentList == true}">
	<ul>
		<comment:showCommentList model="['comments':comments]" />
	</ul>
	<input type="hidden" name='olderTimeRef' value="${olderTimeRef}"/>
	<g:if test="${(totalCount - comments.size()) > 0}" >
		<a class="yj-thread-replies-container yj-show-older-replies" href="#" title="show replies" onclick='loadOlderComment($(this).closest(".comment"), "${commentType}", "${commentHolder.id}", "${commentHolder.class.getCanonicalName()}", "${rootHolder.id}", "${rootHolder.class.getCanonicalName()}", "${createLink(controller:'comment',  action:'getComments')}");return false;'>Show ${totalCount - comments.size()} older comments >></a>
	</g:if>
	</g:if>
</div>