
<%@ page import="species.CommonNames" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'commonNames.label', default: 'CommonNames')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
       
        <div class="container_16">
        	<div class="grid_12">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'commonNames.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'commonNames.name.label', default: 'Name')}" />
                        
                            <th><g:message code="commonNames.language.label" default="Language" /></th>
                        
                            <th><g:message code="commonNames.taxonConcept.label" default="Taxon Concept" /></th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${commonNamesInstanceList}" status="i" var="commonNamesInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${commonNamesInstance.id}">${fieldValue(bean: commonNamesInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: commonNamesInstance, field: "name")}</td>
                        
                            <td>${commonNamesInstance.language?.name}</td>
                        
                            <td>${commonNamesInstance.taxonConcept?.italicisedForm}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${commonNamesInstanceTotal}" />
            </div>
            </div>
        </div>
    </body>
</html>
