
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>

<td><uGroup:interestedSpeciesGroups
		model="['userGroupInstance':userGroupInstance]" /></td>
<td><uGroup:interestedHabitats
		model="['userGroupInstance':userGroupInstance]" /></td>
<td>
	${userGroupInstance.getAllMembersCount()}
</td>

<td><div class="pull-right"><uGroup:joinLeaveGroupTemplate
		model="['userGroupInstance':userGroupInstance, 'showLeave':showLeave, 'showJoin':showJoin]" /></div></td>
