<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'title.checklists')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
</head>
<body>

	<div class="span12">
		<search:searchResultsHeading />
		<uGroup:rightSidebar />
		<!-- main_content -->
		<div id="searchResults" class="list"
			style="margin-left: 0px; clear: both;">
			<clist:filterTemplate />

			<div class="list">
				<clist:showList />
			</div>
		</div>

	</div>

	<asset:script>

$(document).ready(function(){

	$(".list_view").show();
	
    $('.list').on('updatedGallery', function(event) {
    	$(".list_view").show();
    });
	
});


</asset:script>
</body>
</html>
