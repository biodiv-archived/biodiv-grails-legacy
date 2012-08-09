<div class="btn-group comment-popup">
	<a class="btn btn-mini" data-toggle="dropdown" href="#" onclick="$(this).next().show(); $(this).next().find('.comment .yj-message-body').linkify() ;return false;"><i class="icon-comment"></i><g:if test="${totalCount > 0}">${totalCount}</g:if></a>
	<div class="reco-comment-table" style="display: none">
		<comment:showAllComments model="['commentHolder':commentHolder, 'rootHolder':rootHolder]" />
		<div class="reco-comment-close" value="close" onclick="$(this).parent().hide(); return false;">
			<i class="icon-remove"></i>
		</div>
	</div>
</div>
