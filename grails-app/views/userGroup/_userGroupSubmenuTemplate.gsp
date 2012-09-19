
<g:if test="${entityName}">
	<div class="page-header">
		<h1>
			${entityName}
		</h1>
	</div>
</g:if>


<g:if test="${flash.error}">
	<div class="alertMsg alert alert-error" style="clear: both;">
		${flash.error}
	</div>
</g:if>

<div class="alertMsg ${(flash.message)?'alert':'' }">
	${flash.message}
</div>
