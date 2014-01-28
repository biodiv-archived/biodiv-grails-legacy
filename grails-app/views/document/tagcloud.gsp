<%@ page import="content.eml.Document"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="Document Tags"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
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
