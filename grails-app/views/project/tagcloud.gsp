<%@page import="species.utils.Utils"%>
<%@ page import="content.Project"%>
<html>
<head>
<g:set var="title" value="Project Tags"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
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
