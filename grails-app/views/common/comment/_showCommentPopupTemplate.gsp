<div class="btn-group comment-popup">
	<a class="btn btn-mini" data-toggle="dropdown" href="#"
		onclick="$(this).next().show(); dcorateCommentBody($(this).next().find('.comment .yj-message-body')) ;return false;">
		<g:if test="${totalCount == 0}">
			<i class="icon-comment icon-gray"></i>
		</g:if>
		<g:else>
			<i class="icon-comment"></i>
		</g:else>
		${totalCount}</a>
	<div class="reco-comment-table" style="display: none">
		<comment:showAllComments
			model="['commentHolder':commentHolder, 'rootHolder':rootHolder]" />
		<div class="reco-comment-close" value="close"
			onclick="$(this).parent().hide(); return false;">
			<i class="icon-remove"></i>
		</div>
	</div>
</div>
