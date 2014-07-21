<g:if test="${uploadList.size() > 0 }">
	<div>
		<table class="table table-hover tablesorter span8" style="margin-left: 0px;">
			<thead>
				<tr>
					<th>Start Date</th>
					<th>End Date</th>
					<th>Status</th>
					<sUser:ifOwns model="['user':user]">
						<th>Data File</th>
						<th>Log File</th>
						<th>Abort</th>
						<th>Rollback</th>
					</sUser:ifOwns>
				</tr>
			</thead>
			<tbody class="mainContentList">
				<g:each in="${uploadList}" status="i"
					var="uploadLog">
					<tr class="mainContent">
						<td>${uploadLog.startDate}</td>
						<td>${uploadLog.endDate}</td>
						<td>${species.utils.Utils.getTitleCase(uploadLog.status.value())}</td>
						<sUser:ifOwns model="['user':user]">
							<td><a class="btn btn-mini" href="${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'params':[downloadFile:uploadLog.filePath])}">Download</a></td>
							<td><g:if test="${uploadLog.errorFilePath}"><a class="btn btn-mini" href="${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'params':[downloadFile:uploadLog.errorFilePath])}">Download</a></g:if></td>
							<td><g:if test="${uploadLog.status.value() == 'RUNNING'}"><a class="btn btn-mini btn-danger" href="#" onclick="updateBulkUploadStatus($(this), '${uGroup.createLink(action:'abortBulkUpload', controller:'species', id:uploadLog.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}', 'Abort')">Abort</a></g:if></td>
							<td><g:if test="${(uploadLog.status.value() != 'RUNNING') && (uploadLog.status.value() != 'ROLLBACK')}"><a class="btn btn-mini  btn-danger" href="#" onclick="updateBulkUploadStatus($(this), '${uGroup.createLink(action:'rollBackUpload', controller:'species', id:uploadLog.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}', 'Rollback')">Rollback</a></g:if></td>
						</sUser:ifOwns>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</g:if>
