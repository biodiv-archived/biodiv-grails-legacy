function editCommentActivity(targetComp, commentId){
	var that = $(targetComp);
	var message_body = that.parent().find('.yj-message-body');
	var commentVal   = that.parent().find('.yj-message-body').html();
	that.hide();

	var output  = '<div class="editCommentWrapper" id='+commentId+'><textarea name="commentBody" class="comment-textbox" placeholder="Write comment" style="display: none;"></textarea>';
		output += '<div class="commentContainer"><div class="contentbox" contenteditable="true">'+commentVal+'</div><div class="display"></div><div class="msgbox"></div></div><input type="hidden" name="tagUserId" class="tagUserId" value="" />';
		output += '<a href="javascript:void(0);" class="btn btn-mini pull-right cancelComment" title="'+window.i8ln.species.specie.bcanc+'" >'+window.i8ln.species.specie.bcanc+'</a>';
		output += '<a href="javascript:void(0);" class="btn btn-mini pull-right updateComment" title="'+window.i8ln.species.specie.bcmnt+'" >'+window.i8ln.species.specie.bupdate+'</a>';
		output += '</div>';
	message_body.hide().after(output);

}

$('.cancelComment').live('click',function(){	
	showComment($(this));	
});

$('.updateComment').live('click',function(){
	var that = $(this);
	var editCommentWrapper = that.parent();
	var message_body = that.parent().parent().find('.yj-message-body');
	var params = {}
	params['commentBody'] = editCommentWrapper.find('.comment-textbox').text();
	params['commentId']   = editCommentWrapper.attr('id');
	computeUserTag(editCommentWrapper);
	params['tagUserId']   = editCommentWrapper.find('.tagUserId').val();
	loaderFun(that,true,"Updating",'anchor');	
	if(params['commentBody'] != ''){
		var message_body_cache = message_body.html();
		message_body.html(params['commentBody']);
		showComment(that);
		$.post('/comment/addComment',params,function(result){			
				if(!result.success){
					alert("Error While Uploading");
					console.log(result);
					message_body.html(message_body_cache);
					loaderFun(that,false,"Update",'anchor');

				}
		});
	}else{
		showComment(that);
		loaderFun(that,false,"Update",'anchor');
	}
});	

function showComment(that){
	that.parent().parent().find('.yj-message-body').show();
	that.parent().parent().find('.reco-comment-edit, .yj-attributes').show();	
	that.parent().remove();
}

function deleteCommentActivity(targetComp, commentId, url){
    if(confirm(window.i8ln.species.parseUtil.comment)){
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
	var submitButton = $(postComp).children('input[type="submit"]');
	var textComp = $(postComp).children('textarea[name="commentBody"]');
	var contentbox = $(textComp).next().find('.contentbox');
	if($.trim(textComp.text()) === ""){
		contentbox.addClass('comment-textEmpty');
		//$(textComp).next('span').show();
		return false;
	}
	loaderFun(submitButton,true,window.i8ln.text.posting,'input');//submitButton.attr('disabled',true).attr('value','posting');
	computeUserTag(postComp);
	postAsAjax(postComp, url, newCommentUrl, true);
	
	contentbox.removeClass('comment-textEmpty');
	//$(textComp).next('span').hide();
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
			$.each(formData, function( index, value ) {			  
			  	if(value.name == "commentBody"){
			  		value.value = $(postComp).children('textarea[name="commentBody"]').text();			  		
			  	}
			});
			console.log(formData);			
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
    				var submitButton = $(postComp).children('input[type="submit"]');
					loaderFun(submitButton,false,window.i8ln.text.post,'input'); //	submitButton.attr('disabled',false).attr('value','Post');
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
	params["commentBody"] = $(comp).siblings(".comment-textbox").text();
	params["parentId"] = parentId;
	
	if($.trim(params["commentBody"]) === ""){
		$(comp).prev().addClass('comment-textEmpty').next('span').show();
		return false;
	}
	loaderFun($(comp),true,window.i8ln.text.posting,'anchor'); //$(comp).attr('disabled',true).attr('value','posting');
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
				loaderFun($(comp),false,window.i8ln.text.post,'anchor'); //$(comp).attr('disabled',false).attr('value','Post');
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
//console.log(source.html());
destination.html(source.html().replace(/<\/?([b-z]+)[^>]*>/gi, function(match, tag) {
    if(tag === "br"){
        return match;
    }else if(tag === "a"){ 
        return match;
    }else{
        return "";
    }
}));
destination.html(destination.html().replace(/&lt;\/?([b-z]+)*&gt;/gi, function(match, tag) {  	
    if(tag === "br"){
        return match;
    }else if(tag === "a"){ 
        return match;
    }else{
        return "";
    }
}));  

destination.html(destination.html().replace(/&lt;\/?([a]+)[^>]*&gt;/gi, function(match, tag) {  	
    if(tag === "a"){ 
    	match.replace(/&lt;\/?([a-z]+)*&gt;/gi, function(match, tag) {    		
    			return "";
    	});
        return match;
    }else{
        return "";
    }
}));    
  //console.log(destination.html());
}   

function appendCommentWrapper(that){	
	that.after('<div class="commentContainer"><div class="contentbox" contenteditable="true"></div><div class="display"></div><div class="msgbox"></div></div><input type="hidden" name="tagUserId" class="tagUserId" value="" />');
	that.hide();
	that.next().find('.contentbox').focus();
}

function loaderFun(that,boolVal,msgValue,Ele){
	if(Ele == 'input'){
	 	that.attr('disabled',boolVal).attr('value',msgValue);
	}else if(Ele == 'anchor'){
		that.attr('disabled',boolVal).html(msgValue);
	}
}	 

function placeCaretAtEnd(el) {
   el.focus();
   if (typeof window.getSelection != "undefined"
           && typeof document.createRange != "undefined") {
       var range = document.createRange();
       range.selectNodeContents(el);
       range.collapse(false);
       var sel = window.getSelection();
       sel.removeAllRanges();
       sel.addRange(range);
   } else if (typeof document.body.createTextRange != "undefined") {
       var textRange = document.body.createTextRange();
       textRange.moveToElementText(el);
       textRange.collapse(false);
       textRange.select();
   }
}

$(document).on('focus','.comment-textbox',function(){
	if(!$(this).hasClass('noComment')){	
		appendCommentWrapper($(this));
	}
});

$(document).ready(function()
{
    //appendCommentWrapper($('.comment-textbox'));    
    var start=/@/ig;
    var word=/@(\w+)/ig ;
    var word2=/@(\w+\s\w+)/ig ;

$(".contentbox").live("keyup",function() 
{
    //stripTags($(this));
    var content=$(this).text();
    var go= content.match(start);
    var name= content.match(word);
    var name2= content.match(word2);
    if(name2 !== null){
    	name=name2;
    }
    var dataString = 'searchword='+ name;
    var dataString = content.substring(content.indexOf('@') +1);
    var contentbox = $(this);
    if(go !== null)
    {
        if(go.length>0){
       // contentbox.parent().find(".msgbox").slideDown('show');
       // $(this).parent().find(".display").slideUp('show');
       // $(this).parent().find(".msgbox").html("Type the name of someone or something...");
       //console.log("name ="+name);
	       if(name){
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
			                    
			                    output += '<div class="display_box addname" style="cursor:pointer;" align="left" id="'+value.userId+'" title="'+value.label+'">';
			                    output += '<img src="'+value.user_pic+'" class="image"/>';
			                    output += '<a href="javascript:void(0);" id="'+value.userId+'" title="'+value.label+'">';
			                    output += value.label+'</a><br/>';
			                    output +='</div>';
			                });
			               // console.log(output);    
			                contentbox.parent().find(".display").html(output).show();
			            }
			            });
			        }
	    	}
        }
    }else{       
       //contentbox.parent().parent().find('.comment-textbox').html($(this).html());
         contentbox.parent().find(".display").hide();
	    contentbox.parent().find(".msgbox").hide();
       stripTags($(this),contentbox.parent().parent().find('.comment-textbox'))
    }

   // console.log(contentbox.parent().parent().val());
    return false;
});
/*
$(".contentbox").live("focusout",function() {

	$(this).parent().find('.display').hide();

});
*/

$(".addname").live("click",function() 
{
    var username   = $(this).attr('title');
    var userId     = $(this).attr('id');
    var contentbox = $(this).parent().parent().find(".contentbox");
    var comment_textbox = contentbox.parent().parent().find('.comment-textbox');
    //console.log("comment_textbox ="+$(this).parent().parent().html());
    //console.log("comment_textbox111 ="+comment_textbox.html());
    stripTags(contentbox,comment_textbox);    
    var old = contentbox.html();
    var name2= old.match(word2);
    if(name2 !== null){
    	word=word2;
    }
    var content=old.replace(word,""); 
    contentbox.html(content);
    var E="<a class='red tagUsers' contenteditable='false'  href='"+window.location.origin+"/user/show/"+userId+"' rel="+userId+" target='_blank' >"+username+"</a>&nbsp;";
    contentbox.append(E);
    //console.log("Before ="+contentbox.html());
    //contentbox.parent().find('.comment-textbox').html(contentbox.html());
    stripTags(contentbox,comment_textbox);
    //console.log("After ="+comment_textbox.html());

    contentbox.parent().find(".display").hide();
    contentbox.parent().find(".msgbox").hide();
    //console.log(contentbox);
   // contentbox.focus();
   placeCaretAtEnd(contentbox.get(0));
});
});

