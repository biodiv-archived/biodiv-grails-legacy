<div class="mainContent" name="p${params?.offset}">
<%--<tbody class="mainContent">--%>
	<ul>
		<g:each in="${checklistInstanceList}" status="i"
			var="checklistInstance">
			<li>
			<tr>
				<td><a href="${uGroup.createLink(controller:'checklist', action:'show', pos:i, id:checklistInstance.id, userGroupWebaddress:params.webaddress)}">${checklistInstance.title}</a></td>
				<td>${checklistInstance.speciesGroup?.name}</td>
				<td>${checklistInstance.speciesCount}</td>
				<td>${checklistInstance.placeName}</td>
			</tr>
			</li>
		</g:each>
	</ul>	
<%--</tbody>--%>
</div>