var oldFeedProcessing = false;

function loadOlderFeedsInAjax(targetComp, url, feedType){
	$.ajax({
 		url: url,
		dataType: "json",
		data: getFeedParams("older", targetComp),
		success: function(data) {
			if(data.showFeedListHtml){
				var htmlData = $(data.showFeedListHtml);
				dcorateCommentBody(htmlData.find('.yj-message-body'));
				htmlData = removeDuplicateFeed($(targetComp).children('ul'), htmlData, feedType, "older");
    			$(targetComp).children('ul').append(htmlData);
				$(targetComp).children('input[name="olderTimeRef"]').val(data.olderTimeRef);
				updateRelativeTime(data.currentTime);
				if(data.remainingFeedCount && data.remainingFeedCount > 0){
					$(targetComp).children('a').text("Show " + data.remainingFeedCount + " older feeds >>");
				}else{
					$(targetComp).children('a').hide();
				}
			}
			oldFeedProcessing = false;
		}, error: function(xhr, status, error) {
			oldFeedProcessing = false;
			alert(xhr.responseText);
	   	}
	});
}

function loadNewerFeedsInAjax(targetComp, url, feedType){
	$.ajax({ 
     	url:url,
		dataType: 'json', 
		data: getFeedParams("newer", targetComp),	
		success: function(data, statusText, xhr, form) {
        	if(data.showFeedListHtml){
        		var refreshType = $(targetComp).children('input[name="refreshType"]').val();
        		if(refreshType == "auto"){
        			$(targetComp).hide().fadeIn(3000);
        		}
    			var htmlData = $(data.showFeedListHtml);
    			dcorateCommentBody(htmlData.find('.yj-message-body'));
    			htmlData = removeDuplicateFeed($(targetComp).children('ul'), htmlData, feedType, "newer");
    			$(targetComp).children('ul').prepend(htmlData);
    			$(targetComp).children('input[name="newerTimeRef"]').val(data.newerTimeRef);
    			updateRelativeTime(data.currentTime);
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

function removeDuplicateFeed(parentList, newList, feedType, feedTimeType){
	if(feedType === "Specific"){
		return newList
	}
	if(feedTimeType == "older"){
		var newListStr = ""
		$(newList).each(function(index) {
			var liClass = $(this).attr("class");
			if(liClass){
				var selector = 'li[class="' + liClass +  '"]'
				var dupEle = $(parentList).children(selector);
				if(dupEle.attr("class") == undefined){
					newListStr = newListStr + '<li class="'+ liClass +'">' + $(this).html() + '</li>';
				}
			}
		});
		return $(newListStr)
	}
	
	$(newList).each(function(index) {
		var liClass = $(this).attr("class");
		if(liClass){
			var selector = 'li[class="' + liClass +  '"]'
			$(parentList).children(selector).remove();
		}
		
	});
	return newList;
}


function getFeedParams(timeLine, targetComp){
	var feedParams = {}; 
	
	feedParams["rootHolderId"] = $(targetComp).children('input[name="rootHolderId"]').val();
	feedParams["rootHolderType"] = $(targetComp).children('input[name="rootHolderType"]').val();
	feedParams["activityHolderId"] = $(targetComp).children('input[name="activityHolderId"]').val();
	feedParams["activityHolderType"] = $(targetComp).children('input[name="activityHolderType"]').val();
	feedParams["feedType"] = $(targetComp).children('input[name="feedType"]').val();
	feedParams["feedPermission"] = $(targetComp).children('input[name="feedPermission"]').val();
	
	feedParams["refreshType"] = $(targetComp).children('input[name="refreshType"]').val();
	feedParams["timeLine"] = timeLine;
	if(timeLine === "newer"){
		feedParams["refTime"] = $(targetComp).children('input[name="newerTimeRef"]').val();
	}else{
		feedParams["refTime"] = $(targetComp).children('input[name="olderTimeRef"]').val();
	}
	return feedParams;
}

function setUpFeedForTarget(targetComp){
	if(targetComp === null){
		return; 
	}
	
	var feedType = $(targetComp).children('input[name="feedType"]').val();
	var refreshType = $(targetComp).children('input[name="refreshType"]').val();
	var url = $(targetComp).children('input[name="feedUrl"]').val();
	
	if(refreshType === "auto"){
		pollForFeeds(targetComp, url, feedType); //to get newer feeds
		autoLoadOnScroll(targetComp, url, feedType); // to get older feeds on scroll bottom
		loadOlderFeedsInAjax(targetComp, url, feedType); // to load some feeds to start with
	}
}

function setUpFeed(){
	$('body').timeago();
	setUpFeedForTarget(getTargetComp());
}

function pollForFeeds(targetComp, url, feedType){
	window.setInterval(function(){
		if($(window).scrollTop() < 250 ){
			loadNewerFeedsInAjax(targetComp, url, feedType);
		}
	}, 15000);
} 

function autoLoadOnScroll(targetComp, url, feedType){
	$(window).scroll(function() {
		if(oldFeedProcessing){
			return false;
		}
		if($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
			oldFeedProcessing = true;
			loadOlderFeedsInAjax(targetComp, url, feedType);
		}
	});	
}

function removeActivity(targetComp){
	$(targetComp).closest('.activityFeed-container').parent().remove();
}

function getTargetComp(){
	var targetComp = $('.activityfeedAll');
	if(targetComp.length > 0){
		return targetComp;
	}
	
	targetComp = $('.activityfeedGeneric');
	if(targetComp.length > 0){
		return targetComp;
	}
	
	targetComp = $('.activityfeedSpecific');
	if(targetComp.length == 1){
		return targetComp;
	}
	return null;
}

function updateFeeds(){
	var targetComp = getTargetComp();
	if(targetComp){
		var feedType = $(targetComp).children('input[name="feedType"]').val();
		var url = $(targetComp).children('input[name="feedUrl"]').val();
		var refreshType = $(targetComp).children('input[name="refreshType"]').val();
		if(refreshType == "manual"){
			loadNewerFeedsInAjax(targetComp, url, feedType);	
		}
	}
}

