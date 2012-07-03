<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require modules="species"/>
<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title><g:message code="default.create.label"
		args="[entityName]" />
</title>
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a class="home"
			href="${createLink(uri: '/')}"><g:message
					code="default.home.label" />
		</a>
		</span> <span class="menuButton"><g:link class="list" action="list">
				<g:message code="default.list.label" args="[entityName]" />
			</g:link>
		</span>
	</div>
	<div class="body">
		<h1>
			<g:message code="default.create.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${speciesInstance}">
			<div class="errors">
				<g:renderErrors bean="${speciesInstance}" as="list" />
			</div>
		</g:hasErrors>
		<fileuploader:form upload="docs" successAction="upload"
			successController="spreadsheet" errorAction="error"
			errorController="test"/>
	</div>
</body>
</html>
