<%@page import="species.utils.Utils"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require modules="observations_list" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Search Results')}" />
<title>${entityName} | ${Utils.getDomainName(request)}</title>

</head>
<body>

	<div class="span12">
		<search:searchResultsHeading />
		<uGroup:rightSidebar />
		<!-- main_content -->
		<div id="searchResults" class="list"
			style="margin-left: 0px; clear: both;">
			<clist:filterTemplate />

			<div class="observations_list_wrapper">
				<clist:showList />
			</div>
		</div>

	</div>

	<r:script>

$(document).ready(function(){

	$(".list_view").show();
	
    $('.observations_list_wrapper').on('updatedGallery', function(event) {
    	$(".list_view").show();
    });
	
});


</r:script>
</body>
</html>
