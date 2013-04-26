<div class="show-ufile">


	<fileManager:displayIconName id="${uFileInstance?.id}" />

	<g:if test="${uFileInstance?.description}">
		<p>
			<b>Description:</b>
			${uFileInstance?.description }
		</p>
	</g:if>


	<g:if test="${uFileInstance?.contributors}">
		<p>
			<b>Contributor(s):</b>
			${
			uFileInstance?.contributors
		}
		</p>
	</g:if>
	<g:if test="${uFileInstance?.attribution}">
		<p>
			<b>Attribution:</b>
			${uFileInstance?.attribution}
		</p>
	</g:if>
	<g:if test="${uFileInstance?.license}">
		<p>
			<b>License:</b>
			
							<a class="license" href="${uFileInstance?.license?.url}" target="_blank"><img
						src="${createLinkTo(dir:'images/license', file: uFileInstance?.license?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
						alt="${uFileInstance?.license?.name.value()}" /> </a>
			
		</p>
	</g:if>


</div>