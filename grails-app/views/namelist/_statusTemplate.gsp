
<%@page import="species.NamesMetadata.NamePosition"%>
<g:if test="${position == NamePosition.RAW}">
<span class="label label-success status">${raw(status)}</span>
</g:if>
<g:elseif test="${position == NamePosition.WORKING}">
<span class="label status">${raw(status)}</span>
</g:elseif>
<g:else>
<span class="label label-warning status">${raw(status)}</span>
</g:else>

