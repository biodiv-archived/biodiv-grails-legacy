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
					def geoPrivacyAdjustment = observationInstance.fetchGeoPrivacyAdjustment()
					latitude = observationInstance.latitude + geoPrivacyAdjustment
                    longitude = observationInstance.longitude + geoPrivacyAdjustment
                        
                        if(observationInstance?.topology){ 
                            areas = Utils.GeometryAsWKT(observationInstance?.topology)
                        } else if(params.areas) {
                            areas = params.areas
                        }

                        if(!latitude && params.latitude) latitude = params.latitude
                        if(!longitude && params.longitude) longitude = params.longitude

                %>
                <span class="name"><i class="icon-map-marker"> </i>Coordinates</span>
                <div class="value">${(geoPrivacyAdjustment != 0) ? 'Geoprivacy enabled' : latitude.toFloat() + ',' + longitude.toFloat()}</div>

                <input id='areas' type='hidden' name='areas' value='${areas}'/>
               
                <input class="degree_field" id="latitude_field" type="hidden" name="latitude" value="${latitude}"/>
                <input class="degree_field" id="longitude_field" type="hidden" name="longitude" style="width:193px;" value="${longitude}"/>
	</div>

	<r:script>
                $(document).ready(function() {
                    loadGoogleMapsAPI(function() {
                    	initialize(document.getElementById("map_canvas_${observationInstance.id}"), false);
                        var icon;
                    	if(${observationInstance.geoPrivacy}){
                    		icon = (${observationInstance.isChecklist})?geoPrivacyChecklistIcon:geoPrivacyPointIcon
                    	}
                    	initArea(false, undefined, undefined, {icon:icon});
                        //HACK
                        if(searchMarker)
                            map.panTo(searchMarker.getLatLng());
                        /*
                        var latlng = new google.maps.LatLng(${latitude}, ${longitude});
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
