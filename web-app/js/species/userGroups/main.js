/**
 * 
 */
function reloadActionsHeader() {
	$.ajax({
        	url: window.reloadActionsHeaderUrl,
        	 method: "POST",
            dataType: "html",
            success: function(data) {
            	$("#actionsHeader").html(data);
            }
    });
}


$('.home_page_label').click(function(){
	var caret = '<span class="caret"></span>'
	if($.trim(($(this).html())) == $.trim($("#homePageSelector").html().replace(caret, ''))){
		return true;
	}
	$('.home_page_label.active').removeClass('active');
	$(this).addClass('active');
	var homePageVal = $(this).attr("value");
	$(this).closest(".prop").children('input[name="homePage"]').val(homePageVal);
    $("#homePageSelector").html($(this).html() + caret);
    return true;   
});

$('.theme_label').click(function(){
	var caret = '<span class="caret"></span>'
	if($.trim(($(this).html())) == $.trim($("#themeSelector").html().replace(caret, ''))){
		return true;
	}
	$('.theme_label.active').removeClass('active');
	$(this).addClass('active');
	var themeVal = $(this).attr("value");
	$(this).closest(".prop").children('input[name="theme"]').val(themeVal);
    $("#themeSelector").html($(this).html() + caret);
    return true;   
});


function getSelectedVal(labelClass) {
	var retVal = ''; 
	$('.' + labelClass).each (function() {
		if($(this).hasClass('active')) {
			retVal += $(this).attr('value') + ',';
        }
	});
	retVal = retVal.replace(/\s*\,\s*$/,'');
	return retVal;
} 

function loadUserGroupLocation (ne_latitude, ne_longitude, sw_latitude, sw_longitude) { 
    loadGoogleMapsAPI(function() {
        var swRestriction = new google.maps.LatLng('8', '69');
        var neRestriction = new google.maps.LatLng('36', '98');
        var allowedBounds = new google.maps.LatLngBounds(swRestriction, neRestriction);

        var mapOptions = {
            center: new google.maps.LatLng(21.07,79.27),
        zoom: 4,
        mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var map = new google.maps.Map(document.getElementById('map_canvas'),
            mapOptions);

        google.maps.event.addListener(map, 'dragend', function() { checkBounds(); });

        var rectangle = new google.maps.Rectangle();
        rectangle.setMap(map);

        var initSW = new google.maps.LatLng(sw_latitude, sw_longitude); 
        var initNE = new google.maps.LatLng(ne_latitude, ne_longitude); 
        var initRect = new google.maps.LatLngBounds(initSW, initNE);
        rectangle.setOptions({
            bounds: initRect,
            editable: false,
            fillOpacity: 0.1,
            fillColor: '#cc0000',
            strokeWeight: 1
        });

        function checkBounds() {
            if (allowedBounds.contains(map.getCenter())) return;

            var c = map.getCenter(),
                x = c.lng(),
                y = c.lat(),
                maxX = allowedBounds.getNorthEast().lng(),
                maxY = allowedBounds.getNorthEast().lat(),
                minX = allowedBounds.getSouthWest().lng(),
                minY = allowedBounds.getSouthWest().lat();

            if (x < minX) x = minX;
            if (x > maxX) x = maxX;
            if (y < minY) y = minY;
            if (y > maxY) y = maxY;

            map.setCenter(new google.maps.LatLng(y, x));
        }

    })
}


