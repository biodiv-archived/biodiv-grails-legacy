
<g:if test="${entityName}">
	<div class="page-header">
		<h1>
			${entityName}
		</h1>
	</div>
</g:if>

<g:if test="${flash.message}">
	<div class="message alert alert-info">
		${flash.message}
	</div>
</g:if>
<g:hasErrors bean="${observationInstance}">
	<i class="icon-warning-sign"></i>
	<span class="label label-important"> <g:message
			code="fix.errors.before.proceeding" default="Fix errors" /> </span>
	<%--<g:renderErrors bean="${observationInstance}" as="list" />--%>
</g:hasErrors>
