<%@ page import="species.utils.Utils"%>
<div class="sidebar_section">
    <h5><g:message code="default.location.info.label" /></h5>
    <obv:showObservationsLocation model="['userGroup':userGroup]"></obv:showObservationsLocation>
    
    <table class="table table-bordered table-condensed table-striped">
        <tr>
            <td colspan="2">
            <g:if test="${documentInstance.geoPrivacy}">
                <g:message code="default.geoprivacy.enabled.label" />
            </g:if>
            <g:else>
                <g:if test="${documentInstance.placeName != ''}">
                    <g:set var="location" value="${documentInstance.placeName}"/>
                </g:if>
                <g:else>
                    <g:set var="location" value="${documentInstance.reverseGeocodedName}"/>
                </g:else>
                <div class="value ellipsis multiline" title="${location}">
                    ${location}
                </div>
            </g:else>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <div title="${documentInstance.locationScale?.value()}"> ${documentInstance.locationScale?.value()} </div>
        </tr>    
        <tr>
            <td colspan="2">
                <%
                def latitude='',longitude='',areas='';
                def geoPrivacyAdjustment = documentInstance.fetchGeoPrivacyAdjustment()

                latitude = documentInstance.latitude + geoPrivacyAdjustment
                longitude = documentInstance.longitude + geoPrivacyAdjustment

                if(documentInstance?.topology){ 
                   areas = 'POINT (' + longitude.toFloat() + ' ' + latitude.toFloat() +  ')'
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
       
    </table>
</div>  


<asset:script>

    function markChecklistObvs(checklistObvPoints, mapLocationPicker) {
        if(mapLocationPicker.markers) {
            mapLocationPicker.markers.clearLayers();
        } else { 
            mapLocationPicker.markers = new mapLocationPicker.M.MarkerClusterGroup({maxClusterRadius:50});
        }
        var m = [];
        var data = checklistObvPoints //[['255', '17', '78', true, false]]
        
        for(var i=0; i<data.length; i++) {
            var obv = data[i];
            var latitude = obv.lat?obv.lat:obv[1];
            var longitude = obv.lng?obv.lng:obv[2];
            var icon;

            if(obv.geoPrivacy){
                latitude += obv.geoPrivacyAdjust;
                longitude += obv.geoPrivacyAdjust;
            }
            var marker = mapLocationPicker.createMarker(latitude, longitude, {
                draggable: false,
                clusterable: true,
                icon:icon,
                clickable:load_content,
                data:{id:(obv.id?obv.id:obv[0])}
            });
            if(marker) m.push(marker);
        }
        mapLocationPicker.markers.addLayers(m);
        mapLocationPicker.markers.addTo(mapLocationPicker.map);
    }

    $(document).ready(function() {
        loadGoogleMapsAPI(function() {
            var mapLocationPicker = new $.fn.components.MapLocationPicker(document.getElementById("big_map_canvas"));
            mapLocationPicker.initialize();

            var icon;
            
            var ptIcon = mapLocationPicker.M.AwesomeMarkers.icon({
                icon: 'ok', 
                color: 'red'
            });
            var ctIcon = mapLocationPicker.M.AwesomeMarkers.icon({
                icon: 'list', 
                color: 'red'
            });

            if(${documentInstance.geoPrivacy}){
                icon = mapLocationPicker.geoPrivacyPointIcon;
            } else {
                icon = ptIcon;
            }

            mapLocationPicker.initArea(false, undefined, undefined, $("#areas").val(), {icon:icon, layer:'Current Observation' });
            //HACK
            if(mapLocationPicker.searchMarker)
                mapLocationPicker.map.panTo(mapLocationPicker.searchMarker.getLatLng());

        });
    });
</asset:script>

