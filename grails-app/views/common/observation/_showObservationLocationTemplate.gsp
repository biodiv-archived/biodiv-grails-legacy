<%@ page import="species.utils.Utils"%>
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
                
                <input class="degree_field" id="latitude_field" type="hidden" name="latitude" value="${observationInstance?.latitude}"></input>
                <input class="degree_field" id="longitude_field" type="hidden" name="longitude" style="width:193px;" value="${observationInstance?.longitude}"></input>
                <input id="areas" type="hidden" name="areas" value="${Utils.GeometryAsWKT(observationInstance?.areas)}"></input>
	</div>

	<r:script>
                $(document).ready(function() {
                    loadGoogleMapsAPI(function() {
                        initialize(document.getElementById("map_canvas_${observationInstance.id}"), false);
                        /*
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
                        */
                    });
                });
        </r:script>

</div>
