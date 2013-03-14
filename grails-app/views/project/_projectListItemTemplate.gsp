<div class="project-list-item" >
	<g:link url="${uGroup.createLink(controller:'project', action:'show', id:projectInstance.id, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" >
		<g:if test="${projectInstance.title}">
			<h2 class="project-title">projectInstance.title</h2>
		</g:if>
	</g:link>
	
	

</div>