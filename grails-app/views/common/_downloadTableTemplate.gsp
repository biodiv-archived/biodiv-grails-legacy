<g:if test="${downloadLogList.size() > 0 }">
	<div>
		<table class="table table-hover tablesorter span8" style="margin-left: 0px;">
			<thead>
				<tr>
					<th>Date</th>
					<th>Filter Url</th>
					<th>File Type</th>
					<sUser:ifOwns model="['user':user]">
						<th>File</th>
					</sUser:ifOwns>
					<th>Notes</th>
				</tr>
			</thead>
			<tbody class="mainContentList">
				<g:each in="${downloadLogList}" status="i"
					var="downloadLog">
					<tr class="mainContent">
						<td>${downloadLog.createdOn}</td>
						<td><a href="${downloadLog.filterUrl}" title="${downloadLog.filterUrl}">filter url</a></td>
						<td>${downloadLog.type}</td>
						<sUser:ifOwns model="['user':user]">
							<td><a class="btn btn-mini" href="${uGroup.createLink(action:'downloadFile', controller:'observation', id:downloadLog.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">Download</a></td>
						</sUser:ifOwns>
						<td>${downloadLog.notes}</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</g:if>
<g:javascript>
$(document).ready(function(){
});
</g:javascript>
