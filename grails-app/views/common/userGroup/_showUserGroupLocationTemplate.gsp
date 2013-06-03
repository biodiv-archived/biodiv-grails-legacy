<r:script>
    $(document).ready(function() {
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

                var initSW = new google.maps.LatLng('${userGroupInstance.sw_latitude}', '${userGroupInstance.sw_longitude}'); 
                var initNE = new google.maps.LatLng('${userGroupInstance.ne_latitude}', '${userGroupInstance.ne_longitude}'); 
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


    });      
});
</r:script>
<div class="observation_location">
<div id="map_canvas" style="width:100%; height: 300px;"></div>
</div>
