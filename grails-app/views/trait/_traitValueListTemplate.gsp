<%@page import="species.trait.TraitService"%>
<g:set var="traitService" bean="traitService"/>
<h6>${traitName} </h6>
<div class="row-fluid">
<g:each in="${traitValueInstanceList}" var="traitValue" status="i">
<g:if test="${i%(rows-1)==0}">
	</div><div class="row-fluid"  style="margin-top:5px;">
</g:if>
<g:if test="${i==0}">
<div class="${(rows == 6)?'span2':'span3'} btn trait_btn btn-small" data-id='all' data-name='${traitName}'>
	<div class="trait_label" title="All/Any">All</div>
</div>
</g:if>
<div class="${(rows == 6)?'span2':'span3'} btn trait_btn btn-small" data-id='${traitValue.id}' data-name='${traitName}'>
 
<div class="svg_wrap">
<g:if test="${traitValue.icon}">
<%  def svgFile=new File('${grailsApplication.config.speciesPortal.app.rootDir}/img/traitIcons/'+traitValue.icon) %>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" xml:space="preserve" shape-rendering="geometricPrecision" text-rendering="geometricPrecision" image-rendering="optimizeQuality" viewBox="0 0 200 200" width="40" height="40">
<%= svgFile.getText()%>
</svg>
</g:if>
<g:else>
<img src="test.jpg"  width="20" height="20" />
</g:else>
</div>

<div class="trait_label" title="${traitValue.value}">${traitValue.value}</div>
</div>

</g:each>
</div>
