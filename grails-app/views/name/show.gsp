
<%@ page import="species.Name" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'name.label', default: 'Name')}" />
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
                            <td valign="top" class="name"><g:message code="name.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.canonicalForm.label" default="Canonical Form" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "canonicalForm")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.normalizedForm.label" default="Normalized Form" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "normalizedForm")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.italicisedForm.label" default="Italicised Form" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "italicisedForm")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.genus.label" default="Genus" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "genus")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.species.label" default="Species" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "species")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.synonymOf.label" default="Synonym Of" /></td>
                            
                            <td valign="top" class="value"><g:link controller="name" action="show" id="${nameInstance?.synonymOf?.id}">${nameInstance?.synonymOf?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.commonNameOf.label" default="Common Name Of" /></td>
                            
                            <td valign="top" class="value"><g:link controller="name" action="show" id="${nameInstance?.commonNameOf?.id}">${nameInstance?.commonNameOf?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.author.label" default="Author" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "author")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="name.year.label" default="Year" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: nameInstance, field: "year")}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${nameInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
