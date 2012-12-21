<div class="mainContentList">
<div class="mainContent" name="p${params?.offset}">
<%--<tbody class="mainContent">--%>
	<ul>
		<g:each in="${checklistInstanceList}" status="i"
			var="checklistInstance">
			<li>
			<tr>
				<td><a href="${uGroup.createLink(controller:'checklist', action:'show', pos:i, id:checklistInstance.id, userGroupWebaddress:params.webaddress)}">${checklistInstance.title}</a></td>
				<td>${checklistInstance.speciesGroup?.name}</td>
				<td>${checklistInstance.speciesCount}</td>
				<td>${checklistInstance.placeName}</td>
			</tr>
			</li>
		</g:each>
	</ul>	
<%--</tbody>--%>
</div>
			<g:if test="${instanceTotal > (queryParams.max?:0)}">
				<div class="centered">
					<div class="btn loadMore">
						<span class="progress" style="display: none;">Loading ... </span>
						<span class="buttonTitle">Load more</span>
					</div>
				</div>
			</g:if>
	
			<%
				activeFilters?.loadMore = true
				activeFilters?.append = true
				activeFilters?.webaddress = userGroup?.webaddress
			%>

			<div class="paginateButtons" style="visibility: hidden; clear: both">
				<p:paginate total="${instanceTotal?:0}" action="${params.action}" controller="${params.controller?:'checklist'}"
					userGroup="${userGroup}" userGroupWebaddress="${userGroupWebaddress}"
				 	max="${queryParams.max}" params="${activeFilters}" />
			</div>
</div>