<script>
    var swRestriction = new google.maps.LatLng('8', '69');
    var neRestriction = new google.maps.LatLng('36', '98');
    var allowedBounds = new google.maps.LatLngBounds(swRestriction, neRestriction);

      function initialize() {
        var mapOptions = {
          center: new google.maps.LatLng(21.07,79.27),
          zoom: 4,
          mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        var map = new google.maps.Map(document.getElementById('map_canvas'),
          mapOptions);

        google.maps.event.addListener(map, 'click', function(event) { initRect(event.latLng); });

        google.maps.event.addListener(map, 'dragend', function() { checkBounds(); });
      
        var rectangle = new google.maps.Rectangle();
        rectangle.setMap(map);
        initRect();



        google.maps.event.addListener(rectangle, 'bounds_changed', function() {
            var rect_bounds = rectangle.getBounds()
            var sw_lat = rect_bounds.getSouthWest().lat()
            var sw_lng = rect_bounds.getSouthWest().lng()
            var ne_lat = rect_bounds.getNorthEast().lat()
            var ne_lng = rect_bounds.getNorthEast().lng()
            
            $('#sw_latitude').val(sw_lat);
            $('#sw_longitude').val(sw_lng);
            $('#ne_latitude').val(ne_lat);
            $('#ne_longitude').val(ne_lng);

        });

        function initRect(latLng) {
           
            var initRect;

            if (latLng !== undefined) {
                var initSW = new google.maps.LatLng(latLng.lat() - 0.5, latLng.lng() - 0.5); 
                var initNE = new google.maps.LatLng(latLng.lat() + 0.5, latLng.lng() + 0.5); 
                initRect = new google.maps.LatLngBounds(initSW, initNE);
            } else if ('${userGroupInstance.id}') {
                var initSW = new google.maps.LatLng('${userGroupInstance.sw_latitude}', '${userGroupInstance.sw_longitude}'); 
                var initNE = new google.maps.LatLng('${userGroupInstance.ne_latitude}', '${userGroupInstance.ne_longitude}'); 
                initRect = new google.maps.LatLngBounds(initSW, initNE);
            }

            rectangle.setOptions({
              bounds: initRect,
              editable: true,
              fillOpacity: 0.1,
              fillColor: '#cc0000',
              strokeWeight: 1
            });

        }

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

      }

      google.maps.event.addDomListener(window, 'load', initialize);
</script>
<div class="observation_location">
<div id="map_canvas" style="width:100%; height: 300px;"></div>
</div>
<input id="sw_latitude" type="hidden" name="sw_latitude" value="${userGroupInstance?.sw_latitude}"/>
<input id="sw_longitude" type="hidden" name="sw_longitude" value="${userGroupInstance?.sw_longitude}"/>
<input id="ne_latitude" type="hidden" name="ne_latitude" value="${userGroupInstance?.ne_latitude}"/>
<input id="ne_longitude" type="hidden" name="ne_longitude" value="${userGroupInstance?.ne_longitude}"/>
