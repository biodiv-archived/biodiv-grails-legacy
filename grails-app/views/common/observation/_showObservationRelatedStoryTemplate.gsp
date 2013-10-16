<r:script> 
$(document).ready(function() {
    $('#carousel_${id}').jcarousel({
        itemLoadCallback : itemLoadCallback,
        setupCallback : setupCallback,
        url:"${uGroup.createLink(controller:controller, action:action, id:observationId, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}",
        filterProperty:"${filterProperty}",
        filterPropertyValue:"${filterPropertyValue}",
        carouselDivId:"#carousel_" + "${id}",
        carouselMsgDivId:"#relatedObservationMsg_" + "${id}",
        carouselAddObvDivId:"#relatedObservationAddButton_" + "${id}",
        //itemFallbackDimension : window.params.carousel.maxWidth,
        contextFreeUrl:"${uGroup.createLink(controller:resultController?:controller, action:'show')}",
        contextGroupWebaddress:"${userGroupWebaddress}",
        <g:if test="${filterProperty == 'featureBy'}">
            vertical:true,
            scroll:1,
            getItemHTML:getSnippetHTML
        </g:if>
        <g:else>
            scroll:1,
            visible:2,
            getItemHTML:getSnippetTabletHTML,
            horizontal:true
        </g:else>
    });
    $(".jcarousel-prev-vertical").append("<i class='icon-chevron-up'></i>").hover(function(){
        $(this).children().addClass('icon-gray');    
    }, function(){
        $(this).children().removeClass('icon-gray');    
    });

    $(".jcarousel-next-vertical").append("<i class='icon-chevron-down'></i>").hover(function(){
        $(this).children().addClass('icon-gray');    
    }, function(){
        $(this).children().removeClass('icon-gray');    
    });
    
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
					href="${uGroup.createLink(controller:controller, action:'listRelated', id: observationId, parentType:'observation', filterProperty : filterProperty, offset:0, limit:12, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}">Show
					all</a>
			</g:if>
			<g:elseif test="${speciesId}">
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'listRelated', id: speciesId, parentType:'species', filterProperty : filterProperty, filterPropertyValue:filterPropertyValue, offset:0, limit:12, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}">Show
					all</a>
			</g:elseif>
			<g:else>
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'list', (filterProperty) : filterPropertyValue?:true, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}">Show
					all</a>
			</g:else>
		</div>
	</g:if>
<%--	<div id="carouselItemDesc"></div>--%>
</div>
<div id="relatedObservationAddButton_${id}" class="alert alert-info" style="display:none;">
	No observations
</div>

<div id="relatedObservationMsg_${id}" class="alert alert-info" style="display:none;">
	No observations
</div>
