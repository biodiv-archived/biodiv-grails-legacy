
<%@ page import="utils.Newsletter" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'bootstrap/css',file:'bootstrap.css', absolute:true)}" />

        <g:set var="entityName" value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <style>
        .body {
            padding: 10px;        
        }

        .body td {
            padding: 5px;
        }
        </style>
        <r:require modules="core"/>
    </head>
    <body>
        <div class="span12">
            <div style="float: right;"><g:link action="create"><h3>Create newsletter</h3></g:link></div>
            <h1>Newsletters Archive</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table class="table table-striped">
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="date" title="${message(code: 'newsletter.date.label', default: 'Date')}" />
                        
                            <g:sortableColumn property="title" title="${message(code: 'newsletter.title.label', default: 'Title')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${newsletterInstanceList}" status="i" var="newsletterInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:formatDate date="${newsletterInstance.date}" type="date" style="MEDIUM"/></td>
                        
                            <td><g:link action="show" id="${newsletterInstance.id}">${fieldValue(bean: newsletterInstance, field: "title")}</g:link></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${newsletterInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
