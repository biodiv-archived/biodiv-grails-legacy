function showRecos(data, textStatus) {
	$('#recoSummary').html(data.recoHtml);
	var speciesName =  data.speciesName;
	$('.observation_story .species_title').replaceWith(data.speciesNameTemplate);
	$('.page-header .species_title').replaceWith(data.speciesNameTemplateForHeader);
	$('.species-external-link').replaceWith(data.speciesExternalLinkHtml);
	reloadCarousel($('#carousel_a').data('jcarousel'), 'speciesName', speciesName);
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
