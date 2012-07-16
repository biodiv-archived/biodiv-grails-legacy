
<%@ page import="species.participation.Recommendation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'recommendation.label', default: 'Recommendation')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
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
                            <td valign="top" class="name"><g:message code="recommendation.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: recommendationInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="recommendation.suggestedName.label" default="Suggested Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: recommendationInstance, field: "suggestedName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="recommendation.author.label" default="Author" /></td>
                            
                            <td valign="top" class="value"><g:link controller="contributor" action="show" id="${recommendationInstance?.author?.id}">${recommendationInstance?.author?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="recommendation.confidence.label" default="Confidence" /></td>
                            
                            <td valign="top" class="value">${recommendationInstance?.confidence?.encodeAsHTML()}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="recommendation.createdOn.label" default="Created On" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${recommendationInstance?.createdOn}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="recommendation.resolvedName.label" default="Resolved Name" /></td>
                            
                            <td valign="top" class="value"><g:link controller="name" action="show" id="${recommendationInstance?.resolvedName?.id}">${recommendationInstance?.resolvedName?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${recommendationInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
