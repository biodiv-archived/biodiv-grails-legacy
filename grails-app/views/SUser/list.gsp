
<%@ page import="species.auth.SUser" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'SUser.label', default: 'SUser')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'SUser.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="username" title="${message(code: 'SUser.username.label', default: 'Username')}" />
                        
                            <g:sortableColumn property="password" title="${message(code: 'SUser.password.label', default: 'Password')}" />
                        
                            <g:sortableColumn property="accountExpired" title="${message(code: 'SUser.accountExpired.label', default: 'Account Expired')}" />
                        
                            <g:sortableColumn property="accountLocked" title="${message(code: 'SUser.accountLocked.label', default: 'Account Locked')}" />
                        
                            <g:sortableColumn property="enabled" title="${message(code: 'SUser.enabled.label', default: 'Enabled')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${SUserInstanceList}" status="i" var="SUserInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${SUserInstance.id}">${fieldValue(bean: SUserInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: SUserInstance, field: "username")}</td>
                        
                            <td>${fieldValue(bean: SUserInstance, field: "password")}</td>
                        
                            <td><g:formatBoolean boolean="${SUserInstance.accountExpired}" /></td>
                        
                            <td><g:formatBoolean boolean="${SUserInstance.accountLocked}" /></td>
                        
                            <td><g:formatBoolean boolean="${SUserInstance.enabled}" /></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${SUserInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
