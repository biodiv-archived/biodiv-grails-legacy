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
                <%
                    def latitude='',longitude='',areas='';
                    if(observationInstance?.topology instanceof  com.vividsolutions.jts.geom.Point) {
                        latitude = observationInstance.topology.getX()
                        longitude = observationInstance.topology.getY()
                    } else { 
                        if(observationInstance?.topology){ 
                            areas = Utils.GeometryAsWKT(observationInstance?.topology)
                        } else if(params.areas) {
                            areas = params.areas
                        }
                        if(params.latitude) latitude = params.latitude
                        if(params.longitude) longitude = params.longitude
                    }

                %>
                <g:if test="${latitude && longitude}">
		    <span class="name"><i class="icon-map-marker"> </i>Coordinates</span>
                    <div class="value">${latitude},${longitude}</div>
                </g:if>
                <g:elseif test="${areas}">
                     <span class="name"><i class="icon-map-marker"> </i>Centroid</span>
                     <%def centroid = observationInstance.topology.getCentroid()%>
                     <div class="value">${(double)Math.round(centroid.getX() * 1000000) / 1000000},${(double)Math.round(centroid.getY() * 1000000) / 1000000}</div>

                </g:elseif>

                <input id='areas' type='hidden' name='areas' value='${areas}'></input>
               
                <input class="degree_field" id="latitude_field" type="hidden" name="latitude" value="${latitude}"></input>
                <input class="degree_field" id="longitude_field" type="hidden" name="longitude" style="width:193px;" value="${longitude}"></input>
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
