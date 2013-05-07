<div class="show-document">


	
	<fileManager:displayFile
							filePath="${ documentInstance?.uFile?.path}"
							></fileManager:displayFile>

	<g:if test="${documentInstance?.description}">
		<p>
			<b>Description:</b>
			${documentInstance?.description }
		</p>
	</g:if>


	<g:if test="${documentInstance?.contributors}">
		<p>
			<b>Contributor(s):</b>
			${
			documentInstance?.contributors
		}
		</p>
	</g:if>
	<g:if test="${documentInstance?.attribution}">
		<p>
			<b>Attribution:</b>
			${documentInstance?.attribution}
		</p>
	</g:if>
	<g:if test="${documentInstance?.license}">
		<p>
			<b>License:</b>
			
							<a class="license" href="${documentInstance?.license?.url}" target="_blank"><img
						src="${createLinkTo(dir:'images/license', file: documentInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
						alt="${documentInstance?.license?.name.value()}" /> </a>
			
		</p>
	</g:if>


</div>