
<%@ page import="utils.Newsletter" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'newsletter.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="date" title="${message(code: 'newsletter.date.label', default: 'Date')}" />
                        
                            <g:sortableColumn property="title" title="${message(code: 'newsletter.title.label', default: 'Title')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${newsletterInstanceList}" status="i" var="newsletterInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${newsletterInstance.id}">${fieldValue(bean: newsletterInstance, field: "id")}</g:link></td>
                        
                            <td><g:formatDate date="${newsletterInstance.date}" /></td>
                        
                            <td>${fieldValue(bean: newsletterInstance, field: "title")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${newsletterInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
