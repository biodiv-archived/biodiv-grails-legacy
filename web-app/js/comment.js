function deleteCommentActivity(targetComp, commentId, url){
	if(confirm('This comment will be deleted. Are you sure ?')){
		deleteComment(commentId, url);
		removeActivity(targetComp);
	}
}

function deleteComment(commentId, url){
	$.ajax({
		url: url,
		data:{"commentId":commentId},
		
		success: function(data){
			$('.comment .' + commentId).remove();
			return true;
		},
		
		statusCode: {
			401: function() {
				show_login_dialog();
			}	    				    			
		},
		error: function(xhr, status, error) {
			var msg = $.parseJSON(xhr.responseText);
			alert(msg);
			return false;
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
	computeUserTag(postComp);
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
	var options = { 
     	url:url,
		dataType: 'json', 
//		clearForm: true,
//		resetForm: true,
		type: 'POST',
		beforeSubmit: function(formData, jqForm, options) {
			return true;
		}, 
        success: function(data, statusText, xhr, form) {
        	if(data.status == 401) {
        		$(postComp).ajaxSubmit(options);
        	}else if(data.status === 'Error'){
        		alert(data.msg);
        	}
        	else if(data.showCommentListHtml){
    			var htmlData = $(data.showCommentListHtml);
    			//dcorateCommentBody(htmlData.find('.yj-message-body'));
    			$(targetComp).children('ul').prepend(htmlData);
    			$(postComp).children('input[name="newerTimeRef"]').val(data.newerTimeRef);
    			updateCountOnPopup(postComp, data.newlyAddedCommentCount);
    			if(update){
    				updateFeeds();
    				setFollowButton();
    				updateUnionComment(postComp, newCommentUrl);
    			}
    			if(data.clearForm){
    				$(postComp).find('.contentbox').html("");
    				$(postComp).children('textarea[name=commentBody]').val("");
    				$(postComp).children('textarea[name=commentSubject]').val("");
    			}
        	}
    		return false;
        },
        error:function (xhr, ajaxOptions, thrownError){
        	//successHandler is used when ajax login succedes
        	var successHandler = this.success, errorHandler = undefined;
        	handleError(xhr, ajaxOptions, thrownError, successHandler, function(){
        		
        	});
		} 
 	}
	$(postComp).ajaxSubmit(options);
}

function updateCountOnPopup(postComp, newlyAddedCount){
	var popupButton = $(postComp).closest('.comment-popup').children('a');
	if(popupButton.attr("class")){
		var newCount = parseInt(popupButton.text()) + newlyAddedCount;
		popupButton.html('<i class="icon-comment"></i>' + ' ' + newCount);
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
			//dcorateCommentBody(htmlData.find('.yj-message-body'));
			$(targetComp).children('ul').append(htmlData);
			$(targetComp).children('input[name="olderTimeRef"]').val(data.olderTimeRef);
			if(data.remainingCommentCount == 0){
				$(targetComp).children('a').hide();	
			}else{
				$(targetComp).children('a').text("Show " + data.remainingCommentCount + " older comments >>");
			}
			feedPostProcess();
		}, error: function(xhr, status, error) {
			alert(xhr.responseText);
	   	}
	});
}

function replyOnComment(comp, parentId, url){
	var params = {};
	params["commentBody"] = $(comp).siblings(".comment-textbox").val();
	params["parentId"] = parentId;
	
	if($.trim(params["commentBody"]) === ""){
		$(comp).prev().addClass('comment-textEmpty').next('span').show();
		return false;
	}
	
	computeUserTag($(comp).parent());
	params["tagUserId"] = $(comp).siblings(".tagUserId").val();

	var options = {
 		url: url,
		dataType: "json",
		data: params,
		type: 'POST',
		success: function(data) {
			if(data.status == 401) {
				$.ajax(options);
			}else if(data.status === 'Error'){
        		alert(data.msg);
        	}
			else if(data.success){
				$(comp).parent().hide().prev().show();
				updateFeeds();
			}
		},
		error:function (xhr, ajaxOptions, thrownError){
        	//successHandler is used when ajax login succedes
        	var successHandler = this.success, errorHandler = null;
        	handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
		}
	}
	$.ajax(options);
}
 

function computeUserTag(postComp){
	var tagUserId = [];	
	if($(postComp).find('.tagUsers').size() > 0){
		$(postComp).find('.tagUsers').each(function(){
			tagUserId.push($(this).attr('rel'));
			$(this).removeClass('tagUsers');
		});	
		$(postComp).find('.tagUserId').val(tagUserId);
	}
}



function stripTags(source,destination){  
  destination.html(source.html().replace(/<\/?([b-z]+)[^>]*>/gi, function(match, tag) {
    if(tag === "br"){
        return match;
    }else if(tag === "a"){ 
        return match;
    }else{
        return "";
    }
    
}));
  
}  

$(document).ready(function()
{
    $('.comment-textbox').hide();
    $('.comment-textbox').after('<div class="commentContainer"><div class="contentbox" contenteditable="true"></div><div class="display"></div><div class="msgbox"></div></div><input type="hidden" name="tagUserId" class="tagUserId" value="" />');
    var start=/@/ig;
    var word=/@(\w+)/ig;

    $(".contentbox").live("keyup",function() 
{
    //stripTags($(this));
    var content=$(this).text();
    var go= content.match(start);
    var name= content.match(word);
    var dataString = 'searchword='+ name;
    var dataString = content.substring(content.indexOf('@') +1);
    var contentbox = $(this);
    if(go != null)
    {
        if(go.length>0){
       // contentbox.parent().find(".msgbox").slideDown('show');
       // $(this).parent().find(".display").slideUp('show');
       // $(this).parent().find(".msgbox").html("Type the name of someone or something...");
        if(name.length>0)
        {
            $.ajax({
            type: "POST",
            url: "/user/terms?term="+dataString,
            cache: false,
            success: function(html)
            {
                contentbox.parent().find(".msgbox").hide();
                var output = '';
                $.each(html, function(index,value){
                    
                    output += '<div class="display_box" align="left">';
                    output += '<img src="/images/no-image.jpg" class="image"/>';
                    output += '<a href="javascript:void(0);" class="addname" id="'+value.userId+'" title="'+value.label+'">';
                    output += value.label+'</a><br/>';
                    output +='</div>';
                });
               // console.log(output);    
                contentbox.parent().find(".display").html(output).show();
            }
            });
        }
        }
    }else{       
       //contentbox.parent().parent().find('.comment-textbox').html($(this).html());
       stripTags($(this),contentbox.parent().parent().find('.comment-textbox'))
    }

   // console.log(contentbox.parent().parent().val());
    return false;
});

$(".addname").live("click",function() 
{
    var username   = $(this).attr('title');
    var userId     = $(this).attr('id');
    var contentbox = $(this).parent().parent().parent().find(".contentbox");
    var comment_textbox = contentbox.parent().parent().find('.comment-textbox');
    stripTags(contentbox,comment_textbox);    
    var old = contentbox.html();
    var content=old.replace(word,""); 
    contentbox.html(content);
    var E="<a class='red tagUsers' contenteditable='false' style='color: #cc0000;font-weight: bold;' href='"+window.location.origin+"/user/show/"+userId+"' rel="+userId+" target='_blank' >"+username+"</a>&nbsp;";
    contentbox.append(E);
    //console.log(contentbox.html());
    //contentbox.parent().find('.comment-textbox').html(contentbox.html());
    stripTags(contentbox,comment_textbox);
    //console.log(comment_textbox.html());

    contentbox.parent().find(".display").hide();
    contentbox.parent().find(".msgbox").hide();
    //console.log(contentbox);
    contentbox.focus();
});
});

