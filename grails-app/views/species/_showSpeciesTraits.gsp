/*<div>
    <g:render template="/species/factsTable" model="['factsList':speciesInstance.getFacts(), hideSubject:true]"/>
</div>*/
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

