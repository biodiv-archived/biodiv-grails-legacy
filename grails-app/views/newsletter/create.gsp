

<%@ page import="utils.Newsletter" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'bootstrap/css',file:'bootstrap.css', absolute:true)}" />

        <script src="${resource(dir:'plugins',file:'jquery-1.7/js/jquery/jquery-1.7.min.js', absolute:true)}" type="text/javascript" ></script>

        <g:set var="entityName" value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
        <style>
            .body {
                padding: 10px;
            }
        </style>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${newsletterInstance}">
            <div class="errors">
                <g:renderErrors bean="${newsletterInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody id="main-table">
                       
                            <tr class="prop">
                                <td valign="top" class="value ${hasErrors(bean: newsletterInstance, field: 'title', 'errors')}">
                                   <label for="title"><g:message code="newsletter.title.label" default="Title" /></label> <g:textField name="title" value="${newsletterInstance?.title}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="value ${hasErrors(bean: newsletterInstance, field: 'date', 'errors')}">
                                    <label for="date"><g:message code="newsletter.date.label" default="Date" /></label><g:datePicker name="date" precision="day" value="${newsletterInstance?.date}"  />
                                </td>
                            </tr>

                              <tr id="main_editor" class="prop">
                                <td valign="top" class="name">
                                    <ckeditor:editor name="newsitem" height="300px" width="800px">
                                    ${newsletterInstance?.newsitem}
                                    </ckeditor:editor>
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
    <script>
    $(document).ready(function() {
    });
    </script>
    </body>
</html>
