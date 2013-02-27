<g:if test="${downloadLogList.size() > 0 }">
	<div>
		<table class="table table-hover tablesorter span8" style="margin-left: 0px;">
			<thead>
				<tr>
					<th>Date</th>
					<th>Filter Url</th>
					<th>File Type</th>
					<th>File</th>
					<th>Notes</th>
				</tr>
			</thead>
			<tbody class="mainContentList">
				<g:each in="${downloadLogList}" status="i"
					var="downloadLog">
					<tr class="mainContent">
						<td>${downloadLog.dateCreated}</td>
						<td><a href="${downloadLog.filterUrl}" title="${downloadLog.filterUrl}">filter url</a></td>
						<td>${downloadLog.type}</td>
						<td><a href="${uGroup.createLink(action:'downloadFile', controller:'observation', id:downloadLog.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">click here to download</a></td>
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
