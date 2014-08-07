<div class="observations_list" style="clear: both;top:0px;">
	<g:if test="${instanceTotal > 0 }">
		<div>
			<table class="table table-hover span8 tablesorter"
				style="margin-left: 0px; clear: both;">
				<thead>
					<tr>
						<th><g:message code="msg.Title" /></th>
						<th><g:message code="msg.Species.Group" /></th>
						<th><g:message code="msg.Species.Number" /></th>
						<th><g:message code="msg.Place.Name" /></th>
					</tr>
				</thead>
				<tbody class="mainContentList" name="p${params?.offset}">
					<g:each in="${checklistInstanceList}" status="i"
						var="checklistInstance">
						<tr class="mainContent">
							<td><a
								href="${uGroup.createLink(controller:'checklist', action:'show', pos:i, id:checklistInstance.id, userGroupWebaddress:params.webaddress)}">
									${checklistInstance.title}
							</a>
							</td>
							<td><sUser:interestedSpeciesGroups
									model="['userInstance':checklistInstance]" />
							</td>
							<td>
								${checklistInstance.speciesCount}
							</td>
							<td>
								${checklistInstance.placeName}
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>

		<g:if test="${instanceTotal > (queryParams.max?:0)}">
			<div class="centered">
				<div class="btn loadMore">
					<span class="progress" style="display: none;"><g:message code="msg.Loading" /> ... </span> <span
						class="buttonTitle"><g:message code="msg.Load more" /></span>
				</div>
			</div>
		</g:if>

		<%
		activeFilters?.loadMore = true
		activeFilters?.webaddress = userGroup?.webaddress
	%>

		<div class="paginateButtons" style="visibility: hidden; clear: both">
			<p:paginate total="${instanceTotal?:0}" action="${params.action}"
				controller="${params.controller?:'checklist'}"
				userGroup="${userGroup}"
				userGroupWebaddress="${userGroupWebaddress}"
				max="${queryParams.max}" params="${activeFilters}" />
		</div>
	</g:if>
</div>
