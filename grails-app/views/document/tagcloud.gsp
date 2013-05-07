
<%@ page import="content.eml.Document"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Document')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<r:require modules="core" />


</head>
<body>


		<div class="page-header" style="margin-left:20px;">
			<h1>
				Document Tags
			</h1>
		</div>
<div style="margin-left:20px">
			<tc:tagCloud bean="${Document}" controller="Document" action="browser" sort="${true}" style
				color="${[start: '#084B91', end: '#9FBBE5']}"
				size="${[start: 12, end: 30, unit: 'px']}" paramName='tag' />

	</div>
</body>
</html>
