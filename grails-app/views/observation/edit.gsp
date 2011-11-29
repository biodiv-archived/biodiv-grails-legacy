

<%@ page import="species.participation.Observation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'observation.label', default: 'Observation')}" />
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
            <g:hasErrors bean="${observationInstance}">
            <div class="errors">
                <g:renderErrors bean="${observationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${observationInstance?.id}" />
                <g:hiddenField name="version" value="${observationInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="author"><g:message code="observation.author.label" default="Author" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observationInstance, field: 'author', 'errors')}">
                                    <g:select name="author.id" from="${species.Contributor.list()}" optionKey="id" value="${observationInstance?.author?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="createdOn"><g:message code="observation.createdOn.label" default="Created On" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observationInstance, field: 'createdOn', 'errors')}">
                                    <g:datePicker name="createdOn" precision="day" value="${observationInstance?.createdOn}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="notes"><g:message code="observation.notes.label" default="Notes" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observationInstance, field: 'notes', 'errors')}">
                                    <g:textField name="notes" value="${observationInstance?.notes}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="observedOn"><g:message code="observation.observedOn.label" default="Observed On" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observationInstance, field: 'observedOn', 'errors')}">
                                    <g:datePicker name="observedOn" precision="day" value="${observationInstance?.observedOn}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="recommendation"><g:message code="observation.recommendation.label" default="Recommendation" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observationInstance, field: 'recommendation', 'errors')}">
                                    
<ul>
<g:each in="${observationInstance?.recommendation?}" var="r">
    <li><g:link controller="recommendation" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="recommendation" action="create" params="['observation.id': observationInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'recommendation.label', default: 'Recommendation')])}</g:link>

                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="resource"><g:message code="observation.resource.label" default="Resource" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">
                                    <g:select name="resource" from="${species.Resource.list()}" multiple="yes" optionKey="id" size="5" value="${observationInstance?.resource*.id}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="title"><g:message code="observation.title.label" default="Title" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observationInstance, field: 'title', 'errors')}">
                                    <g:textField name="title" value="${observationInstance?.title}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="url"><g:message code="observation.url.label" default="Url" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observationInstance, field: 'url', 'errors')}">
                                    <g:textField name="url" value="${observationInstance?.url}" />
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
