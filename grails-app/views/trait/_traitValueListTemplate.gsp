<%@page import="species.trait.TraitService"%>
<g:set var="traitService" bean="traitService"/>
<g:if test="${hasLabel}">
<h6>${traitName} </h6>
</g:if>
<div class="row-fluid">
<g:each in="${traitValueInstanceList}" var="traitValue" status="i">
<g:if test="${i%(rows-1)==0}">
	</div><div class="row-fluid" style="margin-top:2px;">
</g:if>
<g:if test="${i==0}">
<div class="${(rows == 6)?'span2':'span3'} btn trait_btn btn-small" data-id='all' data-name='${traitName.replaceAll(' ','_').toLowerCase()}'>
	<div class="trait_label" title="All/Any">Any</div>
</div>
</g:if>
<div class="${(rows == 6)?'span2':'span3'} btn trait_btn btn-small" data-id='${traitValue.id}' data-name='${traitName.replaceAll(' ','_').toLowerCase()}'>
<%traitValue.icon = traitValue.icon.replaceAll('.svg','.png')%> 
<div class="svg_wrap">

<%  def iconFile %>
<g:if test="${(new File(grailsApplication.config.speciesPortal.app.rootDir+'/traitsIcons/32/32_'+traitValue.icon)).exists()}">
<% iconFile=grailsApplication.config.speciesPortal.resources.serverURL+'/traitsIcons/32/32_'+traitValue.icon %>
</g:if>
<g:else>
<% iconFile=grailsApplication.config.speciesPortal.resources.serverURL+"/nimage.png"; %>
</g:else>
<img src="${iconFile}" width="32" height="32" />
</div>

<div class="trait_label ellipsis_trait" title="${traitValue.value}">${traitValue.value}</div>
</div>

</g:each>
</div>
