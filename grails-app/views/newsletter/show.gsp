
<%@ page import="utils.Newsletter" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <style>
        .newsletter_wrapper {
        width: 800px;
        padding: 30px;
        margin-left: auto;
        margin-right: auto;
        background-color: #e8f6f0;
        font-family: 'Helvetica Neue', Arial, 'Liberation Sans', FreeSans, sans-serif;
        }
        .newsletter_wrapper .body {
        background-color: #ffffff;
        padding: 10px;
        }
        .newsletter_wrapper .body h1{
        padding: 10px;
        border-bottom: 2px solid #60c59e;         
        }
        .newsletter_wrapper .body .date{
            font-size: 10px;
            font-style: italic;
        }
        </style>
    </head>
    <body>
        <div class="newsletter_wrapper">
        <div class="body">
            <h1>${fieldValue(bean: newsletterInstance, field: "title")}</h1>
            <div class="dialog">
                <table>
                    <tbody>
                   
                        <tr class="prop">
                            <td valign="top" class="value date"><g:formatDate date="${newsletterInstance?.date}" /></td>
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="value">${newsletterInstance?.newsitem}</td>
                        </tr>
                    
                    
                    </tbody>
                </table>
            </div>
            <sec:ifLoggedIn>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${newsletterInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
            </sec:ifLoggedIn>
        </div>
        </div>
    </body>
</html>
