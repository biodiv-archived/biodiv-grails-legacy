<div class="project-list-item" >

	<g:link url="${uGroup.createLink(controller:'project', action:'show', id:projectInstance.id, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" >
		<g:if test="${projectInstance.title}">
			<h5 class="project-title">${projectInstance.title}</h5>
		</g:if>
	</g:link>

	
	<div class="summary">
			<g:if test="${projectInstance.summary}">
			<div class="proj-summary">
			${projectInstance.summary}
			</div>
		</g:if>
	
	</div>
		
	

</div>