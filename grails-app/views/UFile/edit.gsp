

<%@ page import="content.fileManager.UFile" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'UFile.label', default: 'UFile')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${UFileInstance}">
            <div class="errors">
                <g:renderErrors bean="${UFileInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${UFileInstance?.id}" />
                <g:hiddenField name="version" value="${UFileInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="size"><g:message code="UFile.size.label" default="Size" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: UFileInstance, field: 'size', 'errors')}">
                                    <g:textField name="size" value="${fieldValue(bean: UFileInstance, field: 'size')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="path"><g:message code="UFile.path.label" default="Path" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: UFileInstance, field: 'path', 'errors')}">
                                    <g:textField name="path" value="${UFileInstance?.path}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="UFile.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: UFileInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${UFileInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="UFile.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: UFileInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${UFileInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="extension"><g:message code="UFile.extension.label" default="Extension" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: UFileInstance, field: 'extension', 'errors')}">
                                    <g:textField name="extension" value="${UFileInstance?.extension}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="downloads"><g:message code="UFile.downloads.label" default="Downloads" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: UFileInstance, field: 'downloads', 'errors')}">
                                    <g:textField name="downloads" value="${fieldValue(bean: UFileInstance, field: 'downloads')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="doi"><g:message code="UFile.doi.label" default="Doi" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: UFileInstance, field: 'doi', 'errors')}">
                                    <g:textField name="doi" value="${UFileInstance?.doi}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
