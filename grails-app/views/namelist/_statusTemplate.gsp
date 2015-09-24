
<%@page import="species.NamesMetadata.NamePosition"%>
<sUser:isAdmin>
<g:if test="${taxon}">
<a class="status_a" href="${uGroup.createLink(controller:'namelist', 'taxon':taxon.id, absolute:true)}">
</g:if>
</sUser:isAdmin>
<g:if test="${position == NamePosition.RAW}">
<span class="label label-success status dirty_list" title="${g.message(code:'namelist.raw.description')}">${raw(status)}</span>
</g:if>
<g:elseif test="${position == NamePosition.WORKING}">
<span class="label status working_list" title="${g.message(code:'namelist.working.description')}">${raw(status)}</span>
</g:elseif>
<g:else>
<span class="label label-warning status clean_list" title="${g.message(code:'namelist.clean.description')}">${raw(status)}</span>
</g:else>
<sUser:isAdmin>
<g:if test="${taxon}">
</a>
</g:if>
</sUser:isAdmin>
