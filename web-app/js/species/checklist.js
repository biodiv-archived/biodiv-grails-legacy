function showChecklistMapView() {
	$('#checklist_list_map').slideToggle(function() {
	
		if ($(this).is(':hidden')) {
			//$('div.checklist_list_main > div.checklist_list').show();
			$('#map_view_bttn').css('background-color', 'transparent');
			$('#map_results_list > div.checklist_list').remove();
			//updateGallery(undefined, undefined, 0, undefined, window.params.isGalleryUpdate);
			
		}
		/*
		else {
			//$('div.checklist_list_main > div.checklist_list').hide();
			//$('div.checklist_list_main > div.checklist_list').html('');
			//$('div.paginateButtons').hide();
		}
		*/
		google.maps.event.trigger(big_map, 'resize');
		big_map.setCenter(nagpur_latlng);
	});
}


function updateGallery(target, limit, offset, removeUser, isGalleryUpdate) {
	if(target === undefined) {
            target = window.location.pathname + window.location.search;
    }
    
    var a = $('<a href="'+target+'"></a>');
    var url = a.url();
    var href = url.attr('path');
    var params = getFilterParameters(url, limit, offset);
    isGalleryUpdate = (isGalleryUpdate == undefined)?true:isGalleryUpdate
    if(isGalleryUpdate)
    	params["isGalleryUpdate"] = isGalleryUpdate;
    var recursiveDecoded = decodeURIComponent($.param(params));
    
    var doc_url = href+'?'+recursiveDecoded;
    var History = window.History;
    delete params["isGalleryUpdate"]
    History.pushState({state:1}, "Species Portal", '?'+decodeURIComponent($.param(params))); 
    console.log("doc_url " + doc_url);
    if(isGalleryUpdate) {
       	$.ajax({
				url: doc_url,
				dataType: 'json',
				
				beforeSend : function(){
					$('div.checklist_list_main > div.checklist_list').css({"opacity": 0.5});
					//$('#tags_section').css({"opacity": 0.5});
				},
				
				success: updateListPage(params["tag"]),
				statusCode: {
				401: function() {
					show_login_dialog();	
				}	    				    			
			},
			error: function(xhr, status, error) {
				var msg = $.parseJSON(xhr.responseText);
				$('.message').html(msg);
			}
		});
	} else {
		window.location = doc_url;
	}
	
}

function updateListPage(activeTag) {
	return function (data) {
		$('.checklist_list_main').replaceWith(data.checklistListHtml);
		$('.info-message').replaceWith(data.checklistMsgtHtml);
		$('#map_results_list').replaceWith(data.checklistMapHtml);
		//$('#tags_section').replaceWith(data.tagsHtml);
		//setActiveTag(activeTag);
		eatCookies();
	}
}


   
