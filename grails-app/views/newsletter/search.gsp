<%@page import="species.utils.Utils"%>
<%@ page import="utils.Newsletter" %>
<html>
    <head>
    <link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'newsletter', action:'list')}" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
  

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
			<div class="page-header clearfix">
				<search:searchResultsHeading />
			</div>
			<uGroup:rightSidebar/>
            
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list span9"  style="margin-left:0px;">
            <g:if test="${!total}">
				<search:noSearchResults />
			</g:if>
			<g:else>
            
                <table class="table table-striped">
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="date" title="${message(code: 'newsletter.date.label', default: 'Date')}" />
                        
                            <g:sortableColumn property="title" title="${message(code: 'newsletter.title.label', default: 'Title')}" />
                            <g:sortableColumn property="userGroup" title="${message(code: 'newsletter.userGroup.label', default: 'Group')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${instanceList}" status="i" var="newsletterInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:formatDate date="${newsletterInstance.date}" type="date" style="MEDIUM"/></td>
                        
                            <td>
                            	<g:link url="${uGroup.createLink(mapping:'userGroup', action:'page', id:newsletterInstance.id, userGroup:newsletterInstance.userGroup)}">${fieldValue(bean: newsletterInstance, field: "title")}</g:link>
                            	
                            </td>
                            <td><g:link url="${uGroup.createLink(controller:'userGroup', action:'show', id:newsletterInstance.userGroup.id)}">${newsletterInstance.userGroup?newsletterInstance.userGroup.name:''}</g:link></td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            
            <div class="paginateButtons">
                <p:paginateOnSearchResult total="${total}" action="search"
											params="[query:responseHeader.params.q, fl:responseHeader.params.fl]" />
            </div>
            </g:else>
            </div>
        </div>
    </body>
</html>
