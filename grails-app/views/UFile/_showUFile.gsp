<div class="show-ufile">


	<fileManager:displayIconName id="${uFileInstance?.id}" />

	<g:if test="${uFileInstance?.description}">
		<p>
			<b>Description:</b>
			${uFileInstance?.description }
		</p>
	</g:if>
	<p></p>
	<g:if test="${uFileInstance?.contributors}">
		<p>
			Contributors:
			${
			uFileInstance?.description
		}
		</p>
	</g:if>
	<g:if test="${uFileInstance?.attribution}">
		<p>
			Attribution:
			${uFileInstance?.attribution}
		</p>
	</g:if>
	<g:if test="${uFileInstance?.license}">
		<p>
			License:
			${uFileInstance?.license}
		</p>
	</g:if>


</div>