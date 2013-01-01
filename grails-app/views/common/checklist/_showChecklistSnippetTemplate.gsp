<%@page import="species.utils.ImageType"%>

<table  class="table table-hover" style="margin-left: 0px;">
<tbody>
<tr>
<td>
	<div class="media signature thumbnail" style="margin-left:0px; min-width:300px; max-width:300px; ">
		<div class="media-body">
			<div class="media-heading"  style="text-align:left;">
				<a href="${uGroup.createLink(controller:'checklist', action:'show', id:checklistInstance.id, userGroupWebaddress:userGroup?.webaddress)}"><span class="ellipsis" title="${checklistInstance.title}">${checklistInstance.title}</span></a>
			</div>
		</div>
	</div>
</td>
<td><sUser:interestedSpeciesGroups model="['userInstance':checklistInstance]" /></td>
<td>${checklistInstance.speciesCount}</td>
<td>${checklistInstance.placeName}</td>
<td><sUser:showUserTemplate model="['userInstance':checklistInstance.author, 'userGroup':userGroup]" />
</td>
</tr>
</tbody>
</table>