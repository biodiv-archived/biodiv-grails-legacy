
<div class="observation_location">

	<div id="map_canvas_${observationInstance.id}" style="height: 170px;"></div>
	<div class="prop">
		<span class="name"><i class="icon-map-marker"> </i>Place name</span>
			<g:if test="${observationInstance.placeName != ''}">
                            <g:set var="location" value="${observationInstance.placeName}"/>
			</g:if>
			<g:else>
			    <g:set var="location" value="${observationInstance.reverseGeocodedName}"/>
			</g:else>
                    
                        <div class="value ellipsis multiline" title="${location}">
                    ${location}
		</div>
	</div>

	<div class="prop">
		<span class="name"><i class="icon-map-marker"> </i>Coordinates</span>
		<div class="value">${observationInstance.latitude},
			${observationInstance.longitude}
		</div>
	</div>

	<!-- script type="text/javascript"
		src="http://maps.google.com/maps/api/js?sensor=true"></script-->
	<r:script>
                $(document).ready(function() {
                  var latlng = new google.maps.LatLng(${observationInstance.latitude}, ${observationInstance.longitude});
                  var options = {
                    zoom: 13,
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
        </r:script>

</div>
