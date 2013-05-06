<div class="observations_list" style="top: 0px;">


	<g:render template="/document/showBrowserTable"
		model="['documentInstanceList':documentInstanceList]" />





	<g:if test="${documentInstanceTotal > (queryParams.max?:0)}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;">Loading ... </span> <span
					class="buttonTitle">Load more</span>
			</div>
		</div>
	</g:if>

	<%
		activeFilters?.loadMore = true
		activeFilters?.webaddress = userGroup?.webaddress
	%>

	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<p:paginate total="${documentInstanceTotal?:0}" action="browser"
			controller="${params.controller?:'document'}"
			userGroup="${userGroup}" userGroupWebaddress="${userGroupWebaddress}"
			max="${queryParams.max}" params="${activeFilters}" />
	</div>
</div>


