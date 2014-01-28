<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>

<h5 class="ellipsis">
	<a 
		href="${uGroup.createLink([action:'show', controller:'SUser', id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}"
		title="${userInstance.name}"> ${userInstance.name}
	</a>

</h5>
