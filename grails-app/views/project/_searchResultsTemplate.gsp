<div class="observations_list observation" style="top: 0px;">

	<div class="mainContentList">
		<div class="mainContent"  name="l${params?.offset}">
			<ul class="list_view single_list_view thumbnails">
				<g:each in="${projectInstanceList}" status="i" var="projectInstance">
					<li class="thumbnail clearfix">
					
					<project:projectListItem
							model="['projectInstance':projectInstance]" />
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
							activeFilters?.webaddress = userGroup?.webaddress
						%>
	<div class="paginateButtons" style="visibility: hidden; clear: both">

		<center>
			<p:paginate total="${instanceTotal?:0}" action="${params.action}"
				controller="${params.controller?:'species'}"
				userGroup="${userGroup}"
				userGroupWebaddress="${userGroupWebaddress}"
				max="${queryParams.max}" params="${activeFilters}" />
		</center>
	</div>


</div>
