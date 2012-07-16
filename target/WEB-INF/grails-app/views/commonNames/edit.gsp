

<%@ page import="species.CommonNames" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'commonNames.label', default: 'CommonNames')}" />
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
            <g:hasErrors bean="${commonNamesInstance}">
            <div class="errors">
                <g:renderErrors bean="${commonNamesInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${commonNamesInstance?.id}" />
                <g:hiddenField name="version" value="${commonNamesInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="commonNames.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: commonNamesInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${commonNamesInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="language"><g:message code="commonNames.language.label" default="Language" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: commonNamesInstance, field: 'language', 'errors')}">
                                    <g:select name="language.id" from="${species.Language.list()}" optionKey="id" value="${commonNamesInstance?.language?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="taxonConcept"><g:message code="commonNames.taxonConcept.label" default="Taxon Concept" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: commonNamesInstance, field: 'taxonConcept', 'errors')}">
                                    <g:select name="taxonConcept.id" from="${species.TaxonomyDefinition.list()}" optionKey="id" value="${commonNamesInstance?.taxonConcept?.id}"  />
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
