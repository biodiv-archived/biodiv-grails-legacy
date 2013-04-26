
<%@ page import="content.eml.Document" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'document.label', default: 'Document')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <r:require modules="core" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'document.id.label', default: 'Id')}" />
                        
                            <th><g:message code="document.coverage.label" default="Coverage" /></th>
                        
                            <g:sortableColumn property="title" title="${message(code: 'document.title.label', default: 'Title')}" />
                        
                            <g:sortableColumn property="type" title="${message(code: 'document.type.label', default: 'Type')}" />
                        
                            <th><g:message code="document.uFile.label" default="UF ile" /></th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${documentInstanceList}" status="i" var="documentInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${documentInstance.id}">${fieldValue(bean: documentInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: documentInstance, field: "coverage")}</td>
                        
                            <td>${fieldValue(bean: documentInstance, field: "title")}</td>
                        
                            <td>${fieldValue(bean: documentInstance, field: "type")}</td>
                        
                            <td>${fieldValue(bean: documentInstance, field: "uFile")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${documentInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
