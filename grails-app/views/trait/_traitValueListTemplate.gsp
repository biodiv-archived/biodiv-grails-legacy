<%@page import="species.trait.TraitService"%>
<g:set var="traitService" bean="traitService"/>
<div class="row-fluid">
<g:each in="${traitValueInstanceList}" var="traitValue" status="i">

<g:if test="${i%rows==0}">
	</div><div class="row-fluid"  style="margin-top:5px;">
</g:if>

<div class="${(rows == 6)?'span2':'span3'} btn trait_btn btn-small" data-id='${traitValue.id}' data-name='${traitName}'>
<div class="svg_wrap">
<g:if test="${traitValue.icon}">
<img src="test.jpg"  width="20" height="20" />
</g:if>
<g:else>
<img src="test.jpg"  width="20" height="20" />
</g:else>
</div>
<div class="trait_label" title="${traitValue.value}">${traitValue.value}</div>
</div>

</g:each>
</div>
