<g:javascript type="text/javascript"> 
$(document).ready(function() {
	$('#carousel_${id}').jcarousel({
        itemLoadCallback : itemLoadCallback,
        url:"${createLink(controller:controller, action:action, id:observationId)}",
        filterProperty:"${filterProperty}",
        filterPropertyValue:"${filterPropertyValue}",
        carouselDivId:"#carousel_" + "${id}",
        carouselMsgDivId:"#relatedObservationMsg_" + "${id}",
        carouselAddObvDivId:"#relatedObservationAddButton_" + "${id}",
        itemFallbackDimension : 75
	});
	
	$('#carousel_${id} img').hover( function () {
    	$(this).append($("<span> ***</span>"));
  	}, 
  	function () {
    	$(this).find("span:last").remove();
  	}
	);
	
});
</g:javascript> 
  <div id="carousel_${id}" class="jcarousel-skin-ie7"> 
    <ul> 
      <!-- The content will be dynamically loaded in here --> 
    </ul> 
    <div class="observation_links">
  		<g:if test="${observationId}">
		    <a class="btn btn-mini" href="${createLink(controller:controller, action:'listRelated', params: [id: observationId, filterProperty : filterProperty])}">Show all</a>
		</g:if>
                <g:else>
		    <a class="btn btn-mini" href="${createLink(controller:controller, action:'list', params: [(filterProperty) : filterPropertyValue])}">Show all</a>
		</g:else>
	</div>

</div>
<div id="relatedObservationAddButton_${id}" style="padding-left:10px; display:none">
	<g:link controller="observation" action="create"><div class="btn btn-warning">Add an observation</div></g:link>
</div>

<div id="relatedObservationMsg_${id}" style="padding-left:10px; display:none">
	<span class="msg">This is the first observation</span>
</div>
