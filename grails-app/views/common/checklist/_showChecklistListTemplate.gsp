<g:if test="${instanceTotal > 0 }">
<div class="observations_list" style="clear:both;padding-top:50px;">
	<div>
		<table class="table table-hover span8 tablesorter" style="margin-left: 0px; clear:both;">
			<thead>
				<tr>
					<th>Title</th>
					<th>Species Group</th>
					<th>No. of Species</th>
					<th>Place Name</th>
				</tr>
			</thead>
			<tbody class="mainContentList" name="p${params?.offset}">
				<g:each in="${checklistInstanceList}" status="i"
					var="checklistInstance">
					<tr class="mainContent">
						<td><a href="${uGroup.createLink(controller:'checklist', action:'show', pos:i, id:checklistInstance.id, userGroupWebaddress:params.webaddress)}">${checklistInstance.title}</a></td>
						<td><sUser:interestedSpeciesGroups model="['userInstance':checklistInstance]" /></td>
						<td>${checklistInstance.speciesCount}</td>
						<td>${checklistInstance.placeName}</td>
					</tr>
				</g:each>
			</tbody>
		</table>
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
		activeFilters?.webaddress = userGroup?.webaddress
	%>
	
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<g:paginate total="${instanceTotal}" max="${queryParams?.max}"
				action="${params.action}" params="${activeFilters}" />
	</div>
</div>
</g:if>