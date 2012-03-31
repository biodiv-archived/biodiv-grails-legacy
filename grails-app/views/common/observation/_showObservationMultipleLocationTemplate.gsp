
<div class="observation_location">
	<div>
		<script>
                var markers = [];
                var big_map;
                var nagpur_latlng = new google.maps.LatLng('21.07', '79.27');
                
                $(document).ready(function() {
                  var options = {
                    zoom: 4,
                    center: nagpur_latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                  };
                  big_map = new google.maps.Map(document.getElementById("big_map_canvas"), options);

                    var infowindow = new google.maps.InfoWindow({
                        content: 'InfoWindow',
                        maxWidth: 400
                    });

                  function addMarker(id, lat, lng) {
                     var latlng = new google.maps.LatLng(lat, lng);
						var marker = new google.maps.Marker({
							position: latlng,
							map: big_map,
							draggable: false
						});
	                    markers.push(marker);

	                    google.maps.event.addListener(marker, 'click', function() {
                                infowindow.setPosition(marker.getPosition());
                                load_content(big_map, this, id, infowindow); 
	                    });

                  }  
                  <g:each in="${observationInstanceList}" status="i"
						var="observationInstance">
                        addMarker(${observationInstance.id}, ${observationInstance.latitude},  ${observationInstance.longitude}); 

		    </g:each>	
				  
                    google.maps.event.addListener(big_map, 'mouseout', function() {
                        var bounds = getSelectedBounds();
                        refreshList(bounds);
                    });
		  var markerCluster = new MarkerClusterer(big_map, markers, {gridSize: 30, maxZoom: 10});
                
                    
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
                <div class="map_wrapper">
		    <div id="big_map_canvas" style="height: 500px; width: 100%;"></div>
                </div>
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
