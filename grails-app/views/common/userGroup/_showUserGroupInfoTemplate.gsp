
<%@page import="species.utils.ImageType"%>
<div class="observation_info">

	<h5>
		${userGroupInstance.name }
	</h5>

	<div class="signature">
		<span class="name tablet"><i class="icon-time"></i><g:message code="default.founded.label" /></span>
		<obv:showDate
			model="['userGroupInstance':userGroupInstance, 'propertyName':'FoundedOn']" />
	</div>

	<div class="btn btn-primary view-button">
		<g:link mapping="userGroup" action="show"
			params="['webaddress':userGroupInstance.webaddress]"><g:message code="button.view" /></g:link>
	</div>


</div>


