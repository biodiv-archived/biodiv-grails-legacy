<%@ page import="species.utils.Utils"%>
<%@ page import="species.TaxonomyDefinition" %>
<html>
    <head>
    	<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'taxonomyDefinition', action:'list')}" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'taxonomyDefinition.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'taxonomyDefinition.name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="canonicalForm" title="${message(code: 'taxonomyDefinition.canonicalForm.label', default: 'Canonical Form')}" />
                        
                            <g:sortableColumn property="normalizedForm" title="${message(code: 'taxonomyDefinition.normalizedForm.label', default: 'Normalized Form')}" />
                        
                            <g:sortableColumn property="italicisedForm" title="${message(code: 'taxonomyDefinition.italicisedForm.label', default: 'Italicised Form')}" />
                        
                            <g:sortableColumn property="binomialForm" title="${message(code: 'taxonomyDefinition.binomialForm.label', default: 'Binomial Form')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${taxonomyDefinitionInstanceList}" status="i" var="taxonomyDefinitionInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${taxonomyDefinitionInstance.id}">${fieldValue(bean: taxonomyDefinitionInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: taxonomyDefinitionInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: taxonomyDefinitionInstance, field: "canonicalForm")}</td>
                        
                            <td>${fieldValue(bean: taxonomyDefinitionInstance, field: "normalizedForm")}</td>
                        
                            <td>${fieldValue(bean: taxonomyDefinitionInstance, field: "italicisedForm")}</td>
                        
                            <td>${fieldValue(bean: taxonomyDefinitionInstance, field: "binomialForm")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${taxonomyDefinitionInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
