<div class="observations_list" style="top: 0px; clear: both;">
	<g:render template="/discussion/showDiscussionListTemplate"
		model="['discussionInstanceList':discussionInstanceList, canPullResource:canPullResource, 'userGroupInstance':userGroupInstance]" />
		
		
	<% params['isGalleryUpdate'] = false; %>
	<div class="paginateButtons centered">
		<p:paginate controller="discussion" action="list"
			total="${instanceTotal}" userGroup="${userGroup}"
			userGroupWebaddress="${params.webaddress}" params="${activeFilters}"
			max="${queryParams.max }" maxsteps="12" />
	</div>

</div>
