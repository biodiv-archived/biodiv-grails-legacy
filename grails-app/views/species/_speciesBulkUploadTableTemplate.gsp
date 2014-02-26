<g:if test="${uploadList.size() > 0 }">
	<div>
		<table class="table table-hover tablesorter span8" style="margin-left: 0px;">
			<thead>
				<tr>
					<th>Start Date</th>
					<th>End Date</th>
					<sUser:ifOwns model="['user':user]">
						<th>File</th>
						<th>Status</th>
					</sUser:ifOwns>
					<th>Notes</th>
				</tr>
			</thead>
			<tbody class="mainContentList">
				<g:each in="${uploadList}" status="i"
					var="uploadLog">
					<tr class="mainContent">
						<td>${uploadLog.startDate}</td>
						<td>${uploadLog.endDate}</td>
						<sUser:ifOwns model="['user':user]">
							<td><a class="btn btn-mini" href="${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'params':[downloadFile:uploadLog.filePath])}">Download</a></td>
							<td><a class="btn btn-mini" href="#" onclick="rollBack($(this), '${uGroup.createLink(action:'rollBackUpload', controller:'species', id:uploadLog.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}')">${uploadLog.status}</a></td>
						</sUser:ifOwns>
						<td class="ellipsis multiline" style="max-width:250px;">${uploadLog.notes}</td>
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
