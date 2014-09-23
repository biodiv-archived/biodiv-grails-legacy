<g:if test="${speciesFieldInstance?.description}">
	<a id="uBioIframeLink"
		href="http://www.ubio.org/browser/search.php?search_all=${speciesFieldInstance?.description.encodeAsHTML()}">
		${speciesFieldInstance.field.category}
	</a>
	<iframe class="iframe" id="uBioIframe" frameborder="0"><g:message code="msg.iframe.capable.browser.required" />	</iframe>
</g:if>
<g:else>
	<a id="uBioIframeLink"
		href="http://www.ubio.org/browser/search.php?search_all=${speciesInstance.name.genus + ' '+speciesInstance.name.species}">
		${speciesFieldInstance.field.category}
	</a>
	<iframe class="iframe" id="uBioIframe" frameborder="0"><g:message code="msg.iframe.capable.browser.required" /></iframe>
</g:else>
