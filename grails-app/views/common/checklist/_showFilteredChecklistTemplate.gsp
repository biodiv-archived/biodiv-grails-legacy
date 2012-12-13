<div class="checklist_list" > 
<table class="table table-hover span8" style="margin-left: 0px;">
	<thead>
		<tr>
			<th>Title</th>
			<th>Species Group</th>
			<th>No. of Species</th>
			<th>Place Name</th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${checklistInstanceList}" status="i"
			var="checklistInstance">
			<tr>
				<td><a href="${uGroup.createLink(controller:'checklist', action:'show', pos:i, id:checklistInstance.id, userGroupWebaddress:params.webaddress)}">${checklistInstance.title}</a></td>
				<td>${checklistInstance.speciesGroup?.name}</td>
				<td>${checklistInstance.speciesCount}</td>
				<td>${checklistInstance.placeName}</td>
			</tr>
		</g:each>
	</tbody>
</table>
</div>

