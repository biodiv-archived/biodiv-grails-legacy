<div class="observations_list observation" style="top: 0px;">

<div class="mainContentList">
	<div class="mainContent">

		<ul class="list_view thumbnails">

			<g:if test="${instanceTotal > 0}">
				<g:each in="${speciesInstanceList}" status="i" var="speciesInstance">
					<li class="thumbnail clearfix">
						<s:showSnippet model="['speciesInstance':speciesInstance]" />
					</li>
				</g:each>
				<li>
					<div class="paginateButtons" style="clear: both">
						<center>
							<p:paginate total="${instanceTotal?:0}" action="${params.action}"
								controller="${params.controller?:'species'}"
								userGroup="${userGroup}"
								userGroupWebaddress="${userGroupWebaddress}"
								max="${queryParams.max}" params="${activeFilters}" />
						</center>
					</div>
				</li>

			</g:if>
		</ul>
	</div>
</div>
</div>