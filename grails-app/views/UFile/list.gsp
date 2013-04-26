
<%@ page import="content.fileManager.UFile" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'UFile.label', default: 'UFile')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'UFile.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="size" title="${message(code: 'UFile.size.label', default: 'Size')}" />
                        
                            <g:sortableColumn property="path" title="${message(code: 'UFile.path.label', default: 'Path')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'UFile.name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="description" title="${message(code: 'UFile.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="extension" title="${message(code: 'UFile.extension.label', default: 'Extension')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${UFileInstanceList}" status="i" var="UFileInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${UFileInstance.id}">${fieldValue(bean: UFileInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: UFileInstance, field: "size")}</td>
                        
                            <td>${fieldValue(bean: UFileInstance, field: "path")}</td>
                        
                            <td>${fieldValue(bean: UFileInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: UFileInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: UFileInstance, field: "extension")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${UFileInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
