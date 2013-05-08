<div class="observations_list" style="top: 0px;">


	<g:render template="/document/showBrowserTable"
		model="['documentInstanceList':documentInstanceList]" />


	<% params['isGalleryUpdate'] = false; %>
	<div class="paginateButtons centered">
		<p:paginate controller="document" action="browser"
			total="${documentInstanceTotal}" userGroup="${userGroup}"
			userGroupWebaddress="${userGroupWebaddress}" params="${params}"
			max="${queryParams.max }" maxsteps="12" />
	</div>

</div>
