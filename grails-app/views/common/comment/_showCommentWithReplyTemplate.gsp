<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<div class="yj-message-container">
	<div class="yj-avatar">
		<a href="${uGroup.createLink(controller:'SUser', action:'show', id:feedInstance.author.id, userGroup:feedInstance.fetchUserGroup(), 'userGroupWebaddress':feedInstance.fetchUserGroup()?.webaddress)}">
			<img class="small_profile_pic"
				src="${feedInstance.author.profilePicture(ImageType.SMALL)}"
				title="${feedInstance.author.name}" />
		</a>
	</div>
	<b> ${feedInstance.author.name} :
	<g:if test="${commentInstance.isMainThread()}">
		<span class="yj-context"> ${raw(commentContext)} </span>
	</g:if>
	<g:else>	
		<span class="yj-context" title="${commentInstance.fetchParentText()}"><g:message code="default.reply.to.label" /></span><g:link controller="SUser" action="show"
			id="${commentInstance.fetchParentCommentAuthor()?.id}"> ${commentInstance.fetchParentCommentAuthor()?.name} </g:link>
	</g:else>
	</b>
	<div class="feedActivityHolderContext yj-message-body">
		
	<%  def styleVar = 'block';
        def clickcontentVar = '' 
    %> 
    <g:if test="${commentInstance?.language?.id != userLanguage?.id}">
        <%  
          styleVar = "none"
          clickcontentVar = '<a href="javascript:void(0);" class="clickcontent btn btn-mini">'+commentInstance?.language?.twoLetterCode?.toUpperCase()+'</a>';
        %>
    </g:if>
        
    ${raw(clickcontentVar)}
    <div style="display:${styleVar}">
		${raw(Utils.linkifyYoutubeLink(commentInstance.body?.replaceAll("\\n",'<br/>')))}
	</div>	
	




	</div>
	<g:if test="${feedPermission != 'readOnly' && commentInstance}">
		<sUser:ifOwns model="['user':commentInstance.author]">
		
        <div class="reco-comment-edit" value="edit" title="${g.message(code:'showcommentwithreply.edit.comment')}"
				onclick="editCommentActivity(this, ${commentInstance.id}); return false;">
				<i class="icon-edit"></i>
			</div>

        <div class="reco-comment-close" value="close" title="${g.message(code:'showcommentwithreply.delete.comment')}"
				
				onclick="deleteCommentActivity(this, ${commentInstance.id}, '${uGroup.createLink(controller:'comment', action:'removeComment',  userGroup:feedInstance.fetchUserGroup(), 'userGroupWebaddress':feedInstance.fetchUserGroup()?.webaddress)}'); return false;">
				<i class="icon-remove"></i>
			</div>
		</sUser:ifOwns>
	</g:if>
	<time class="timeago" datetime="${feedInstance.lastUpdated.getTime()}"></time>
	
	<div class="comment-reply">
		<a data-toggle="dropdown" href="#" title="${g.message(code:'showcommentwithreply.reply.comment')}" onclick='$(this).hide();$(this).siblings(".commnet-reply-popup").show();return false'><g:message code="default.reply.label" /></a>
		<div class="commnet-reply-popup clearfix" style="display: none;position:relative;">
			<div class="popup-form-close" value="close" onclick='$(this).parent().hide().prev().show(); return false;'>
				<i class="icon-remove"></i>
			</div>
			<textarea name="commentBody" class="comment-textbox" placeholder="${g.message(code:'showcommentwithreply.reply.comment')}"></textarea>
			<a href="#" class="btn btn-mini pull-right" title="${g.message(code:'showcommentwithreply.post.comment')}" onclick='replyOnComment($(this), ${commentInstance.id}, "${uGroup.createLink(controller:'comment', action:'addComment',  userGroup:feedInstance.fetchUserGroup(), 'userGroupWebaddress':feedInstance.fetchUserGroup()?.webaddress)}"); return false;'><g:message code="button.post" /></a>	
		</div>
	</div>
	
</div>
