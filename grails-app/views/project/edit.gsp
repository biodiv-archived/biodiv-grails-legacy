
<%@page import="species.utils.Utils"%>
<%@ page import="content.Project" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <r:require modules="add_file" />
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
            <g:hasErrors bean="${projectInstance}">
            <div class="errors">
                <g:renderErrors bean="${projectInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${projectInstance?.id}" />
                <g:hiddenField name="version" value="${projectInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="title"><g:message code="project.title.label" default="Title" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'title', 'errors')}">
                                    <g:textField name="title" value="${projectInstance?.title}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="direction"><g:message code="project.direction.label" default="Direction" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'direction', 'errors')}">
                                    <g:select name="direction.id" from="${content.StrategicDirection.list()}" optionKey="id" value="${projectInstance?.direction?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="granteeURL"><g:message code="project.granteeURL.label" default="Grantee URL" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'granteeURL', 'errors')}">
                                    <g:textField name="granteeURL" value="${projectInstance?.granteeURL}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="granteeName"><g:message code="project.granteeName.label" default="Grantee Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'granteeName', 'errors')}">
                                    <g:textField name="granteeName" value="${projectInstance?.granteeName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="locations"><g:message code="project.locations.label" default="Locations" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'locations', 'errors')}">
                                    <g:select name="locations" from="${content.Location.list()}" multiple="yes" optionKey="id" size="5" value="${projectInstance?.locations*.id}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="grantFrom"><g:message code="project.grantFrom.label" default="Grant From" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'grantFrom', 'errors')}">
                                    <g:datePicker name="grantFrom" precision="day" value="${projectInstance?.grantFrom}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="grantTo"><g:message code="project.grantTo.label" default="Grant To" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'grantTo', 'errors')}">
                                    <g:datePicker name="grantTo" precision="day" value="${projectInstance?.grantTo}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="grantedAmount"><g:message code="project.grantedAmount.label" default="Granted Amount" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'grantedAmount', 'errors')}">
                                    <g:textField name="grantedAmount" value="${fieldValue(bean: projectInstance, field: 'grantedAmount')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="projectProposal"><g:message code="project.projectProposal.label" default="Project Proposal" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'projectProposal', 'errors')}">
                                    <g:textField name="projectProposal" value="${projectInstance?.projectProposal}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="projectReport"><g:message code="project.projectReport.label" default="Project Report" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'projectReport', 'errors')}">
                                    <g:textField name="projectReport" value="${projectInstance?.projectReport}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dataContributionIntensity"><g:message code="project.dataContributionIntensity.label" default="Data Contribution Intensity" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'dataContributionIntensity', 'errors')}">
                                    <g:textField name="dataContributionIntensity" value="${projectInstance?.dataContributionIntensity}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="analysis"><g:message code="project.analysis.label" default="Analysis" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'analysis', 'errors')}">
                                    <g:textField name="analysis" value="${projectInstance?.analysis}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="misc"><g:message code="project.misc.label" default="Misc" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'misc', 'errors')}">
                                    <g:textField name="misc" value="${projectInstance?.misc}" />
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
