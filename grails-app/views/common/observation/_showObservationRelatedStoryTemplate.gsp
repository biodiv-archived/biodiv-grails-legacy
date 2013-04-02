<r:script> 
$(document).ready(function() {
	$('#carousel_${id}').jcarousel({
		itemLoadCallback : itemLoadCallback,
        url:"${uGroup.createLink(controller:controller, action:action, id:observationId, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}",
        filterProperty:"${filterProperty}",
        filterPropertyValue:"${filterPropertyValue}",
        carouselDivId:"#carousel_" + "${id}",
        carouselMsgDivId:"#relatedObservationMsg_" + "${id}",
        carouselAddObvDivId:"#relatedObservationAddButton_" + "${id}",
        itemFallbackDimension : window.params.carousel.maxWidth,
        contextFreeUrl:"${uGroup.createLink(controller:resultController?:controller, action:'show')}",
        contextGroupWebaddress:"${userGroupWebaddress}",
        scroll:3    
	});

/*	$('#carousel_${id} img').hover( function () {
    	$(this).append($("<span> ***</span>"));
  	}, 
  	function () {
    	$(this).find("span:last").remove();
  	}
	);

	$('#carousel_${id} .jcarousel-item ').live('mouseenter', function(event) {		
		$.ajax({
 				url: "${uGroup.createLink(controller:controller, action:'snippet')}",
 				method:"GET",
 				dataType:'html',
 				data:{'id':${observationId?:speciesId}},
 				success : function(data, textStatus, xhr){
 					console.log($(this));	  					
 					$('#carouselItemDesc').html(data).popover('show');
 				},
			error: function (xhr, status, thrownError) {
				console.log("Error while callback to new comment"+xhr.responseText)
			}
		});
	}).live('mouseleave' , function(event){
		//$("#carouselItemDesc").hide();
	});
*/	
});
</r:script>

<div id="carousel_${id}" class="jcarousel-skin-ie7">
	<ul>
		<!-- The content will be dynamically loaded in here -->
	</ul>
	<g:if test="${!hideShowAll}">
		<div class="observation_links">
			<g:if test="${observationId}">
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'listRelated', id: observationId, parentType:'observation', filterProperty : filterProperty, offset:0, limit:9, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}">Show
					all</a>
			</g:if>
			<g:elseif test="${speciesId}">
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'listRelated', id: speciesId, parentType:'species', filterProperty : filterProperty, filterPropertyValue:filterPropertyValue, offset:0, limit:9, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}">Show
					all</a>
			</g:elseif>
			<g:else>
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'list', (filterProperty) : filterPropertyValue, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}">Show
					all</a>
			</g:else>
		</div>
	</g:if>
<%--	<div id="carouselItemDesc"></div>--%>
</div>
<div id="relatedObservationAddButton_${id}" style="display:none;">
	<!--<g:link controller="observation" action="create"><div class="btn btn-warning">Add an observation</div></g:link>-->
	<span class="msg">No observations</span>
</div>

<div id="relatedObservationMsg_${id}" style="display:none;">
	<span class="msg">No observations</span>
</div>
