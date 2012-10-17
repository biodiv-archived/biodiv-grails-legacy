<r:script> 
$(document).ready(function() {
	$('#carousel_${id}').jcarousel({
		itemLoadCallback : itemLoadCallback,
        //buttonNextCallback:buttonNextCallback,
        //itemVisibleInCallback:itemVisibleInCallback,
        url:"${uGroup.createLink(controller:controller, action:action, id:observationId, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}",
        filterProperty:"${filterProperty}",
        filterPropertyValue:"${filterPropertyValue}",
        carouselDivId:"#carousel_" + "${id}",
        carouselMsgDivId:"#relatedObservationMsg_" + "${id}",
        carouselAddObvDivId:"#relatedObservationAddButton_" + "${id}",
        itemFallbackDimension : 75,
        contextUrl:"${uGroup.createLink(controller:resultController?:controller, action:'show', 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}"
	});
	
	$('#carousel_${id} img').hover( function () {
    	$(this).append($("<span> ***</span>"));
  	}, 
  	function () {
    	$(this).find("span:last").remove();
  	}
	);
	
});
</r:script> 

  <div id="carousel_${id}" class="jcarousel-skin-ie7"> 
    <ul> 
      <!-- The content will be dynamically loaded in here --> 
    </ul> 
    <div class="observation_links">
  		<g:if test="${observationId}">
		    <a class="btn btn-mini" href="${uGroup.createLink(controller:controller, action:'listRelated', id: observationId, filterProperty : filterProperty, offset:0, limit:9, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}">Show all</a>
		</g:if>
        <g:else>
		    <a class="btn btn-mini" href="${uGroup.createLink(controller:controller, action:'list', (filterProperty) : filterPropertyValue, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}">Show all</a>
		</g:else>
	</div>

</div>
<div id="relatedObservationAddButton_${id}" style="display:none">
	<!--<g:link controller="observation" action="create"><div class="btn btn-warning">Add an observation</div></g:link>-->
	<span class="msg">No observations</span>
</div>

<div id="relatedObservationMsg_${id}" style="display:none">
	<span class="msg">This is the first observation</span>
</div>
