

<%@ page import="species.participation.Recommendation"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'recommendation.label', default: 'Recommendation')}" />
<title><g:message code="default.create.label"
		args="[entityName]" />
</title>
</head>
<body>

	<div class="container_16">
		<h1>
			<g:message code="default.create.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${recommendationInstance}">
			<div class="errors">
				<g:renderErrors bean="${recommendationInstance}" as="list" />
			</div>
		</g:hasErrors>
		<g:form action="save">
			<div class="dialog">
				<table>
					<tbody>
						<reco:create/>
					</tbody>
				</table>
			</div>
			<div class="buttons">
				<span class="button"><g:submitButton name="create"
						class="save" acction="save"
						value="${message(code: 'default.button.create.label', default: 'Create')}" />
				</span>
			</div>
		</g:form>
	</div>
</body>
</html>
