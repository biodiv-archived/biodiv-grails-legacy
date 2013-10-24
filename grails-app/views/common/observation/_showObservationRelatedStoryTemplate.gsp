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
	<ul style="list-style:none; width:100%; margin-left:0px">
            <!-- The content will be dynamically loaded in here -->
            <li style="float: left; list-style: none;"><div class="thumbnail"><div class="observation_th snippet"><div class="figure observation_story_image span2"><a href="/observation/show/3"><img class="img-polaroid" src="http://indiabiodiversity.localhost.org/biodiv/observations//2114b786-3e5e-4480-b5ed-58c561f2d06d/627_th1.jpg" title="test1" alt=""></a></div><div class="span10" style= "margin-left:10px;"><h5 class="popover-title"><b>Featured :</b> test1 <small>on Oct 22 2013</small></h5><div style="padding-left:16px; padding-right:16px">dsfgdrgh<p>- Observed at '292/C, Road Number 1, Ashok Nagar, Ranchi, Jharkhand 834002, India' by Rahul Kumar Sinha on 17/10/2013</p></div></div></div></div></li>
	</ul>
	<g:if test="${!hideShowAll}">
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

