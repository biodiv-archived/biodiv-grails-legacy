var newFeedProcessing = false;
var oldFeedProcessing = false;
var oldFeedRetry = 0;
var maxOldFeedRetry = 3;

function loadOlderFeedsInAjax(targetComp){
	var url = $(targetComp).children('input[name="feedUrl"]').val();
	var feedType = $(targetComp).children('input[name="feedType"]').val();
	var feedOrder = $(targetComp).children('input[name="feedOrder"]').val();
	
	$.ajax({
 		url: url,
		dataType: "json",
		data: getFeedParams("older", targetComp),
		success: function(data) {
			if(data.showFeedListHtml){
				var htmlData = $(data.showFeedListHtml);
				htmlData = removeDuplicateFeed($(targetComp).children('ul'), htmlData, feedType, "older", targetComp);
				if(feedOrder === "latestFirst"){
					htmlData.appendTo($(targetComp).children('ul')).hide().slideDown(1000);
				}else{
					htmlData.prependTo($(targetComp).children('ul')).hide().slideDown(1000);
				}
    			$(targetComp).children('input[name="olderTimeRef"]').val(data.olderTimeRef);
    			if(data.remainingFeedCount && data.remainingFeedCount > 0){
					$(targetComp).children('.activiyfeedoldermsg').text("Show " + ((feedType !== "GroupSpecific") ? data.remainingFeedCount: "") + " older feeds >>");
				}else{
					$(targetComp).children('.activiyfeedoldermsg').hide();
				}
				//if no new component added and still feeds availabel at server then calling loading again
				if((htmlData.val() === undefined) && (data.remainingFeedCount > 0) && (oldFeedRetry < maxOldFeedRetry) ){
					oldFeedRetry++;
					loadOlderFeedsInAjax(targetComp)
					return
				}
				
				feedPostProcess();
			}
			oldFeedProcessing = false;
			oldFeedRetry = 0;
		}, error: function(xhr, status, error) {
			oldFeedProcessing = false;
			oldFeedRetry = 0;
			alert(xhr.responseText);
	   	}
	});
}

function loadNewerFeedsInAjax(targetComp, checkFeed){
	
	var url = $(targetComp).children('input[name="feedUrl"]').val();
	var feedType = $(targetComp).children('input[name="feedType"]').val();
	var paramData =  getFeedParams("newer", targetComp);
	var feedOrder = $(targetComp).children('input[name="feedOrder"]').val();
	
	paramData["checkFeed"] = checkFeed;
	
	$.ajax({ 
     	url:url,
		dataType: 'json', 
		data: paramData,	
		success: function(data, statusText, xhr, form) {
			if(checkFeed){
				if(data.feedAvailable){
					newFeedProcessing = true;
					$(targetComp).children('.activiyfeednewermsg').show();
                                        $("#activityTicker").html("New");
				}
			}
			else if(data.showFeedListHtml){
				var refreshType = $(targetComp).children('input[name="refreshType"]').val();
        		if(refreshType == "auto"){
        			$(targetComp).hide().fadeIn(3000);
        			$(targetComp).children('.activiyfeednewermsg').hide();
        		}
    			var htmlData = $(data.showFeedListHtml);
    			htmlData = removeDuplicateFeed($(targetComp).children('ul'), htmlData, feedType, "newer", targetComp);
    			if(feedOrder === "latestFirst"){
    				htmlData.prependTo($(targetComp).children('ul')).hide().slideDown(1000);
			}else{
                                htmlData.appendTo($(targetComp).children('ul')).hide().slideDown(1000);
			}
    			$(targetComp).children('input[name="newerTimeRef"]').val(data.newerTimeRef);
    			newFeedProcessing = false;
    			feedPostProcess();
        	}
			
        	return false;
        },
        error:function (xhr, ajaxOptions, thrownError){
        	//successHandler is used when ajax login succedes
        	newFeedProcessing = false;
        	var successHandler = this.success, errorHandler = undefined;
        	handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
		} 
 	});
}

function removeDuplicateFeed(parentList, newList, feedType, feedTimeType, targetComp){
	//alert(feedType + "  " + feedTimeType);
	if(feedType === "Specific"){
		/*
		var isCommentThread = ($(targetComp).children('input[name="isCommentThread"]').val() == "true");
		alert("is comment " + isCommentThread);
		if(!isCommentThread || feedTimeType == "newer"){
			return newList
		}
		//removing the last component as it may be repeating
		var lastCompClass = $(newList).last().attr("class")
		alert("==== last comp class " + lastCompClass);
		if($(targetComp).siblings(".feedSubParentContext").hasClass(lastCompClass)){
			alert("hiding");
			$(newList).last().hide();
		}
		*/
		return newList
	}
	if(feedTimeType == "older"){
		var newListStr = ""
		$(newList).each(function(index) {
			var liClass = $(this).attr("class");
			if(liClass){
				if(feedType === "GroupSpecific" && liClass.match("^species.groups.UserGroup")){
					newListStr = newListStr + '<li class="'+ liClass +'">' + $(this).html() + '</li>';
				}else{
					var selector = 'li[class="' + liClass +  '"]'
					var dupEle = $(parentList).children(selector);
					if(dupEle.attr("class") == undefined){
						newListStr = newListStr + '<li class="'+ liClass +'">' + $(this).html() + '</li>';
					}	
				}
			}
		});
		return $(newListStr)
	}
	
	$(newList).each(function(index) {
		var liClass = $(this).attr("class");
		if(liClass){
			if(!(feedType === "GroupSpecific" && liClass.match("^species.groups.UserGroup"))){
				var selector = 'li[class="' + liClass +  '"]'
				$(parentList).children(selector).remove();
			}
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
	feedParams["feedCategory"] = $(targetComp).children('input[name="feedCategory"]').val();
	feedParams["feedClass"] = $(targetComp).children('input[name="feedClass"]').val();
	feedParams["feedPermission"] = $(targetComp).children('input[name="feedPermission"]').val();
	feedParams["feedOrder"] = $(targetComp).children('input[name="feedOrder"]').val();
	
	feedParams["isCommentThread"] = $(targetComp).children('input[name="isCommentThread"]').val();
	feedParams["subRootHolderId"] = $(targetComp).children('input[name="subRootHolderId"]').val();
	feedParams["subRootHolderType"] = $(targetComp).children('input[name="subRootHolderType"]').val();
	
	feedParams["feedHomeObjectId"] = $(targetComp).children('input[name="feedHomeObjectId"]').val();
	feedParams["feedHomeObjectType"] = $(targetComp).children('input[name="feedHomeObjectType"]').val();
	feedParams["webaddress"] = $(targetComp).children('input[name="webaddress"]').val();
	
	feedParams["refreshType"] = $(targetComp).children('input[name="refreshType"]').val();
	feedParams["timeLine"] = timeLine;
	if(timeLine === "newer"){
		feedParams["refTime"] = $(targetComp).children('input[name="newerTimeRef"]').val();
	}else{
		feedParams["refTime"] = $(targetComp).children('input[name="olderTimeRef"]').val();
	}
	feedParams["user"] = $(targetComp).children('input[name="user"]').val();
	return feedParams;
}

function setUpFeedForTarget(targetComp){
	if(targetComp === null){
		return; 
	}
	
	var refreshType = $(targetComp).children('input[name="refreshType"]').val();
	if(refreshType !== "auto"){
		//for manual refresh will add feeds during page creation and not in ajax call
		return
	}
	
	//resetting time range
	setUpTimeRef(targetComp);
	
	loadOlderFeedsInAjax(targetComp); // to load some feeds to start with
	autoLoadOnScroll(targetComp); // to get older feeds on scroll bottom
	pollForFeeds(targetComp); //to get newer feeds
}

function setUpFeed(timeUrl){
	initRelativeTime(timeUrl);
	setUpFeedForTarget(getTargetComp());
}


function setUpTimeRef(targetComp){
	var refTime = new Date().getTime()
	$(targetComp).children('input[name="newerTimeRef"]').val(refTime);
	$(targetComp).children('input[name="olderTimeRef"]').val(refTime);
}

//on user click fetch
function pollForFeeds(targetComp){
	window.setInterval(function(){
		if(!newFeedProcessing){
			loadNewerFeedsInAjax(targetComp, true);
		}
	}, 20000);
} 

// on automatic fetch
//function pollForFeeds(targetComp){
//	window.setInterval(function(){
//		if($(window).scrollTop() < 250 ){
//			loadNewerFeedsInAjax(targetComp, false);
//		}
//	}, 2000);
//} 

function autoLoadOnScroll(targetComp){
	$(window).scroll(function() {
		if(oldFeedProcessing){
			return false;
		}
		if($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
			oldFeedProcessing = true;
			loadOlderFeedsInAjax(targetComp);
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

	targetComp = $('.activityfeedMyFeeds');
	if(targetComp.length > 0){
		return targetComp;
	}

	targetComp = $('.activityfeedGroupSpecific');
	if(targetComp.length > 0){
		return targetComp;
	}
		
	targetComp = $('.activityfeedSpecific');
	if(targetComp.length == 1){
		return targetComp;
	}
        
        targetComp = $('.activityfeedUser');
	if(targetComp.length == 1){
		return targetComp;
	}
	return null;
}

function updateFeeds(){
	var targetComp = getTargetComp();
	if(targetComp){
		var refreshType = $(targetComp).children('input[name="refreshType"]').val();
		if(refreshType == "manual"){
			loadNewerFeedsInAjax(targetComp, false);	
		}else{
			//on auto refresh forcing to check new feeds right away
			if(!newFeedProcessing){
				loadNewerFeedsInAjax(targetComp, true);	
			}
		}
	}
}

function updateFeedComponent(targetComp, feedCategory){
	 $(targetComp).children('input[name="feedCategory"]').val(feedCategory);
	 setUpTimeRef(targetComp);
	 $(targetComp).children('ul').empty();
	 loadOlderFeedsInAjax(targetComp);
}

$('.feed_filter_label').click(function(){
	var caret = '<span class="caret"></span>'
	if($.trim(($(this).html())) == $.trim($("#feedFilterButton").html().replace(caret, ''))){
		return true;
	}
	$('.feed_filter_label.active').removeClass('active');
	$(this).addClass('active');
    $("#feedFilterButton").html($(this).html() + caret);
    var feedCategory =  $(this).attr("value");
    var targetComp =  $(this).closest(".feedFilterDiv").next(".activityfeed");
    updateFeedComponent(targetComp, feedCategory);
    return true;   
});


function followObject(className, id, comp, url){
	var doFollow = ($(comp).text() == 'Unfollow') ? false : true;
	$.ajax({
		url: url,
		data:{'className':className, 'id':id, 'follow':doFollow},
		
		success: function(data){
			if(data.status == 'success') {
				$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				$("html, body").animate({ scrollTop: 0 });
				toggleFollowButton(comp);
			}
			return false;
		},
		
		error:function (xhr, ajaxOptions, thrownError){
			//successHandler is used when ajax login suceedes
        	var successHandler = this.success, errorHandler = null;
        	handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
		} 
	});
}

function toggleFollowButton(comp){
	if(($(comp).text() == 'Unfollow')){
		$(comp).html('<i class="icon-play-circle"></i>Follow')
	}else{
		$(comp).html('<i class="icon-play-circle"></i>Unfollow')
	}
}

function setFollowButton(){
	var comp = $("#followButton");
	if(($(comp).text() == 'Follow')){
		$(comp).html('Unfollow')
	}
}
