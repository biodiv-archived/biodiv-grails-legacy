
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>

<td><uGroup:interestedSpeciesGroups
		model="['userGroupInstance':userGroupInstance]" /></td>
<td><uGroup:interestedHabitats
		model="['userGroupInstance':userGroupInstance]" /></td>
<td>
	${userGroupInstance.getAllMembersCount()}
</td>
<td><uGroup:joinLeaveGroupTemplate
		model="['userGroupInstance':userGroupInstance]" /></td>
