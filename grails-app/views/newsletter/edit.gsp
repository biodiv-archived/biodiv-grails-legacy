

<%@ page import="utils.Newsletter" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <r:require modules="core"/>
    </head>
    <body>
        <div class="span12">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${newsletterInstance}">
            <div class="errors">
                <g:renderErrors bean="${newsletterInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${newsletterInstance?.id}" />
                <g:hiddenField name="version" value="${newsletterInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                             <tr class="prop">
                                    <td valign="top" class="value ${hasErrors(bean: newsletterInstance, field: 'title', 'errors')}">
                                        <g:textField name="title" value="${newsletterInstance?.title}" />
                                    </td>
                                </tr>

                            <tr class="prop">
                                <td valign="top" class="value ${hasErrors(bean: newsletterInstance, field: 'date', 'errors')}">
                                    <g:datePicker name="date" precision="day" value="${newsletterInstance?.date}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="value ${hasErrors(bean: newsletterInstance, field: 'newsitem', 'errors')}">
                                    <ckeditor:editor name="newsitem" height="300px" width="100%">
                                    ${newsletterInstance?.newsitem}
                                    </ckeditor:editor>
                                </td>
                            </tr>
                              <g:if test="${newsletterInstance.userGroup}">
                            	<input type="hidden" name="userGroupId" value="${newsletterInstance.userGroup.id}"/>
                            </g:if>                      
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
