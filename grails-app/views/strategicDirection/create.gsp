

<%@ page import="content.StrategicDirection" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'strategicDirection.label', default: 'StrategicDirection')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
        <r:require modules="core"/>
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
            <g:hasErrors bean="${strategicDirectionInstance}">
            <div class="errors">
                <g:renderErrors bean="${strategicDirectionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <%
                def form_action= uGroup.createLink(action:'save', controller:'strategicDirection', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
            %>
            <form action="${form_action}" method="POST" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="strategy"><g:message code="strategicDirection.strategy.label" default="Strategy" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: strategicDirectionInstance, field: 'strategy', 'errors')}">
                                    <g:textField name="strategy" value="${strategicDirectionInstance?.strategy}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </form>
        </div>
    </body>
</html>
