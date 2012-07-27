<%@page import="species.participation.Comment"%>
<div class="comment">
	<ul>
		<comment:showCommentList model="['comments':comments]" />
	</ul>	
	<input type="hidden" name='newerTimeRef' value="${newerTimeRef}"/>
	<input type="hidden" name='olderTimeRef' value="${olderTimeRef}"/>
	<g:if test="${(totalCount - comments.size()) > 0}" >
		<a class="yj-thread-replies-container yj-show-older-replies" href="#" title="show replies" onclick='loadOlderComment($(this).closest(".comment"), "${commentType}", "${commentHolder.id}", "${commentHolder.class.getCanonicalName()}", "${rootHolder.id}", "${rootHolder.class.getCanonicalName()}", "${createLink(controller:'comment',  action:'getComments')}");return false;'>Show older replies >></a>
	</g:if>
	<comment:postComment model="['commentHolder':commentHolder, 'rootHolder':rootHolder, commentType:commentType]" />
</div>