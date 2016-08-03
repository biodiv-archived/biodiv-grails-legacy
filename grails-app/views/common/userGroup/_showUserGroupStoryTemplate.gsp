
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>

<td><uGroup:interestedSpeciesGroups
		model="['userGroupInstance':userGroupInstance]" /></td>
<td><uGroup:interestedHabitats
		model="['userGroupInstance':userGroupInstance]" /></td>
<td>
	${userGroupInstance.getAllMembersCount()}
</td>

<td>
	<g:if test="${userGroupInstance.allowUsersToJoin}">
		<g:message code="label.open" /></a>
	</g:if>
	<g:else>
		<g:message code="label.close" /></a>
	</g:else>
</td>
<td><div class="pull-right"><uGroup:joinLeaveGroupTemplate
		model="['userGroupInstance':userGroupInstance, 'showLeave':showLeave, 'showJoin':showJoin]" /></div></td>
