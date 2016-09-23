<%@page import="species.trait.TraitService"%>
<g:set var="traitService" bean="traitService"/>
<div class="row-fluid">
<g:each in="${traitValueInstanceList}" var="traitValue" status="i">

<g:if test="${i%4==0}">
	</div><div class="row-fluid"  style="margin-top:5px;">
</g:if>

<div class="span3 btn trait_btn btn-small">
<div class="svg_wrap">
<g:if test="${traitValue.icon}">
<%  def svgFile=new File('/home/ifp/git/biodiv/app-conf/img/traitIcons/'+traitValue.icon) %>

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
