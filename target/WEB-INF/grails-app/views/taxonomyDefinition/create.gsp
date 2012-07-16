

<%@ page import="species.TaxonomyDefinition" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition')}" />
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
            <g:hasErrors bean="${taxonomyDefinitionInstance}">
            <div class="errors">
                <g:renderErrors bean="${taxonomyDefinitionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="taxonomyDefinition.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: taxonomyDefinitionInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${taxonomyDefinitionInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="canonicalForm"><g:message code="taxonomyDefinition.canonicalForm.label" default="Canonical Form" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: taxonomyDefinitionInstance, field: 'canonicalForm', 'errors')}">
                                    <g:textField name="canonicalForm" value="${taxonomyDefinitionInstance?.canonicalForm}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="normalizedForm"><g:message code="taxonomyDefinition.normalizedForm.label" default="Normalized Form" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: taxonomyDefinitionInstance, field: 'normalizedForm', 'errors')}">
                                    <g:textField name="normalizedForm" value="${taxonomyDefinitionInstance?.normalizedForm}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="italicisedForm"><g:message code="taxonomyDefinition.italicisedForm.label" default="Italicised Form" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: taxonomyDefinitionInstance, field: 'italicisedForm', 'errors')}">
                                    <g:textField name="italicisedForm" value="${taxonomyDefinitionInstance?.italicisedForm}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="binomialForm"><g:message code="taxonomyDefinition.binomialForm.label" default="Binomial Form" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: taxonomyDefinitionInstance, field: 'binomialForm', 'errors')}">
                                    <g:textField name="binomialForm" value="${taxonomyDefinitionInstance?.binomialForm}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="group"><g:message code="taxonomyDefinition.group.label" default="Group" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: taxonomyDefinitionInstance, field: 'group', 'errors')}">
                                    <g:select name="group.id" from="${species.SpeciesGroup.list()}" optionKey="id" value="${taxonomyDefinitionInstance?.group?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="rank"><g:message code="taxonomyDefinition.rank.label" default="Rank" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: taxonomyDefinitionInstance, field: 'rank', 'errors')}">
                                    <g:textField name="rank" value="${fieldValue(bean: taxonomyDefinitionInstance, field: 'rank')}" />
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
