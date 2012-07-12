function showRecos(data, textStatus) {
	$('#recoSummary').html(data.recoHtml);
	var speciesName =  data.speciesName;
	$('.species_title').html(data.speciesNameTemplate);
	reloadCarousel($('#carousel_a').data('jcarousel'), 'speciesName', speciesName);
	var infoMsg = data.recoVoteMsg;
	showRecoUpdateStatus(infoMsg, 'success');
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
			//$(".deleteCommentIcon").tooltip('hide');
			showRecoUpdateStatus(data.success, 'success');
		},
		
		statusCode: {
			401: function() {
				show_login_dialog();
			}	    				    			
		},
		error: function(xhr, status, error) {
			//$(".deleteCommentIcon").tooltip('hide');
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
			preLoadRecos(3, false, obvId, liComponent);
			return false;
		},
		
		statusCode: {
			401: function() {
				show_login_dialog();
			}	    				    			
		},
		error: function(xhr, status, error) {
			handleError(xhr, status, error, this.success,showRecoUpdateStatus);
			return false;
		}
	});
}


