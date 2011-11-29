
<%@ page import="species.SpeciesGroup" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'speciesGroup.label', default: 'SpeciesGroup')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
       <div class="container_12">

		<div class="grid_12">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'speciesGroup.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'speciesGroup.name.label', default: 'Name')}" />
                        
                            <th><g:message code="speciesGroup.parentGroup.label" default="Parent Group" /></th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${speciesGroupInstanceList}" status="i" var="speciesGroupInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${speciesGroupInstance.id}">${fieldValue(bean: speciesGroupInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: speciesGroupInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: speciesGroupInstance, field: "parentGroup")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${speciesGroupInstanceTotal}" />
            </div>
        </div>
        </div>
    </body>
</html>
