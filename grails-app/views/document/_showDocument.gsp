<div class="show-document">


	<fileManager:displayIconName id="${documentInstance?.uFile?.id}" />

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
	<g:if test="${documentInstance?.uFile?.license}">
		<p>
			<b>License:</b>
			
							<a class="license" href="${documentInstance?.uFile?.license?.url}" target="_blank"><img
						src="${createLinkTo(dir:'images/license', file: documentInstance?.uFile?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
						alt="${documentInstance?.uFile?.license?.name.value()}" /> </a>
			
		</p>
	</g:if>


</div>