
<%@page import="species.NamesMetadata.NamePosition"%>
<g:if test="${taxon}">
<a class="status_a" href="${uGroup.createLink(controller:'namelist', 'taxon':taxon.id, absolute:true)}">
</g:if>
<g:if test="${position == NamePosition.RAW}">
<span class="label label-success status dirty_list" title="This name appears in the IBP Raw list">${raw(status)}</span>
</g:if>
<g:elseif test="${position == NamePosition.WORKING}">
<span class="label status working_list" title="This name appears in the IBP Working list">${raw(status)}</span>
</g:elseif>
<g:else>
<span class="label label-warning status clean_list" title="This name appears in the IBP Clean list">${raw(status)}</span>
</g:else>
<g:if test="${taxon}">
</a>
</g:if>
