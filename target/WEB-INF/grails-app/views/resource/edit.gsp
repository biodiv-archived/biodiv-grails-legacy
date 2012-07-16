

<%@ page import="species.Resource" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'resource.label', default: 'Resource')}" />
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
            <g:hasErrors bean="${resourceInstance}">
            <div class="errors">
                <g:renderErrors bean="${resourceInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${resourceInstance?.id}" />
                <g:hiddenField name="version" value="${resourceInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="fileName"><g:message code="resource.fileName.label" default="File Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'fileName', 'errors')}">
                                    <g:textField name="fileName" value="${resourceInstance?.fileName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="url"><g:message code="resource.url.label" default="Url" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'url', 'errors')}">
                                    <g:textField name="url" value="${resourceInstance?.url}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="resource.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${resourceInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="mimeType"><g:message code="resource.mimeType.label" default="Mime Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'mimeType', 'errors')}">
                                    <g:textField name="mimeType" value="${resourceInstance?.mimeType}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="attributors"><g:message code="resource.attributors.label" default="Attributors" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'attributors', 'errors')}">
                                    <g:select name="attributors" from="${species.Contributor.list()}" multiple="yes" optionKey="id" size="5" value="${resourceInstance?.attributors*.id}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="contributors"><g:message code="resource.contributors.label" default="Contributors" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'contributors', 'errors')}">
                                    <g:select name="contributors" from="${species.Contributor.list()}" multiple="yes" optionKey="id" size="5" value="${resourceInstance?.contributors*.id}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="licenses"><g:message code="resource.licenses.label" default="Licenses" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'licenses', 'errors')}">
                                    <g:select name="licenses" from="${species.License.list()}" multiple="yes" optionKey="id" size="5" value="${resourceInstance?.licenses*.id}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="observation"><g:message code="resource.observation.label" default="Observation" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'observation', 'errors')}">
                                    
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="speciesFields"><g:message code="resource.speciesFields.label" default="Species Fields" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'speciesFields', 'errors')}">
                                    
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="type"><g:message code="resource.type.label" default="Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: resourceInstance, field: 'type', 'errors')}">
                                    <g:select name="type" from="${species.Resource$ResourceType?.values()}" keys="${species.Resource$ResourceType?.values()*.name()}" value="${resourceInstance?.type?.name()}"  />
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
