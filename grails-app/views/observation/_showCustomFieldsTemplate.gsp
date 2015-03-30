<g:if test="${customFields?.size() > 0}">
<div style="margin-top:8px;" class="sidebar_section">
	<h5><g:message code="heading.customfields" /></h5>
	<div>
		<table class="table"
			style="margin-left: 0px;">
			<tbody>
				<g:each in="${customFields}" status="i"
					var="annot">
					<tr>
						<td>
							${annot.key}
						</td>
						<td>
							${annot.value}
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</div>  
</g:if>
