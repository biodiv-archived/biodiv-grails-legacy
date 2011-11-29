

<%@ page import="species.participation.Recommendation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'recommendation.label', default: 'Recommendation')}" />
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
            <g:hasErrors bean="${recommendationInstance}">
            <div class="errors">
                <g:renderErrors bean="${recommendationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${recommendationInstance?.id}" />
                <g:hiddenField name="version" value="${recommendationInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="suggestedName"><g:message code="recommendation.suggestedName.label" default="Suggested Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: recommendationInstance, field: 'suggestedName', 'errors')}">
                                    <g:textField name="suggestedName" value="${recommendationInstance?.suggestedName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="author"><g:message code="recommendation.author.label" default="Author" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: recommendationInstance, field: 'author', 'errors')}">
                                    <g:select name="author.id" from="${species.Contributor.list()}" optionKey="id" value="${recommendationInstance?.author?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="confidence"><g:message code="recommendation.confidence.label" default="Confidence" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: recommendationInstance, field: 'confidence', 'errors')}">
                                    <g:select name="confidence" from="${species.Recommendation$ConfidenceType?.values()}" keys="${species.Recommendation$ConfidenceType?.values()*.name()}" value="${recommendationInstance?.confidence?.name()}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="createdOn"><g:message code="recommendation.createdOn.label" default="Created On" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: recommendationInstance, field: 'createdOn', 'errors')}">
                                    <g:datePicker name="createdOn" precision="day" value="${recommendationInstance?.createdOn}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="resolvedName"><g:message code="recommendation.resolvedName.label" default="Resolved Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: recommendationInstance, field: 'resolvedName', 'errors')}">
                                    <g:select name="resolvedName.id" from="${species.Name.list()}" optionKey="id" value="${recommendationInstance?.resolvedName?.id}"  />
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
