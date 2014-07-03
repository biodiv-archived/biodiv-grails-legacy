	<div class="observation_location">
                <g:javascript>

  
                $(document).ready(function() {
                    window.params.snippetUrl = "${uGroup.createLink(controller:'observation', action:'snippet', 'userGroupWebaddress':userGroup?.webaddress) }"
                    <g:if test="{params.id}">
                    window.params.filteredMapBasedObservationsListUrl = "${uGroup.createLink( controller:'observation', action: "list",'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, id:params.id)}" + location.search
                    </g:if><g:else>
                    window.params.filteredMapBasedObservationsListUrl = "${uGroup.createLink( controller:'observation', action: "list", 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" + location.search
                    </g:else>
                });
                </g:javascript>
		<div class="map_wrapper map_class">
                    <div id="big_map_canvas">
                        <center>
                            <div id="spinner" class="spinner">
                            <img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"
                                alt="${message(code:'spinner.alt',default:'Loading...')}" />
                            </div>
                       </center>
                    </div>
		</div>
	</div>
        
