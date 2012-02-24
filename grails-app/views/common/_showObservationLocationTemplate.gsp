
<div class="grid_5 observation_location">
	<div>
		<p class="prop">
			<span class="name">Place name</span> <div class="value"> ${observationInstance.placeName}
			</div>
		</p>
                <p class="prop">
			<span class="name">Coordinates</span> <div class="value"> ${observationInstance.latitude}, ${observationInstance.longitude}
			</div>
		</p>
                <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=true"></script>
                <script>
                $(document).ready(function() {
                  var latlng = new google.maps.LatLng(${observationInstance.latitude}, ${observationInstance.longitude});
                  var options = {
                    zoom: 4,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.HYBRID
                  };
                  var map = new google.maps.Map(document.getElementById("map_canvas_${observationInstance.id}"), options);
                  var marker = new google.maps.Marker({
                    map: map,
                    draggable: false
                  });
                
                  marker.setPosition(latlng);
                  map.setCenter(latlng);

                });
                </script>
                <div id="map_canvas_${observationInstance.id}" style="height:170px;"></div>
	</div>
</div>
