<%@page import="species.utils.Utils"%>
<%@ page import="utils.Newsletter"%>
<html>
<head>
<link rel="canonical"
	href="${Utils.getIBPServerDomain() + createLink(controller:'newsletter', action:'list')}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />


<g:set var="entityName"
	value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<style>
.body {
	padding: 10px;
}

.body td {
	padding: 5px;
}
</style>
<r:require modules="observations_list" />
</head>
<body>
	<div class="span12">
		<search:searchResultsHeading />

		<g:if test="${flash.message}">
			<div class="message alert alert-info">
				${flash.message}
			</div>
		</g:if>

		<uGroup:rightSidebar />
		<!-- main_content -->
		<div id="searchResults" class="list"
			style="margin-left: 0px; clear: both;">
			<obv:showObservationFilterMessage
				model="['instanceTotal':total, 'queryParams':queryParams, resultType:'page']" />
			<div class="list" style="top: 0px;">
				<newsletter:searchResults />
			</div>
		</div>

	</div>

	<r:script>
		$(document).ready(function() {
			$(".list_view").show();
	
		    $('.list').on('updatedGallery', function(event) {
		    	$(".list_view").show();
		    });
			
		});
	</r:script>
</body>

</html>
