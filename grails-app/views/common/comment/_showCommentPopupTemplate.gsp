<div class="btn-group">
	<a class="btn btn-mini" data-toggle="dropdown" href="#" onclick="$(this).next().show(); return false;"><i class="icon-comment"></i>Comment</a>
	<div class="reco-comment-table" style="display: none">
		<comment:showAllComments model="['commentHolder':commentHolder, 'rootHolder':rootHolder]" />
		<div class="reco-comment-close" value="close" onclick="$(this).parent().hide(); return false;">
			<i class="icon-remove"></i>
		</div>
	</div>
</div>
