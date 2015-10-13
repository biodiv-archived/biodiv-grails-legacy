<g:if test="${uploadList.size() > 0 }">
	<div>
		<table class="table table-hover tablesorter span8" style="margin-left: 0px;">
			<thead>
				<tr>
					<th><g:message code="speciesbulkuploadtable.start.date" /> </th>
					<th><g:message code="speciesbulkuploadtable.end.date" /></th>
					<th><g:message code="speciesbulkuploadtable.status" /></th>
					<sUser:ifOwns model="['user':user]">
						<th><g:message code="speciesbulkuploadtable.data.file" /></th>
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
							<g:if test="${uploadLog.status.value() == 'SUCCESS'}">
								<td><a class="btn btn-mini" href="${uGroup.createLink(action:'downloadSpeciesFile', controller:'UFile', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'params':[downloadFile:uploadLog.filePath])}"><g:message code="button.download" /></a></td>
							</g:if>
						</sUser:ifOwns>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</g:if>
