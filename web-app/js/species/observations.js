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
