
<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<r:require modules="core" />


</head>
<body>
    <div class="span12">
        <g:render template="/project/projectSubMenuTemplate"
            model="['entityName':'Project Tags']" />
        <uGroup:rightSidebar />

		<project:showTagsCloud model="[tagType:'project']"></project:showTagsCloud>
    </div>
	
</body>
</html>
