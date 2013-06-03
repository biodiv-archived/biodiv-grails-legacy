<g:if test="${instanceTotal > 0 }">
<div class="observation_location_wrapper">
	<div class="observation_location">
		<div class="checklist_list">
		<g:set var="snippetUrl" value="${uGroup.createLink(controller:'checklist', action:'snippet', 'userGroupWebaddress':userGroup?.webaddress) }"/>
		<g:javascript>
                var markers = [];
                var big_map;
                var  nagpur_latlng, swRestriction, neRestriction, allowedBounds;
  
                $(document).ready(function() {
                    loadGoogleMapsAPI(function() {
                        initMap();
                    });
                });

                function initMap() {
                nagpur_latlng = new google.maps.LatLng('21.07', '79.27');
                swRestriction = new google.maps.LatLng('8', '69');
                neRestriction = new google.maps.LatLng('36', '98');

                var allowedBounds = new google.maps.LatLngBounds(swRestriction, neRestriction);
                
                  var options = {
                    zoom: 4,
                    center: nagpur_latlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    minZoom: 4,
                    maxZoom: 15
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

                  function getRandomNumber(){
					return ((Math.random() -.5) / 200);
                  }

                  function jitterCloseMarker(big_map){
                	var zoomLevel = big_map.getZoom();
              		if(zoomLevel >= 13){
                  		var markerKeys = [];
                  		var mapBounds = big_map.getBounds()
                  		for (var i = 0; i < markers.length; i++) {
                      		var pos = markers[i].getPosition();
                      		if(mapBounds.contains(pos)){
									if($.inArray(pos.toString(), markerKeys) != -1){
										markers[i].setPosition(new google.maps.LatLng(pos.lat() + getRandomNumber(), pos.lng() + getRandomNumber()));
									}else{
										markerKeys.push(pos.toString());
									}
                              }
                  			}
                      }
                  }  
                  <g:each in="${checklistInstanceList}" status="i"
						var="checklistInstance">
                        addMarker(${checklistInstance.id}, ${checklistInstance.latitude},  ${checklistInstance.longitude}); 

		    		</g:each>	
				  
<%--                    google.maps.event.addListener(big_map, 'mouseout', function() {--%>
<%--                        var bounds = getSelectedBounds();--%>
<%--                        refreshList(bounds);--%>
<%--                    });--%>

                    google.maps.event.addListener(big_map, 'zoom_changed', function() {
                    	jitterCloseMarker(big_map);
                    	
                    });
                    
		  var markerCluster = new MarkerClusterer(big_map, markers, {gridSize: 30, maxZoom:13});
                
                    
                  function load_content(map, marker, id, infowindow){
                      $.ajax({
                        url: "${snippetUrl }"+"/"+id,
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


                google.maps.event.addListener(big_map, 'dragend', function() { checkBounds(); });
                
                function checkBounds() {
                     if (allowedBounds.contains(big_map.getCenter())) return;

                     var c = big_map.getCenter(),
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

                     big_map.setCenter(new google.maps.LatLng(y, x));
                }

                }
                </g:javascript>
        </div>        
		<div class="map_wrapper">
			<div id="big_map_canvas" style="height: 500px; width: 100%;"></div>
		</div>
	</div>
</div>
</g:if>
	<r:script>
            function refreshList(bounds){
            	<g:if test="{params.id}">
                var url = "${uGroup.createLink( controller:'checklist', action: "filteredMapBasedChecklistList",'userGroupWebaddress':userGroup?.webaddress, id:params.id)}" + location.search
                </g:if><g:else>
                var url = "${uGroup.createLink( controller:'checklist', action: "filteredMapBasedChecklistList", 'userGroupWebaddress':userGroup?.webaddress)}" + location.search
                </g:else>
                if (bounds !== undefined){
                    var sep = (location.search == "") ? "?" : "&";
                    url = url + sep + "bounds=" + bounds
			        $.ajax({
	                    url:  url,
	                    dataType: "html",
	                    success: function(data) {
	                        $("#map_results_list").html(data.mapViewHtml);
	                    }
	                });
                }    
            }

            $(function(){
                refreshList();
            });
        </r:script>
