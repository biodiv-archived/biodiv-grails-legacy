<%@page import="species.utils.ImageType"%>

<g:if test="${!recoComments.isEmpty()}">
	<div class="btn-group" id="reco_comment_${recoId}">

		<a class="reco-comment-action" data-toggle="dropdown" href="#" onclick="$(this).next('.reco-comment-table').show(); return false;"><g:if test="${recoComments.size() > 1}">View comments</g:if>
					<g:else>View comment</g:else></a>
		<div class="reco-comment-table" style="display: none">
			<div class="reco-comment-close" value="close" onclick="$(this).parent().hide(); return false;">
				<i class="icon-remove"></i>
			</div>
			<div class="flagged">
				<ul>
					<g:each var="recoVoteInstance" in="${recoComments}">
						<li style="padding: 0 5px; clear: both;"><span
							class="flagInstanceClass ellipsis multiline">
							<g:link controller="SUser" action="show" id="${recoVoteInstance.author?.id}">
							<img class="small_profile_pic"
								src="${recoVoteInstance.author?.icon(ImageType.VERY_SMALL)}"
								title="${recoVoteInstance.author.name}"/>
							</g:link> on <g:formatDate date="${recoVoteInstance.votedOn}" type="datetime" style="LONG" timeStyle="SHORT"/> : ${recoVoteInstance.comment} 
						</span>
						<sUser:ifOwns model="['user':recoVoteInstance.author]">
								<a href="#" onclick="removeRecoComment(${recoVoteInstance.recoVoteId},'#reco_comment_' + ${recoId}, '${createLink(controller:'observation', action:'deleteRecoVoteComment')}',$(this).parent()); return false;"><span class="deleteCommentIcon" data-original-title="Remove this comment" ><i class="icon-trash icon-red"></i></span></a>
						</sUser:ifOwns>
						</li>
					</g:each>
				</ul>
			</div>
		</div>

	</div>
</g:if>
