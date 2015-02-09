<g:if test="${downloadLogList.size() > 0 }">
	<div>
		<table class="table table-hover tablesorter span8" style="margin-left: 0px;">
			<thead>
				<tr>
					<th><g:message code="default.date.label" /></th>
					<th><g:message code="default.filter.url.label" /> </th>
					<th><g:message code="default.file.type" /> </th>
					<sUser:ifOwns model="['user':user]">
						<th><g:message code="default.file.label" /></th>
					</sUser:ifOwns>
					<th><g:message code="default.category.label" /></th>
					<th><g:message code="default.notes.label" /></th>
				</tr>
			</thead>
			<tbody class="mainContentList">
				<g:each in="${downloadLogList}" status="i"
					var="downloadLog">
					<tr class="mainContent">
                        <td>${downloadLog.createdOn}</td>
                        <%
                        def filterUrl = downloadLog.filterUrl;
                        if(downloadLog.sourceType == 'Unique Species') {
                            filterUrl = filterUrl.replace("distinctReco", 'list');
                        }
                        %>
						<td><a href="${filterUrl}" title="${filterUrl}"><g:message code="button.filter.url" /> </a></td>
						<td>${downloadLog.type}</td>
						<sUser:ifOwns model="['user':user]">
							<td><a class="btn btn-mini" href="${uGroup.createLink(action:'downloadFile', controller:'observation', id:downloadLog.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"><g:message code="button.download" /></a></td>
						</sUser:ifOwns>
						<td>${downloadLog.sourceType}</td>
						<td class="ellipsis multiline" style="max-width:250px;">${downloadLog.notes}</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</g:if>
