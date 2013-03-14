
<%@ page import="content.Project" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <r:require modules="core"/>
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
                            <td valign="top" class="name"><g:message code="project.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.title.label" default="Title" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "title")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.direction.label" default="Direction" /></td>
                            
                            <td valign="top" class="value"><g:link controller="strategicDirection" action="show" id="${projectInstance?.direction?.id}">${projectInstance?.direction?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.granteeURL.label" default="Grantee URL" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "granteeURL")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.granteeName.label" default="Grantee Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "granteeName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.locations.label" default="Locations" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${projectInstance.locations}" var="l">
                                    <li><g:link controller="location" action="show" id="${l.id}">${l?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.grantFrom.label" default="Grant From" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${projectInstance?.grantFrom}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.grantTo.label" default="Grant To" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${projectInstance?.grantTo}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.grantedAmount.label" default="Granted Amount" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "grantedAmount")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.projectProposal.label" default="Project Proposal" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "projectProposal")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.projectReport.label" default="Project Report" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "projectReport")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.dataContributionIntensity.label" default="Data Contribution Intensity" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "dataContributionIntensity")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.analysis.label" default="Analysis" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "analysis")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="project.misc.label" default="Misc" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "misc")}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${projectInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
