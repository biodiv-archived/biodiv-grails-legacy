
<%@ page import="species.Resource" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'resource.label', default: 'Resource')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'resource.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="fileName" title="${message(code: 'resource.fileName.label', default: 'File Name')}" />
                        
                            <g:sortableColumn property="url" title="${message(code: 'resource.url.label', default: 'Url')}" />
                        
                            <g:sortableColumn property="description" title="${message(code: 'resource.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="mimeType" title="${message(code: 'resource.mimeType.label', default: 'Mime Type')}" />
                        
                            <g:sortableColumn property="type" title="${message(code: 'resource.type.label', default: 'Type')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${resourceInstanceList}" status="i" var="resourceInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${resourceInstance.id}">${fieldValue(bean: resourceInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: resourceInstance, field: "fileName")}</td>
                        
                            <td>${fieldValue(bean: resourceInstance, field: "url")}</td>
                        
                            <td>${fieldValue(bean: resourceInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: resourceInstance, field: "mimeType")}</td>
                        
                            <td>${fieldValue(bean: resourceInstance, field: "type")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${resourceInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
