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
			<b>Contributors:</b>
			${
			uFileInstance?.description
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
			${uFileInstance?.license}
		</p>
	</g:if>


</div>