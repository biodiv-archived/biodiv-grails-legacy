
<%@ page import="species.Name" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'name.label', default: 'Name')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'name.id.label', default: 'Id')}" />
                        
                        	<g:sortableColumn property="name" title="${message(code: 'name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="canonicalForm" title="${message(code: 'name.canonicalForm.label', default: 'Canonical Form')}" />
                        
                            <g:sortableColumn property="normalizedForm" title="${message(code: 'name.normalizedForm.label', default: 'Normalized Form')}" />
                        
                            <g:sortableColumn property="italicisedForm" title="${message(code: 'name.italicisedForm.label', default: 'Italicised Form')}" />
                        
                            <g:sortableColumn property="genus" title="${message(code: 'name.genus.label', default: 'Genus')}" />
                        
                            <g:sortableColumn property="species" title="${message(code: 'name.species.label', default: 'Species')}" />
                            
                            <g:sortableColumn property="synonymOf" title="${message(code: 'name.species.label', default: 'Synonym of')}" />
                            
                            <g:sortableColumn property="commonNameOf" title="${message(code: 'name.species.label', default: 'Common Name of')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${nameInstanceList}" status="i" var="nameInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${nameInstance.id}">${fieldValue(bean: nameInstance, field: "id")}</g:link></td>
                        
                        	<td>${fieldValue(bean: nameInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: nameInstance, field: "canonicalForm")}</td>
                        
                            <td>${fieldValue(bean: nameInstance, field: "normalizedForm")}</td>
                        
                            <td>${nameInstance.italicisedForm}</td>
                        
                            <td>${fieldValue(bean: nameInstance, field: "genus")}</td>
                        
                            <td>${fieldValue(bean: nameInstance, field: "species")}</td>
                            
                            <td><g:link action="show" id="${nameInstance.synonymOf}">${nameInstance.synonymOf?.canonicalForm}</g:link></td>
                            
                            <td><g:link action="show" id="${nameInstance.commonNameOf}">${nameInstance.commonNameOf?.canonicalForm}</g:link></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${nameInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
