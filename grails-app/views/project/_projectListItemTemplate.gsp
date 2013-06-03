<div class="project-list-item observation_story">

	<g:link
		url="${uGroup.createLink(controller:'project', action:'show', id:projectInstance.id, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress, params:['pos':pos])}">
		<g:if test="${projectInstance.title}">
			<h5 class="project-title">
				${projectInstance.title}
			</h5>
		</g:if>
	</g:link>


	<div class="summary">
		<g:if test="${projectInstance.summary}">
			<div class="proj-summary">
				${projectInstance.summary}
			</div>
		</g:if>

	</div>

	<g:if test="${projectInstance.granteeOrganization}">
		<div class="proj-grantee" style="padding-top: 10px;">
			<b> Grantee :</b>
			${projectInstance.granteeOrganization}
		</div>

	</g:if>

</div>
