

<%@ page import="species.groups.SpeciesGroup" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'speciesGroup.label', default: 'SpeciesGroup')}" />
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
            <g:hasErrors bean="${speciesGroupInstance}">
            <div class="errors">
                <g:renderErrors bean="${speciesGroupInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${speciesGroupInstance?.id}" />
                <g:hiddenField name="version" value="${speciesGroupInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="speciesGroup.name.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: speciesGroupInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${speciesGroupInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="parentGroup"><g:message code="speciesGroup.parentGroup.label" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: speciesGroupInstance, field: 'parentGroup', 'errors')}">
                                    <g:select name="parentGroup.id" from="${species.SpeciesGroup.list()}" optionKey="id" value="${speciesGroupInstance?.parentGroup?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="taxonConcept"><g:message code="speciesGroup.taxonConcept.label"  /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: speciesGroupInstance, field: 'taxonConcept', 'errors')}">
                                    
<ul>
<g:each in="${speciesGroupInstance?.taxonConcept?}" var="t">
    <li><g:link controller="taxonomyDefinition" action="show" id="${t.id}">${t?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="taxonomyDefinition" action="create" params="['speciesGroup.id': speciesGroupInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'taxonomyDefinition.label', default: 'TaxonomyDefinition')])}</g:link>

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
