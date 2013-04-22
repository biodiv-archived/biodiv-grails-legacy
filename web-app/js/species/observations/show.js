function showRecos(data, textStatus) {
        if(textStatus && textStatus == 'append')
        	$('#recoSummary').append(data.recoHtml);
        else
        	$('#recoSummary').html(data.recoHtml);
	var speciesName =  data.speciesName;
	$('.species_title').replaceWith(data.speciesNameTemplate);
	$('.page-header .species-page-link').hide();
	$('.species-external-link').replaceWith(data.speciesExternalLinkHtml);
        if($('#carousel_a').length > 0) {
    	    reloadCarousel($('#carousel_a').data('jcarousel'), 'speciesName', speciesName);
        }
	showRecoUpdateStatus(data.msg, data.status);
}

function showRecoUpdateStatus(msg, type) {
	if(!msg) return;
	
	if(type === 'info') {
		$("#seeMoreMessage").html(msg).show().removeClass().addClass('alert alert-info');
	} else if(type === 'success') {
		$("#seeMoreMessage").html(msg).show().removeClass().addClass('alert alert-success');
	} else if(type === 'error') {
		$("#seeMoreMessage").html(msg).show().removeClass().addClass('alert alert-error');
	} else {
		$("#seeMoreMessage").hide();
	}
}

function removeRecoComment(recoVoteId, commentDivId, url, commentComp){
	$.ajax({
		url: url,
		data:{"id":recoVoteId},
		
		success: function(data){
			if($(commentDivId + ' li').length > 1){
				commentComp.remove();
			}else{
				$(commentDivId).remove(); 
			}
			// $(".deleteCommentIcon").tooltip('hide');
			showRecoUpdateStatus(data.success, 'success');
		},
		
		statusCode: {
			401: function() {
				show_login_dialog();
			}	    				    			
		},
		error: function(xhr, status, error) {
			// $(".deleteCommentIcon").tooltip('hide');
			var msg = $.parseJSON(xhr.responseText);
			showRecoUpdateStatus(msg.error, 'error');
		}
	});
}

function addAgreeRecoVote(obvId, recoId, currentVotes, liComponent, url){
	$.ajax({
		url: url,
		data:{'obvId':obvId, 'recoId':recoId, 'currentVotes':currentVotes},
		
		success: function(data){
			if(data.status == 'success') {
				if(data.canMakeSpeciesCall === false){
	         		$('#selectedGroupList').modal('show');
	         	} else {
	         		preLoadRecos(3, false, obvId, liComponent);
	         		updateFeeds();
	         		setFollowButton();
	         		showRecoUpdateStatus(data.msg, data.status);
	         	}
			} else {
				showRecoUpdateStatus(data.msg, data.status);
			}
			return false;
		},
		
		error:function (xhr, ajaxOptions, thrownError){
			//successHandler is used when ajax login succedes
        	var successHandler = this.success, errorHandler = showRecoUpdateStatus;
        	handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
		} 
	});
}

function removeRecoVote(obvId, recoId, url){
	$.ajax({
		url: url,
		data:{'obvId':obvId, 'recoId':recoId},
		
		success: function(data){
			if(data.status == 'success') {
				preLoadRecos(3, false, obvId);
	         	updateFeeds();
	         	setFollowButton();
	         	showRecoUpdateStatus(data.msg, data.status);
			} else {
				showRecoUpdateStatus(data.msg, data.status);
			}
			return false;
		},
		
		error:function (xhr, ajaxOptions, thrownError){
			//successHandler is used when ajax login suceedes
        	var successHandler = this.success, errorHandler = showRecoUpdateStatus;
        	handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
		} 
	});
	
}

function preLoadRecos(max, offset, seeAllClicked) {
    $("#seeMoreMessage").hide();
    $("#seeMore").hide();

    $.ajax({
        url: window.params.observation.getRecommendationVotesURL,
        method: "POST",
        dataType: "json",
        data: {max:max , offset:offset},	
        success: function(data) {
            if(data.status == 'success') {
                if(offset>0)
                    showRecos(data, 'append');
                else
                    showRecos(data, null);
                //$("#recoSummary").html(data.recoHtml);
                var uniqueVotes = parseInt(data.uniqueVotes);
                if(uniqueVotes > offset+max){
                    $("#seeMore").show();
                } else {
                    $("#seeMore").hide();
                }
                showRecoUpdateStatus(data.msg, data.status);
            } else {
                showRecoUpdateStatus(data.msg, data.status);
            }
        }, error: function(xhr, status, error) {
            handleError(xhr, status, error, undefined, function() {
                var msg = $.parseJSON(xhr.responseText);
                showRecoUpdateStatus(msg.msg, msg.status);
            });
        }
    });
}
 
