<div class="observations_list observation" style="clear: both;">

	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">
			<ul class="grid_view thumbnails obvListwrapper">
				<g:each in="${instanceList}" status="i" var="instance">
					<li class="thumbnail" style="clear: both;margin-left:0px;width:100%;">
                    <g:render template="/trait/showTraitTemplate" model="['trait':instance]"/>
					</li>
				</g:each>
			</ul>			
		</div>
	</div>
        
    <g:if test="${instanceTotal > (queryParams.max?:0)}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;"><g:message code="msg.loading" /></span> <span
					class="buttonTitle"><g:message code="msg.load.more" /></span>
			</div>
		</div>
	</g:if>
	
	<%
		activeFilters?.loadMore = true
		activeFilters?.webaddress = userGroupInstance?.webaddress
	%>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<p:paginate total="${instanceTotal?:0}" action="${params.action}" controller="${params.controller?:'observation'}"
			userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress?:params.webaddress}"
			 max="${queryParams.max}" params="${activeFilters}" />
	</div>	
</div>
