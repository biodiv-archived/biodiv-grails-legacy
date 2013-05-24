<%@ page import="content.eml.Document"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Document Tags')}" />
<title>${entityName} | ${Utils.getDomainName(request)}</title>
<r:require modules="core" />
</head>
<body>
	<div class="page-header" style="margin-left:20px;">
		<h1>
			${entityName}
		</h1>
	</div>
	<div style="margin-left:20px">
		<project:showTagsCloud model="[tagType:'document']"></project:showTagsCloud>
	</div>
</body>
</html>
