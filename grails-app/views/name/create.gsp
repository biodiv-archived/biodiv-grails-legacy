

<%@ page import="species.Name" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'name.label', default: 'Name')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${nameInstance}">
            <div class="errors">
                <g:renderErrors bean="${nameInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="canonicalForm"><g:message code="name.canonicalForm.label" default="Canonical Form" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: nameInstance, field: 'canonicalForm', 'errors')}">
                                    <g:textField name="canonicalForm" value="${nameInstance?.canonicalForm}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="normalizedForm"><g:message code="name.normalizedForm.label" default="Normalized Form" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: nameInstance, field: 'normalizedForm', 'errors')}">
                                    <g:textField name="normalizedForm" value="${nameInstance?.normalizedForm}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="italicisedForm"><g:message code="name.italicisedForm.label" default="Italicised Form" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: nameInstance, field: 'italicisedForm', 'errors')}">
                                    <g:textField name="italicisedForm" value="${nameInstance?.italicisedForm}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="genus"><g:message code="name.genus.label" default="Genus" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: nameInstance, field: 'genus', 'errors')}">
                                    <g:textField name="genus" value="${nameInstance?.genus}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="species"><g:message code="name.species.label" default="Species" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: nameInstance, field: 'species', 'errors')}">
                                    <g:textField name="species" value="${nameInstance?.species}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="synonymOf"><g:message code="name.synonymOf.label" default="Synonym Of" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: nameInstance, field: 'synonymOf', 'errors')}">
                                    <g:select name="synonymOf.id" from="${species.Name.list()}" optionKey="id" value="${nameInstance?.synonymOf?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="commonNameOf"><g:message code="name.commonNameOf.label" default="Common Name Of" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: nameInstance, field: 'commonNameOf', 'errors')}">
                                    <g:select name="commonNameOf.id" from="${species.Name.list()}" optionKey="id" value="${nameInstance?.commonNameOf?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="name.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: nameInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${nameInstance?.name}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
