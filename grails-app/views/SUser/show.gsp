<%@ page import="species.auth.SUser" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'SUser.label', default: 'SUser')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
    	<div class="container_16 big_wrapper">
		<div class="observation  grid_16">
        <div class="body">
            <h1>User Profile</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="SUser.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: SUserInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="SUser.username.label" default="Username" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: SUserInstance, field: "username")}</td>
                            
                        </tr>
                    
<%--                        <tr class="prop">--%>
<%--                            <td valign="top" class="name"><g:message code="SUser.password.label" default="Password" /></td>--%>
<%--                            --%>
<%--                            <td valign="top" class="value">${fieldValue(bean: SUserInstance, field: "password")}</td>--%>
<%--                            --%>
<%--                        </tr>--%>
<%--                    --%>
<%--                        <tr class="prop">--%>
<%--                            <td valign="top" class="name"><g:message code="SUser.accountExpired.label" default="Account Expired" /></td>--%>
<%--                            --%>
<%--                            <td valign="top" class="value"><g:formatBoolean boolean="${SUserInstance?.accountExpired}" /></td>--%>
<%--                            --%>
<%--                        </tr>--%>
<%--                    --%>
<%--                        <tr class="prop">--%>
<%--                            <td valign="top" class="name"><g:message code="SUser.accountLocked.label" default="Account Locked" /></td>--%>
<%--                            --%>
<%--                            <td valign="top" class="value"><g:formatBoolean boolean="${SUserInstance?.accountLocked}" /></td>--%>
<%--                            --%>
<%--                        </tr>--%>
<%--                    --%>
<%--                        <tr class="prop">--%>
<%--                            <td valign="top" class="name"><g:message code="SUser.enabled.label" default="Enabled" /></td>--%>
<%--                            --%>
<%--                            <td valign="top" class="value"><g:formatBoolean boolean="${SUserInstance?.enabled}" /></td>--%>
<%--                            --%>
<%--                        </tr>--%>
<%--                    --%>
<%--                        <tr class="prop">--%>
<%--                            <td valign="top" class="name"><g:message code="SUser.openIds.label" default="Open Ids" /></td>--%>
<%--                            --%>
<%--                            <td valign="top" style="text-align: left;" class="value">--%>
<%--                                <ul>--%>
<%--                                <g:each in="${SUserInstance.openIds}" var="o">--%>
<%--                                    <li><g:link controller="openID" action="show" id="${o.id}">${o?.encodeAsHTML()}</g:link></li>--%>
<%--                                </g:each>--%>
<%--                                </ul>--%>
<%--                            </td>--%>
<%--                            --%>
<%--                        </tr>--%>
<%--                    --%>
<%--                        <tr class="prop">--%>
<%--                            <td valign="top" class="name"><g:message code="SUser.passwordExpired.label" default="Password Expired" /></td>--%>
<%--                            --%>
<%--                            <td valign="top" class="value"><g:formatBoolean boolean="${SUserInstance?.passwordExpired}" /></td>--%>
<%--                            --%>
<%--                        </tr>--%>
<%--                    --%>
                    </tbody>
                </table>
            </div>
            </div>
            </div>
<%--            --%>
<%--            <div class="buttons">--%>
<%--                <g:form>--%>
<%--                    <g:hiddenField name="id" value="${SUserInstance?.id}" />--%>
<%--                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>--%>
<%--                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>--%>
<%--                </g:form>--%>
<%--            </div>--%>
            <obv:showRelatedStory model="['controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'user', 'filterPropertyValue':SUserInstance.id, 'id':'a']" />
        </div>
    </body>
</html>
