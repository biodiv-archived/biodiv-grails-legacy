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
				<th title="Observation">Observation</th>
				<th title="Comments">Comments</th>
			</tr>
		</thead>
		<tbody>
			<g:each in="${checklistInstance.observations}" var="observation">
				<tr>
					<g:each in="${observation.fetchChecklistAnnotation()}" var="annot">
						<td>
							<g:if test="${annot.key.equalsIgnoreCase('scientific_name')}">
								<g:if test="${observation.maxVotedReco?.taxonConcept && observation.maxVotedReco.taxonConcept?.canonicalForm != null}">
									<a href="${uGroup.createLink(action:'show', controller:'species', id:observation.maxVotedReco.taxonConcept.findSpeciesId(), 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
										<i> ${observation.maxVotedReco.taxonConcept.canonicalForm}</i>
									</a>
								</g:if>
								<g:else>
									<i>${annot.value}</i>
								</g:else>
							</g:if>
							<g:else>
								${annot.value}
							</g:else>
						</td>
					</g:each>
					<td>
						<a href="${uGroup.createLink(action:'show', controller:'observation', id:observation.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
							url</a>
					</td>
					<td>
						<comment:showCommentPopup model="['commentHolder':observation, 'rootHolder':checklistInstance]" />
					</td>
				</tr>
			</g:each>	
		</tbody>
	</table>
</div>
