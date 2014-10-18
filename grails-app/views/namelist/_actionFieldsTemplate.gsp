<% def span_final = 8; def input_width = 156; %>
<g:if test="${showArrow}">
	<% span_final = 6; input_width = 123;%>
	<div class="span2" style="margin:0px;">
		<button class="btn btn-mini"><i class="icon-chevron-left"></i></button>
	</div>
</g:if>
<div class="span2" style="margin:0px;">
	<button class="btn btn-mini"><i class="icon-trash"></i></button>
</div>
<div class="span2" style="margin:0px;">
	<button class="btn btn-mini"><i class="icon-refresh"></i></button>
</div>
<div class="span${span_final}" style="margin:0px;">	
	<input type="text" placeholder="Search" class="span12" style="width:${input_width}px;margin:0px;min-height: 22px;"/>
</div>
