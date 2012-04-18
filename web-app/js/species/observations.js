function showRecos(data, textStatus) {
	jQuery('#recoSummary').html(jQuery(data).find('recoHtml').text());
	var speciesName =  jQuery(data).find('speciesName').text();
	jQuery('.species_title').html(jQuery(data).find('speciesNameTemplate').text());
	reloadCarousel($('#carousel_a').data('jcarousel'), 'speciesName', speciesName);
	var infoMsg = jQuery(data).find('recoVoteMsg').text();
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
