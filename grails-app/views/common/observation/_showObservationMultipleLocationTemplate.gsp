	<div class="observation_location">
                <g:javascript>

  
                $(document).ready(function() {
                    window.params.snippetUrl = "${uGroup.createLink(controller:'observation', action:'snippet', 'userGroupWebaddress':userGroup?.webaddress) }"
                    <g:if test="{params.id}">
                    window.params.filteredMapBasedObservationsListUrl = "${uGroup.createLink( controller:'observation', action: "filteredMapBasedObservationsList",'userGroupWebaddress':userGroup?.webaddress, id:params.id)}" + location.search
                    </g:if><g:else>
                    window.params.filteredMapBasedObservationsListUrl = "${uGroup.createLink( controller:'observation', action: "filteredMapBasedObservationsList", 'userGroupWebaddress':userGroup?.webaddress)}" + location.search
                    </g:else>
                });

                /*  function addMarkers() {
                    console.log("adding markers");
                    <g:each in="${observationInstanceList}" status="i" var="observationInstance">
                     addMarker(${observationInstance[0]}, ${observationInstance[1].getCentroid().getY()},  ${observationInstance[1].getCentroid().getX()}); 
                    </g:each>
                }*/

                function initMap() {
/*                   nagpur_latlng = new google.maps.LatLng('21.07', '79.27');
                   swRestriction = new google.maps.LatLng('8', '69');
                   neRestriction = new google.maps.LatLng('36', '98');
                   allowedBounds = new google.maps.LatLngBounds(swRestriction, neRestriction);

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
*/									
                 }
                </g:javascript>
		<div class="map_wrapper">
                    <div id="big_map_canvas" style="height: ${height?:'500'}px; width: ${width?:'100%'};">
                        <center>
                            <div id="spinner" class="spinner">
                            <img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"
                                alt="${message(code:'spinner.alt',default:'Loading...')}" />
                            </div>
                       </center>
                    </div>
		</div>
	</div>
	<div id="map_results_list"></div>
	<r:script>
            $(function(){
//                refreshList();
            });
        </r:script>
</div>
