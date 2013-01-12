<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>

<h5 style="margin: 0;">
	<a class="ellipsis"
		href="${uGroup.createLink([action:'show', controller:'SUser', id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}"
		title="${userInstance.name}"> ${userInstance.name}
	</a>
</h5>
