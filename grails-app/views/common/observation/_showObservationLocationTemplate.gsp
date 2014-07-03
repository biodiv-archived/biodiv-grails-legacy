<%@ page import="species.utils.Utils"%>

<div class="sidebar_section">
    <h5>Species Distribution</h5>
    <obv:showObservationsLocation model="['userGroup':userGroup]"></obv:showObservationsLocation>
    <h5>Location Information</h5>
    <table class="table table-bordered table-condensed table-striped">
        <tr>
            <td colspan="2">
            <g:if test="${observationInstance.geoPrivacy}">
            	Geoprivacy enabled
            </g:if>
            <g:else>
                <g:if test="${observationInstance.placeName != ''}">
                	<g:set var="location" value="${observationInstance.placeName}"/>
                </g:if>
                <g:else>
                	<g:set var="location" value="${observationInstance.reverseGeocodedName}"/>
                </g:else>
                <div class="value ellipsis multiline" title="${location}">
                	${location}
            	</div>
            </g:else>
            </td>
        </tr>
        <tr>
            <td colspan="2">
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
                ${(geoPrivacyAdjustment != 0) ? 'Geoprivacy enabled' : latitude.toFloat() + ',' + longitude.toFloat()}

				<input id='areas' type='hidden' name='areas' value='${areas}'/>

                <input class="degree_field" id="latitude_field" type="hidden" name="latitude" value="${latitude}"/>
                <input class="degree_field" id="longitude_field" type="hidden" name="longitude" style="width:193px;" value="${longitude}"/>

            </td>
        </tr>
        <g:each in="${observationInstance.getObservationFeatures()}" var="feature">
        <tr>
            <td class=" feature_icon ${feature.key.toLowerCase().replaceAll(/\s+/,'_')}" title="${feature.key}"></td>
            <td>${feature.value}</td>
        </tr>
        </g:each>
    </table>
</div>	

<g:if test="${!observationInstance.isChecklist && observationInstance.maxVotedReco}">
    <div class="sidebar_section tile temporalDist">
        <h5>Temporal Distribution</h5>
        <div id="temporalDist" style="height:108px;">
        </div>
        <ul>
            <li>Jan</li>
            <li>Feb</li>
            <li>Mar</li>
            <li>Apr</li>
            <li>May</li>
            <li>Jun</li>
            <li>Jul</li>
            <li>Aug</li>
            <li>Sep</li>
            <li>Oct</li>
            <li>Nov</li>
            <li>Dec</li>
        </ul>
    </div>
</g:if>

<r:script>
    $(document).ready(function() {
        loadGoogleMapsAPI(function() {
            var mapLocationPicker = new $.fn.components.MapLocationPicker(document.getElementById("big_map_canvas"));
            <g:if test="${!observationInstance.isChecklist}">
            showObservationMapView("${observationInstance.id}", ${observationInstance.fromDate.getTime()});
            </g:if>
            var icon;
            
            var ptIcon = M.AwesomeMarkers.icon({
                icon: 'ok', 
                color: 'red'
            });
            var ctIcon = M.AwesomeMarkers.icon({
                icon: 'list', 
                color: 'red'
            });

            if(${observationInstance.geoPrivacy}){
                icon = (${observationInstance.isChecklist})?geoPrivacyChecklistIcon:geoPrivacyPointIcon;
            } else {
                icon = (${observationInstance.isChecklist})?ctIcon:ptIcon;
            }

            mapLocationPicker.initArea(false, undefined, undefined, undefined,{icon:icon, layer:'Current Observation' });
            //HACK
            if(mapLocationPicker.searchMarker)
                map.panTo(mapLocationPicker.searchMarker.getLatLng());

            <g:if test="${!observationInstance.isChecklist}">
                resetMap();
            </g:if>
        });
    });
</r:script>

