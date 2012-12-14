<html>
<head>

<meta name="layout" content="main" />
<r:require modules="species" />
<title>Search Species</title>
</head>
<body>
	<div class="span12">
		<div class="outer_wrapper">
			<s:showSubmenuTemplate />
			<div class="page-header clearfix">
				<search:searchResultsHeading />
			</div>
			<uGroup:rightSidebar />
			<!-- main_content -->
			<div id="searchResults" class="list span9" style="margin-left: 0px;">
				<s:searchResults/>
			</div>

		</div>
	</div>

<r:script>

$(document).ready(function(){
		
	$("#removeQueryFilter").live('click', function(){
           	$( "#searchTextField" ).val('');
          	$("#search").click();
           	return false;
    });
});
	$( "#search" ).unbind('click');
	
	$( "#search" ).click(function() {
		$("#searchBox").attr("action", '/'+$('#category').val()+'/search');
		$("#searchBox").submit();
	});


</r:script>
</body>
</html>
