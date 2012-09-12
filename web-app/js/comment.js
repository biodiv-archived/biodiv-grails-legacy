
function deleteComment(commentId, url){
	$.ajax({
		url: url,
		data:{"commentId":commentId},
		
		success: function(data){
			$('.comment .' + commentId).remove();
		},
		
		statusCode: {
			401: function() {
				show_login_dialog();
			}	    				    			
		},
		error: function(xhr, status, error) {
			var msg = $.parseJSON(xhr.responseText);
			alert(msg);
		}
	});
}

function postComment(postComp, url, newCommentUrl) {
	var textComp = $(postComp).children('textarea[name="commentBody"]');
	if($.trim(textComp.val()) === ""){
		$(textComp).addClass('comment-textEmpty');
		$(textComp).next('span').show();
		return false;
	}
	
	postAsAjax(postComp, url, newCommentUrl, true);
	
	$(textComp).removeClass('comment-textEmpty');
	$(textComp).next('span').hide();
	return false;
}

function updateUnionComment(postComp, url){
	var unionPostComp = $('.union-comment .post-comment').children('form');
	if(unionPostComp){
		var postCommentProp = $(postComp).children('input[name="commentHolderId"]').val() + $(postComp).children('input[name="commentHolderType"]').val();
		var unionPostCommentProp = $(unionPostComp).children('input[name="commentHolderId"]').val() + $(unionPostComp).children('input[name="commentHolderType"]').val();
		if(postCommentProp !== unionPostCommentProp ){
			postAsAjax(unionPostComp, url, url, false);
		}
	}
}

function postAsAjax(postComp, url, newCommentUrl, update){
	var targetComp = $(postComp).closest('.comment');
	$(postComp).ajaxSubmit({ 
     	url:url,
		dataType: 'json', 
//		clearForm: true,
//		resetForm: true,
		type: 'POST',
		beforeSubmit: function(formData, jqForm, options) {
			return true;
		}, 
        success: function(data, statusText, xhr, form) {
        	if(data.showCommentListHtml){
    			var htmlData = $(data.showCommentListHtml);
    			dcorateCommentBody(htmlData.find('.yj-message-body'));
    			$(targetComp).children('ul').prepend(htmlData);
    			$(postComp).children('input[name="newerTimeRef"]').val(data.newerTimeRef);
    			updateCountOnPopup(postComp, data.newlyAddedCommentCount);
    			if(update){
    				updateFeeds();
        			updateUnionComment(postComp, newCommentUrl);
    			}
    			if(data.clearForm){
    				$(postComp).children('textarea[name=commentBody]').val("");
    			}
        	}
    		return false;
        },
        error:function (xhr, ajaxOptions, thrownError){
        	//successHandler is used when ajax login succedes
        	var successHandler = this.success, errorHandler = undefined;
        	handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
		} 
 	});
}

function updateCountOnPopup(postComp, newlyAddedCount){
	var popupButton = $(postComp).closest('.comment-popup').children('a');
	if(popupButton.attr("class")){
		var newCount = parseInt(popupButton.text()) + newlyAddedCount;
		popupButton.html('<i class="icon-comment"></i>' + newCount);
	}
}

function loadOlderComment(targetComp, commentType, commentHolderId, commentHolderType, rootHolderId, rootHolderType, url){
	var refTime = $(targetComp).children('input[name="olderTimeRef"]').val();
 	$.ajax({
 		url: url,
		dataType: "json",
		data: {commentType:commentType, commentHolderId:commentHolderId , commentHolderType:commentHolderType, rootHolderId:rootHolderId, rootHolderType:rootHolderType, refTime:refTime},	
		success: function(data) {
			var htmlData = $(data.showCommentListHtml);
			dcorateCommentBody(htmlData.find('.yj-message-body'));
			$(targetComp).children('ul').append(htmlData);
			$(targetComp).children('input[name="olderTimeRef"]').val(data.olderTimeRef);
			if(data.remainingCommentCount == 0){
				$(targetComp).children('a').hide();	
			}else{
				$(targetComp).children('a').text("Show " + data.remainingCommentCount + " older comments >>");
			}
		}, error: function(xhr, status, error) {
			alert(xhr.responseText);
	   	}
	});
}

 

