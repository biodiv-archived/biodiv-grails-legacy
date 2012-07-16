
<%@ page import="species.participation.Recommendation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'recommendation.label', default: 'Recommendation')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'recommendation.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="suggestedName" title="${message(code: 'recommendation.suggestedName.label', default: 'Suggested Name')}" />
                        
                            <th><g:message code="recommendation.author.label" default="Author" /></th>
                        
                            <g:sortableColumn property="confidence" title="${message(code: 'recommendation.confidence.label', default: 'Confidence')}" />
                        
                            <g:sortableColumn property="createdOn" title="${message(code: 'recommendation.createdOn.label', default: 'Created On')}" />
                        
                            <th><g:message code="recommendation.resolvedName.label" default="Resolved Name" /></th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${recommendationInstanceList}" status="i" var="recommendationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${recommendationInstance.id}">${fieldValue(bean: recommendationInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: recommendationInstance, field: "suggestedName")}</td>
                        
                            <td>${fieldValue(bean: recommendationInstance, field: "author")}</td>
                        
                            <td>${fieldValue(bean: recommendationInstance, field: "confidence")}</td>
                        
                            <td><g:formatDate date="${recommendationInstance.createdOn}" /></td>
                        
                            <td>${fieldValue(bean: recommendationInstance, field: "resolvedName")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${recommendationInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
