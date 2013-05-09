<div class="observations_list" style="clear: both; top: 0px;">
	<div class="project-list tab-content span">
		<g:each in="${projectInstanceList}" status="i"
			var="projectInstance">
			<div class="${(i % 2) == 0 ? 'odd' : 'even'} item">
				<project:projectListItem
					model="['projectInstance':projectInstance, 'pos':i]" />
			</div>
		</g:each>
	</div>
	<% params['isGalleryUpdate'] = false; %>
	<div class="paginateButtons centered" style="clear: both; padding-top: 30px;">
		<p:paginate controller="project" action="list"
			total="${instanceTotal}" userGroup="${userGroup}"
			userGroupWebaddress="${userGroupWebaddress}" params="${params}"
			max="${queryParams.max }" maxsteps="12" />
	</div>
</div>