<div class="grid_15 observation_location">
	<div>
		<script type="text/javascript"
			src="http://maps.google.com/maps/api/js?sensor=true"></script>
		<g:javascript src="markerclusterer.js"
			base="${grailsApplication.config.grails.serverURL+'/js/location/google/'}"></g:javascript>
		<script>
                var markers = [];
                
                $(document).ready(function() {
                  var latlng = new google.maps.LatLng('22.77', '77.22');
                  var options = {
                    zoom: 4,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.HYBRID
                  };
                  var map = new google.maps.Map(document.getElementById("map_canvas"), options);

                  <g:each in="${observationInstanceList}" status="i"
						var="observationInstance">
		                var latlng = new google.maps.LatLng(
		                		${observationInstance.latitude}, ${observationInstance.longitude});
						var marker = new google.maps.Marker({
							position: latlng,
							map: map,
							draggable: false
						});
	                    markers.push(marker);

	                    google.maps.event.addListener(marker, 'click', function() {
	                        map.setZoom(8);
	                        map.setCenter(marker.getPosition());
	                    });
				  </g:each>	
				  
				  var markerCluster = new MarkerClusterer(map, markers);
                
                  map.setCenter(latlng);

                });
                </script>
		<div id="map_canvas" style="height: 170px;"></div>
	</div>
</div>