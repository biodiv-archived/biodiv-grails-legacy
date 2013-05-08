<table class="show-document">


	<tr>
		<td class="prop"><span class="grid_3 name">File</span></td>
		<td class="linktext"><fileManager:displayFile
				filePath="${ documentInstance?.uFile?.path}"></fileManager:displayFile>

		</td>

		<g:if test="${documentInstance?.description}">

			<tr>
				<td class="prop"><span class="grid_3 name">Description</span></td>
				<td class="linktext">
					${documentInstance?.description }
				</td>
			</tr>
		</g:if>


		<g:if test="${documentInstance?.contributors}">
			<tr>
				<td class="prop"><span class="grid_3 name">Contributor(s)</span>
			</td>
			<td class="linktext">
				${
			documentInstance?.contributors
		}
			</td>
	</tr>
	</g:if>
	<g:if test="${documentInstance?.attribution}">
		<tr>
			<td class="prop"><span class="grid_3 name">Attribution</span></td>
			<td class="linktext">
				${documentInstance?.attribution}
			</td>

		</tr>
	</g:if>
	<g:if test="${documentInstance?.license}">
		<tr>
			<td class="prop"><span class="grid_3 name">License</span></td>

			<td class="linktext"><a class="license"
				href="${documentInstance?.license?.url}" target="_blank"><img
					src="${createLinkTo(dir:'images/license', file: documentInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
					alt="${documentInstance?.license?.name.value()}" /> </a></td>

		</tr>
	</g:if>


</table>