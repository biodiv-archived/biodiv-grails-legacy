<%@page import="species.utils.Utils"%>
<%@ page import="utils.Newsletter" %>
<html>
    <head>
<g:set var="title" value="Newsletters"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
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
            <div style="float: right;"><g:link action="create"><h3><g:message code="link.create.newsletter" /></h3></g:link></div>
            <h1><g:message code="link.newsleters.archive" /></h1>
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
