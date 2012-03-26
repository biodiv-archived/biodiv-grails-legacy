
<div class="observation_location">
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
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                  };
                  var big_map = new google.maps.Map(document.getElementById("big_map_canvas"), options);

                  <g:each in="${observationInstanceList}" status="i"
						var="observationInstance">
		                var latlng = new google.maps.LatLng(
		                		${observationInstance.latitude}, ${observationInstance.longitude});
						var marker = new google.maps.Marker({
							position: latlng,
							map: big_map,
							draggable: false
						});
	                    markers.push(marker);

                            var infowindow = new google.maps.InfoWindow({
                                content: 'An InfoWindow'
                            });

	                    google.maps.event.addListener(marker, 'click', function() {
	                        big_map.setZoom(8);
	                        big_map.setCenter(marker.getPosition());
                                infowindow.open(big_map, marker);
	                    });

		    </g:each>	
				  
				  var markerCluster = new MarkerClusterer(big_map, markers);
                
                  big_map.setCenter(latlng);

                });
                </script>
		<div id="big_map_canvas" style="height: 300px;"></div>
	</div>
</div>
