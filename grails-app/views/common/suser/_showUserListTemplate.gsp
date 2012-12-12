<div class="users_list">
	<div class="mainContentList">
		<div class="mainContent">
			<ul class="grid_view thumbnails">
	
				<g:each in="${userInstanceList}" status="i" var="userInstance">
					<g:if test="${i% ((params.controller != 'userGroup')?3:3) == 0}">
						<li class="thumbnail" style="clear: both;">
					</g:if>
					<g:else>
						<li class="thumbnail" style="margin:0px;">
					</g:else>
					<sUser:showUserSnippetTablet model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserSnippetTablet>
					</li>
				</g:each>
			</ul>
	
	
			<ul class="list_view thumbnails" style="display: none;">
				<g:each in="${userInstanceList}" status="i" var="userInstance">
					<li class="thumbnail" style="clear: both;"><sUser:showUserSnippet
							model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserSnippet></li>
				</g:each>
			</ul>
		</div>
	</div>

	<g:if test="${userInstanceTotal > params.max}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;">Loading ... </span> <span
					class="buttonTitle">Load more</span>
			</div>
		</div>
	</g:if>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<p:paginate total="${userInstanceTotal}" max="${params.max}" action="${params.action}" controller="${params.controller?:'user'}"
			userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress}"
			params="${params}" />
	</div>
</div>

