<%@ page import="species.utils.Utils"%>
<%@ page import="species.TaxonomyDefinition" %>
<html>
    <head>
    	<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'taxonomyDefinition', action:'show', id:taxonomyDefinitionInstance.id)}" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.canonicalForm.label" default="Canonical Form" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "canonicalForm")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.normalizedForm.label" default="Normalized Form" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "normalizedForm")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.italicisedForm.label" default="Italicised Form" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "italicisedForm")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.binomialForm.label" default="Binomial Form" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "binomialForm")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.group.label" default="Group" /></td>
                            
                            <td valign="top" class="value"><g:link controller="speciesGroup" action="show" id="${taxonomyDefinitionInstance?.group?.id}">${taxonomyDefinitionInstance?.group?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.author.label" default="Author" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "author")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.rank.label" default="Rank" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "rank")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="taxonomyDefinition.year.label" default="Year" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: taxonomyDefinitionInstance, field: "year")}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${taxonomyDefinitionInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
