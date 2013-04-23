<g:if test="${observationInstance.metaData.size() > 0 }">
	<div>
		<table class="table table-hover tablesorter span8" style="margin-left: 0px;">
			<thead>
				<tr>
					<th>Name</th>
					<th>Value</th>
				</tr>
			</thead>
			<tbody class="mainContentList">
				<g:each in="${observationInstance.metaData}" status="i"
					var="metaDataRow">
					<tr class="mainContent">
						<td>${metaDataRow.key}</td>
						<td class="ellipsis multiline" style="max-width:250px;">${metaDataRow.value}</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</g:if>
