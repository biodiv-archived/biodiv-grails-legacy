<%@page import="species.utils.ImageType"%>

<g:if test="${!recoComments.isEmpty()}">
	<div class="btn-group" id="reco_comment_${recoId}">

		<a class="reco-comment-action" data-toggle="dropdown" href="#" onclick="$(this).next('.reco-comment-table').show(); return false;"><g:if test="${recoComments.size() > 1}"><g:message code="button.view.comment" /></g:if>
					<g:else><g:message code="button.View.comment" /></g:else></a>
		<div class="reco-comment-table" style="display: none">
			<div class="reco-comment-close" value="close" onclick="$(this).parent().hide(); return false;">
				<i class="icon-remove"></i>
			</div>
			<div class="flagged">
				<ul>
					<g:each var="recoVoteInstance" in="${recoComments}">
						<li style="padding: 0 5px; clear: both;"><span
							class="flagInstanceClass ellipsis multiline">
							<g:link url="${uGroup.createLink(controller:"user", action:"show", id:recoVoteInstance.author?.id)}">
							<img class="small_profile_pic"
								src="${recoVoteInstance.author?.profilePicture(ImageType.SMALL)}"
								title="${recoVoteInstance.author.name}"/>
							</g:link> <g:message code="text.on" /> <g:formatDate date="${recoVoteInstance.votedOn}" type="datetime" style="LONG" timeStyle="SHORT"/> : ${recoVoteInstance.comment} 
						</span>
						<sUser:ifOwns model="['user':recoVoteInstance.author]">
								<a href="#" onclick="removeRecoComment(${recoVoteInstance.recoVoteId},'#reco_comment_' + ${recoId}, '${uGroup.createLink(controller:'observation', action:'deleteRecoVoteComment')}',$(this).parent()); return false;"><span class="deleteCommentIcon" data-original-title="Remove this comment" ><i class="icon-trash icon-red"></i></span></a>
						</sUser:ifOwns>
						</li>
					</g:each>
				</ul>
			</div>
		</div>

	</div>
</g:if>
