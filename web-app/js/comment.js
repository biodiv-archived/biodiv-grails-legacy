
function deleteComment(commentId, commentComp, url){
	$.ajax({
		url: url,
		data:{"commentId":commentId},
		
		success: function(data){
			$(commentComp).remove();
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

function postComment(postComp, url) {
	var targetComp = $(postComp).closest('.comment');
	var refTime = $(targetComp).children('input[name="newerTimeRef"]').val();
	$(postComp).ajaxSubmit({ 
     	url:url,
		dataType: 'json', 
		data : {refTime:refTime},
		clearForm: true,
		resetForm: true,
		type: 'POST',
		beforeSubmit: function(formData, jqForm, options) {
			return true;
		}, 
        success: function(data, statusText, xhr, form) {
        	$(targetComp).children('ul').prepend(data.showCommentListHtml);
        	$(targetComp).children('input[name="newerTimeRef"]').val(data.newerTimeRef);
        	return false;
        },
        error:function (xhr, ajaxOptions, thrownError){
        	var msg = $.parseJSON(xhr.responseText);
       		alert("error " + msg);
		} 
 	});
	return false;
}

function loadOlderComment(targetComp, commentType, commentHolderId, commentHolderType, rootHolderId, rootHolderType, url){
	var refTime = $(targetComp).children('input[name="olderTimeRef"]').val();
 	$.ajax({
 		url: url,
		dataType: "json",
		data: {commentType:commentType, commentHolderId:commentHolderId , commentHolderType:commentHolderType, rootHolderId:rootHolderId, rootHolderType:rootHolderType, refTime:refTime},	
		success: function(data) {
			$(targetComp).children('ul').append(data.showCommentListHtml);
			$(targetComp).children('input[name="olderTimeRef"]').val(data.olderTimeRef);
		}, error: function(xhr, status, error) {
			alert(xhr.responseText);
	   	}
	});
}

 

