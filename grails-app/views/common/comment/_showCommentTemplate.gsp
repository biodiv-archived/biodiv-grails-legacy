<%@page import="species.utils.ImageType"%>
<div class="yj-thread-replies-container">
	<div class="yj-message-container">
		<div class="yj-avatar">
			<g:link controller="SUser" action="show" id="${commentInstance.author.id}">
				<img class="small_profile_pic" src="${commentInstance.author.icon(ImageType.SMALL)}" title="${commentInstance.author.name}" />
			</g:link>
		</div>
			<b> ${commentInstance.author.name} :</b>
			<comment:showCommentContext model="['commentInstance' : commentInstance]" /> 
			<div class="yj-message-body">${commentInstance.body}</div>
			<div class="yj-attributes">
				<g:formatDate date="${commentInstance.lastUpdated}" type="datetime" style="LONG" timeStyle="SHORT"/>
				<sUser:ifOwns model="['user':commentInstance.author]">
					<a href="#" onclick="deleteComment(${commentInstance.id}, $(this).closest('li'), '${createLink(controller:'comment', action:'removeComment')}'); return false;">
					<span class="deleteFlagIcon"><i class="icon-trash icon-red"></i></span></a>
				</sUser:ifOwns>
		</div>
	<%----%>
	<%--<g:if test="${commentInstance.comments}">--%>
	<%--	<comment:showAllComments model="['commentHolder':commentInstance]" />--%>
	<%--</g:if>--%>
	<%--<hr>--%>
	</div>
</div>
