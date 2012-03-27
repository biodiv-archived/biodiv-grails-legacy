
<div class="observation_location">
	<div>
		<script type="text/javascript"
			src="http://maps.google.com/maps/api/js?sensor=true"></script>
		<g:javascript src="markerclusterer.js"
			base="${grailsApplication.config.grails.serverURL+'/js/location/google/'}"></g:javascript>
		<script>
                var markers = [];
                
                $(document).ready(function() {
                  var latlng = new google.maps.LatLng('21.07', '79.27');
                  var options = {
                    zoom: 4,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                  };
                  var big_map = new google.maps.Map(document.getElementById("big_map_canvas"), options);

                    var infowindow = new google.maps.InfoWindow({
                        content: 'InfoWindow',
                        maxWidth: 400
                    });
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

	                    google.maps.event.addListener(marker, 'click', function() {
                                infowindow.setPosition(marker.getPosition());
                                load_content(big_map, this, ${observationInstance.id}, infowindow); 
	                    });

                            

		    </g:each>	
				  
                    google.maps.event.addListener(big_map, 'mouseout', function() {
                        var bounds = getSelectedBounds();
                        refreshList(bounds);
                    });
		  var markerCluster = new MarkerClusterer(big_map, markers, {gridSize: 30, maxZoom: 10});
                
                  big_map.setCenter(latlng);
                    
                  function load_content(map, marker, id, infowindow){
                      $.ajax({
                        url: '/biodiv/observation/snippet/' + id ,
                        success: function(data){
                          infowindow.setContent("<div id='info-content'>" + data + "</div>");
                          infowindow.open(map, marker);
                        }
                      });
                    }
                    
                  function getSelectedBounds() {
                    var bounds = '';
                    var swLat = big_map.getBounds().getSouthWest().lat();
                    var swLng = big_map.getBounds().getSouthWest().lng();
                    var neLat = big_map.getBounds().getNorthEast().lat();
                    var neLng = big_map.getBounds().getNorthEast().lng();

                    bounds = [swLat, swLng, neLat, neLng].join()
                    return bounds;    
                }


                });
                </script>
		<div id="big_map_canvas" style="height: 500px;"></div>
	</div>
        <div id="map_results_list"></div>
        <script>
            function refreshList(bounds){
                var url = "${g.createLink(controller: "observation", action: "filteredList")}" + location.search
                if (bounds !== undefined)
                    var sep = (location.search == "") ? "?" : "&";
                    url = url + sep + "bounds=" + bounds

                $.ajax({
                    url:  url,
                    dataType: "html",
                    success: function(data) {
                        $("#map_results_list").html(data);
                    }
                });    
            }

            $(function(){
                refreshList();
            });
        </script>
</div>
