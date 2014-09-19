<r:script> 
$(document).ready(function() {
    relatedStory("${relatedInstanceList}", "${filterProperty}", "${id}", "${userGroupWebaddress}", "${filterPropertyValue}")
});
</r:script>

<div id="carousel_${id}" class="jcarousel-skin-ie7" style="clear:both;" data-url="${uGroup.createLink(controller:controller, action:action, id:observationId, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress)}" data-contextFreeUrl=${uGroup.createLink(controller:resultController?:controller, action:'show')}>

    <g:if test="${relatedInstanceList && filterProperty == 'featureBy'}">
    <g:if test="${controller.toLowerCase().equals('featured')}">
        <h4><g.message code="heading.feature.content" /></h4>
    </g:if>
    <g:else>
   
    <% def controller_name=""  %>
    <g:if test="${controller=='species'}">
        <%  controller_name=g.message(code:'default.species.label')  %>
    </g:if>
    <g:elseif test="${controller=='observation'}">
        <%  controller_name=g.message(code:'default.observation.label')  %>
    </g:elseif>
    <g:else test="${controller=='document'}" >
        <%  controller_name=g.message(code:'feature.part.document')   %>
    </g:else>


      


    <h4><g:message code="heading.featured" args="${ [controller_name] }" /> </h4>
            </g:else>
    </g:if>
	<ul style="list-style:none; width:100%; margin-left:0px;">
            <!-- The content will be dynamically loaded in here along with static content present here in featuredInstanceList-->
            <g:each in="${relatedInstanceList}" var="relatedInstanceDetails">
                <li style="float: left; list-style: none;">
		

<g:render template="/${relatedInstanceDetails.controller.equalsIgnoreCase('checklist')?'observation':relatedInstanceDetails.controller}/relatedSnippetTemplate" model="[relatedInstanceDetails:relatedInstanceDetails, controller:relatedInstanceDetails.controller?:controller]"/>
                
                </li>
            </g:each>
        </ul>

	<g:if test="${!hideShowAll && (relatedInstanceList||filterProperty != 'featureBy')}">
		<div class="observation_links">
			<g:if test="${observationId}">
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'listRelated', id: observationId, parentType:'observation', filterProperty : filterProperty, offset:0, limit:12, 'userGroupInstance':userGroupInstance)}"><g:message code="button.show.all" />
					</a>
			</g:if>
			<g:elseif test="${speciesId}">
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'listRelated', id: speciesId, parentType:'species', filterProperty : filterProperty, filterPropertyValue:filterPropertyValue, offset:0, limit:12, 'userGroupInstance':userGroupInstance)}"><g:message code="button.show.all" />
					</a>
			</g:elseif>
                        <g:else>
				<a class="btn btn-mini"
					href="${uGroup.createLink(controller:controller, action:'list', (filterProperty) : filterPropertyValue, 'userGroupInstance':userGroupInstance)}"><g:message code="button.show.all" />
					</a>
			</g:else>
		</div>
	</g:if>
<%--	<div id="carouselItemDesc"></div>--%>
</div>
<div id="relatedObservationAddButton_${id}" class="alert alert-info" style="display:none;">
	<g:message code="msg.no.observations" />
    </div>

<g:if test="${filterProperty != 'featureBy'}">
    <div id="relatedObservationMsg_${id}" class="alert alert-info" style="display:none;">
        <g:message code="msg.no.data" />
    </div>
</g:if>

