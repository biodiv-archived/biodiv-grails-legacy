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
        <r:require modules="observations_list"/>
    </head>
    <body>
        <div class="span12">
			<search:searchResultsHeading />
			
			<g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            
			<uGroup:rightSidebar/>
			
			<!-- main_content -->
			<div id="searchResults" class="list" style="margin-left: 0px; clear:both;">
				<newsletter:searchResults/>
			</div>
		</div>
         
<g:javascript>
$(document).ready(function() {
	window.params = {
		'offset':"${params.offset}",
		'isGalleryUpdate':"${params.isGalleryUpdate}",	
		"tagsLink":"${uGroup.createLink(controller:'newsletter', action: 'tags')}",
		"queryParamsMax":"${queryParams?.max}",
		'speciesName':"${params.speciesName }",
		'isFlagged':"${params.isFlagged }"
	}
	
});

</g:javascript>
    </body>

</html>
