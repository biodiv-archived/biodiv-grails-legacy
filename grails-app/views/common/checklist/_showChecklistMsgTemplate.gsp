<div id="info-message" class="info-message">
	<g:if test="${instanceTotal == 0}">
		<search:noSearchResults />
	</g:if>
	<g:else>
		<span class="name" style="color: #b1b1b1;margin-top: 10px;"><i
			class="icon-screenshot"></i> ${instanceTotal} </span>
			<g:message code="msg.Checklist" /><g:if test="${instanceTotal > 1}">s</g:if>
	</g:else>
</div>
