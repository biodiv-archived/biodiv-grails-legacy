<%@page import="species.utils.ImageType"%>

<g:if test="${!recoComments.isEmpty()}">
	<div class="btn-group" id="reco_comment_${recoId}">

		<a class="reco-comment-action" data-toggle="dropdown" href="#">View
			comment</a>
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
								title="${commentInstance.author.username}"/>
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

<style>
.reco-comment-table {
	background-clip: padding-box;
	background-color: #FFFFFF;
	border-color: rgba(0, 0, 0, 0.2);
	border-radius: 0 0 5px 5px;
	border-style: solid;
	border-width: 1px;
	box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
	display: none;
	float: left;
	left: 0;
	list-style: none outside none;
	margin: 0;
	min-width: 400px;
	max-width: 400px;
	width: 400px;
	padding: 10px;
	position: absolute;
	top: 100%;
	z-index: 1000;
	color: #000000;
}

.reco-comment-close {
	position: absolute;
	top: 0;
	right: 0;
}
</style>
