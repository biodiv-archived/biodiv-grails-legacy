<div class="post-comment">
	<form class="form-horizontal" onSubmit='return postComment(this, "${createLink(controller:'comment', action:'addComment')}")'>
		<textarea name="commentBody" class="comment-textbox" placeholder="Write comment" ></textarea>
		<span  style="color:#B84A48; display:none;">Please write comment</span>
		<input type="hidden" name='commentHolderId' value="${commentHolder.id}" />
		<input type="hidden" name='commentHolderType' value="${commentHolder.class.getCanonicalName()}" />
		<input type="hidden" name='rootHolderId' value="${rootHolder.id}" />
		<input type="hidden" name='rootHolderType' value="${rootHolder.class.getCanonicalName()}" />
		<input type="hidden" name='commentType' value="${commentType}" />
		<input type="submit" value="Post" class="btn comment-post-btn " style="float:right;"/>
	</form>
</div>
