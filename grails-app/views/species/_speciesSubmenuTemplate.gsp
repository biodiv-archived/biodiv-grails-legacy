
<g:if test="${entityName}">
	<div class="page-header">
		<s:showHeadingAndSubHeading model="['heading':entityName, 'subHeading':subHeading, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]"/>		
	</div>
</g:if>

<g:hasErrors bean="${speciesInstance}">
	<i class="icon-warning-sign"></i>
	<span class="label label-important"> <g:message
			code="fix.errors.before.proceeding" default="Fix errors" /> </span>
	<%--<g:renderErrors bean="${observationInstance}" as="list" />--%>
</g:hasErrors>
