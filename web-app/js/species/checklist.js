function showChecklistMapView() {
	$('#checklist_list_map').slideToggle(function() {
	
		if ($(this).is(':hidden')) {
			$('div.checklist_list_main > div.checklist_list').show();
			$('#map_view_bttn').css('background-color', 'transparent');
			$('#map_results_list > div.checklist_list').remove();
			updateGallery(undefined, undefined, 0, undefined, window.params.isGalleryUpdate);
			
		} else {
			$('div.checklist_list_main > div.checklist_list').hide();
			$('div.checklist_list_main > div.checklist_list').html('');
			$('div.paginateButtons').hide();
		}
		google.maps.event.trigger(big_map, 'resize');
		big_map.setCenter(nagpur_latlng);
	});
}

