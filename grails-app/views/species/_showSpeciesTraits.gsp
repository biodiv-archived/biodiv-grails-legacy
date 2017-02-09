	
<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'facts.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
    </head>
    <body>
    	<%= trait %>
    </body>
</html>

