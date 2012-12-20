<html>
<head>

<meta name="layout" content="main" />
<r:require modules="species, observations_list" />
<title>Search Species</title>
</head>
<body>
	<div class="span12">
		<div class="outer_wrapper">
			<search:searchResultsHeading />
			<s:showSubmenuTemplate />
			<uGroup:rightSidebar />
			<!-- main_content -->
			<div id="searchResults" class="list" style="margin-left: 0px; clear:both;">
				<s:speciesFilter/>
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


</r:script>
</body>
</html>
