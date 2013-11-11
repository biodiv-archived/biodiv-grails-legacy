
function refreshMarkers(p, url, callback) {
    if(!p) p = new Array()

    p['fetchField'] = "id,latitude,longitude,isChecklist,geoPrivacy";
    p['max'] = -1;
    delete p['bounds']
    
    if(!url) url = window.params.observation.occurrencesUrl+'?'+decodeURIComponent($.param(p));
    else url = url+'?'+decodeURIComponent($.param(p));

    if(markers) {
        markers.clearLayers();
    } else 
        markers = new M.MarkerClusterGroup({maxClusterRadius:50});

    $.ajax({
        url: url,
        dataType: "json",
        success: function(data) {
            var m = [];
            for(var i=0; i<data.observations.length; i++) {
                var obv = data.observations[i];
                var latitude = obv.lat?obv.lat:obv[1];
            	var longitude = obv.lng?obv.lng:obv[2];
            	var icon;
                
                /*if(obv.geoPrivacy || obv[4]){
                	icon = (obv.isChecklist || obv[3])?geoPrivacyChecklistIcon:geoPrivacyPointIcon;
                	latitude += data.geoPrivacyAdjust;
                	longitude += data.geoPrivacyAdjust;
                } else{*/
                	icon = (obv.isChecklist || obv[3])?checklistIcon:pointIcon;
                //}
                var marker = createMarker(latitude, longitude, {
                    draggable: false,
                    clusterable: true,
                    icon:icon,
                    clickable:load_content,
                    data:{id:(obv.id?obv.id:obv[0])}
                });
                if(marker) m.push(marker);
            }
            markers.addLayers(m);
            markers.addTo(map);
            if(callback)
                callback(data);
        }
    });
}



