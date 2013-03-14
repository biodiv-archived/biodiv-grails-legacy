
<%@ page import="content.Project" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <r:require modules="core"/>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'project.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="title" title="${message(code: 'project.title.label', default: 'Title')}" />
                        
                            <th><g:message code="project.direction.label" default="Direction" /></th>
                        
                            <g:sortableColumn property="granteeURL" title="${message(code: 'project.granteeURL.label', default: 'Grantee URL')}" />
                        
                            <g:sortableColumn property="granteeName" title="${message(code: 'project.granteeName.label', default: 'Grantee Name')}" />
                        
                            <g:sortableColumn property="grantFrom" title="${message(code: 'project.grantFrom.label', default: 'Grant From')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${projectInstanceList}" status="i" var="projectInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${projectInstance.id}">${fieldValue(bean: projectInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: projectInstance, field: "title")}</td>
                        
                            <td>${fieldValue(bean: projectInstance, field: "direction")}</td>
                        
                            <td>${fieldValue(bean: projectInstance, field: "granteeURL")}</td>
                        
                            <td>${fieldValue(bean: projectInstance, field: "granteeName")}</td>
                        
                            <td><g:formatDate date="${projectInstance.grantFrom}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${projectInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
