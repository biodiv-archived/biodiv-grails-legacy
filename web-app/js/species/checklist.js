function showChecklistMapView() {
	$('#observations_list_map').slideToggle(function() {
	
		if ($(this).is(':hidden')) {
			$('#map_view_bttn').css('background-color', 'transparent');
			$('#map_results_list > div.checklist_list').remove();
			
		}
		google.maps.event.trigger(big_map, 'resize');
		big_map.setCenter(nagpur_latlng);
	});
}
