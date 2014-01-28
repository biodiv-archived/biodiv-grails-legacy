<g:if test="${annotations.size() > 0 }">
	<div>
		<table class="table table-hover"
			style="margin-left: 0px;">
			<tbody class="mainContentList">
				<g:each in="${annotations}" status="i"
					var="annot">
					<tr class="mainContent">
						<td>
							${annot.key.replaceAll("_", " ").capitalize()}
						</td>
						<td class="ellipsis multiline">
							${annot.value}
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</g:if>