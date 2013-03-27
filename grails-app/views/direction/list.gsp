
<%@ page import="content.StrategicDirection" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'strategicDirection.label', default: 'StrategicDirection')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'strategicDirection.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="strategy" title="${message(code: 'strategicDirection.strategy.label', default: 'Strategy')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${strategicDirectionInstanceList}" status="i" var="strategicDirectionInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${strategicDirectionInstance.id}">${fieldValue(bean: strategicDirectionInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: strategicDirectionInstance, field: "strategy")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${strategicDirectionInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
