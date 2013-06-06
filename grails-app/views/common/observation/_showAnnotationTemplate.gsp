<g:if test="${annotations.size() > 0 }">
	<div>
		<table class="table table-hover tablesorter span8"
			style="margin-left: 0px;">
			<thead>
				<tr>
					<th>Name</th>
					<th>Value</th>
				</tr>
			</thead>
			<tbody class="mainContentList">
				<g:each in="${annotations}" status="i"
					var="annot">
					<tr class="mainContent">
						<td>
							${annot.key}
						</td>
						<td class="ellipsis multiline" style="max-width: 250px;">
							${annot.value}
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</g:if>