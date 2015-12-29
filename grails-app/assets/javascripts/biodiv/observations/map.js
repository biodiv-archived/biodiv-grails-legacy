
function refreshMarkers(p, url, callback, mapLocationPicker) {
    if(!p) p = new Array()

    p['fetchField'] = "id,latitude,longitude,isChecklist,geoPrivacy";
    p['max'] = -1;
    //delete p['bounds']
    
    if(!url) url = window.params.observation.occurrencesUrl+'?'+decodeURIComponent($.param(p));
    else url = url+'?'+decodeURIComponent($.param(p));

    if(mapLocationPicker.markers) {
        mapLocationPicker.markers.clearLayers();
    } else 
        mapLocationPicker.markers = new mapLocationPicker.M.MarkerClusterGroup({maxClusterRadius:50});
    $.ajax({
        url: url,
        dataType: "json",
        success: function(data) {
            var m = [];
            for(var i=0; i<data.model.observations.length; i++) {
                var obv = data.model.observations[i];
                var latitude = obv.lat?obv.lat:obv[1];
            	var longitude = obv.lng?obv.lng:obv[2];
            	var icon;
                
                if(obv.geoPrivacy){
                	latitude += obv.geoPrivacyAdjust;
                	longitude += obv.geoPrivacyAdjust;
                }
                var marker = mapLocationPicker.createMarker(latitude, longitude, {
                    draggable: false,
                    clusterable: true,
                    icon:icon,
                    clickable:load_content,
                    data:{id:(obv.id?obv.id:obv[0])}
                });
                if(marker) m.push(marker);
            }
            mapLocationPicker.markers.addLayers(m);
            mapLocationPicker.markers.addTo(mapLocationPicker.map);
            if(callback)
                callback(data);
        }
    });
}



