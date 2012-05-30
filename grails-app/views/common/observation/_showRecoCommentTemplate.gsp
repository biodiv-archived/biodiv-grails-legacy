<%@page import="species.utils.ImageType"%>

<g:if test="${!recoComments.isEmpty()}">
	<div class="btn-group" id="reco_comment_${recoId}">

		<a class="reco-comment-action" data-toggle="dropdown" href="#"><g:if test="${recoComments.size() > 1}">View comments</g:if>
					<g:else>View comment</g:else></a>
		<div class="reco-comment-table" style="display: none">
			<div class="reco-comment-close" value="close">
				<i class="icon-remove"></i>
			</div>
			<div id="flagged">
				<ul>
					<g:each var="commentInstance" in="${recoComments}">
						<li style="padding: 0 5px; clear: both;"><span
							class="flagInstanceClass">
							<g:link controller="SUser" action="show" id="${commentInstance.author?.id}">
							<img class="very_small_profile_pic"
								src="${commentInstance.author?.icon(ImageType.VERY_SMALL)}"
								title="${commentInstance.author.name}"/>
							</g:link> on <g:formatDate date="${commentInstance.votedOn}" type="datetime" style="LONG" timeStyle="SHORT"/> : ${commentInstance.comment} 
						</span></li>
					</g:each>
				</ul>
			</div>
		</div>

	</div>
</g:if>

<script>
	$(document).ready(function() {
		$('#reco_comment_' + ${recoId} + ' .reco-comment-action').click(function() {
			$('#reco_comment_' + ${recoId} +' .reco-comment-table').show();
		});
		$('#reco_comment_' + ${recoId} +' .reco-comment-close').click(function() {
			$('#reco_comment_' + ${recoId} + ' .reco-comment-table').hide();
		});
	});
</script>
