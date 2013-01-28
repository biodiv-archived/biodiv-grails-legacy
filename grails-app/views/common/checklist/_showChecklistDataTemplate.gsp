<style>
.reco-comment-table {
	left:auto;
	right:0;
}
</style>
<div style="overflow:auto;">
	<table class="table table-hover tablesorter" style="margin-left: 0px;">
		
		<thead>
			<tr>
				<g:each in="${checklistInstance.fetchColumnNames()}" var="cName">
					<th title="${cName}">${cName.replaceAll("_", " ")}</th>
				</g:each>
				<th title="Comments">Comments</th>
			</tr>
		</thead>
		<tbody>
			<%
				def preRowNo = -1
				def prevRow = null
				def totalRows = checklistInstance.row.size()
			%>
			<g:each in="${checklistInstance.row}"  status="i" var="row">
				<%
					def currentRowNo = row.rowId
				%>
				<g:if test="${preRowNo !=  currentRowNo}">
					<g:if test="${preRowNo != -1}">
						<td>
							<comment:showCommentPopup model="['commentHolder':prevRow, 'rootHolder':checklistInstance]" />
						</td>
						</tr>
					</g:if>
					<%
						preRowNo = currentRowNo
						prevRow = row 
					%>
					<tr>
				</g:if>
				
				<g:if test="${row.reco}">
					<g:if test="${row.reco.taxonConcept && row.reco.taxonConcept.canonicalForm != null}">
						<td>
						<a href="${uGroup.createLink(action:'show', controller:'species', id:row.reco.taxonConcept.findSpeciesId(), 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
							<i> ${row.reco.taxonConcept.canonicalForm}</i>
						</a>
						</td>
					</g:if>
					<g:else>
						<td><i>${row.value}</i></td>
					</g:else>
				</g:if>
				<g:else>
					<td>${row.value}</td>
				</g:else>
				<g:if test="${totalRows > 1 && i == (totalRows-1)}">
					<td>
						<comment:showCommentPopup model="['commentHolder':prevRow, 'rootHolder':checklistInstance]" />
					</td>
				</g:if>
			</g:each>
		</tbody>
	</table>
</div>
