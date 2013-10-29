<r:script> 
$(document).ready(function() {
    <g:if test="${relatedInstanceList || filterProperty != 'featureBy'}">
    $('#carousel_${id}').jcarousel({
        itemLoadCallback : itemLoadCallback,
        initCallback : initCallback,
        setupCallback : setupCallback,
        url:"${uGroup.createLink(controller:controller, action:action, id:observationId, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}",
        filterProperty:"${filterProperty}",
        filterPropertyValue:"${filterPropertyValue}",
        carouselDivId:"#carousel_" + "${id}",
        carouselMsgDivId:"#relatedObservationMsg_" + "${id}",
        carouselAddObvDivId:"#relatedObservationAddButton_" + "${id}",
        itemFallbackDimension : window.params.carousel.maxWidth,
        contextFreeUrl:"${uGroup.createLink(controller:resultController?:controller, action:'show')}",
        contextGroupWebaddress:"${userGroupWebaddress}",
        <g:if test="${filterProperty == 'featureBy'}">
            vertical:true,
            scroll:1,
            getItemHTML:getSnippetHTML
        </g:if>
        <g:else>
            scroll:3,
            getItemHTML:getSnippetTabletHTML,
            horizontal:true
        </g:else>
    });
    </g:if>
});
</r:script>

<div id="carousel_${id}" class="jcarousel-skin-ie7">

    <g:if test="${relatedInstanceList || filterProperty != 'featureBy'}">
        <h5>Featured ${controller.toLowerCase().capitalize()}</h5>
    </g:if>
	<ul style="list-style:none; width:100%; margin-left:0px;">
            <!-- The content will be dynamically loaded in here along with static content present here in featuredInstanceList-->
            <g:each in="${relatedInstanceList}" var="relatedInstanceDetails">
                <li style="float: left; list-style: none;">
                <g:render template="/${resultController?:controller}/relatedSnippetTemplate" model="[relatedInstanceDetails:relatedInstanceDetails, controller:resultController?:'observation']"/>
                </li>
            </g:each>
        </ul>

	<g:if test="${!hideShowAll && (relatedInstanceList||filterProperty != 'featureBy')}">
		<div class="observation_links">
			<g:if test="${observationId}">
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'listRelated', id: observationId, parentType:'observation', filterProperty : filterProperty, offset:0, limit:12, 'userGroupInstance':userGroupInstance)}">Show
					all</a>
			</g:if>
			<g:elseif test="${speciesId}">
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'listRelated', id: speciesId, parentType:'species', filterProperty : filterProperty, filterPropertyValue:filterPropertyValue, offset:0, limit:12, 'userGroupInstance':userGroupInstance)}">Show
					all</a>
			</g:elseif>
                        <g:else>
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'list', (filterProperty) : filterPropertyValue, 'userGroupInstance':userGroupInstance)}">Show
					all</a>
			</g:else>
		</div>
	</g:if>
<%--	<div id="carouselItemDesc"></div>--%>
</div>
<div id="relatedObservationAddButton_${id}" class="alert alert-info" style="display:none;">
	No observations
    </div>

<g:if test="${filterProperty != 'featureBy'}">
    <div id="relatedObservationMsg_${id}" class="alert alert-info" style="display:none;">
        No data!!
    </div>
</g:if>

