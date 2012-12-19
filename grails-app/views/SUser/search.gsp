<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' /></title>
<r:require modules="susers_list"/>
<g:set var="entityName"
	value="${message(code: 'searchlabel', default: 'Search Results')}" />

<style type="text/css">
.snippet.tablet .figure img {
	height: auto;
}

.figure .thumbnail {
	height: 120px;
	margin: 0 auto;
	text-align: center;
	*font-size: 120px;
	line-height: 120px;
}
</style>

</head>

<body>

	<div class="span12">
		<div class="page-header">
			<search:searchResultsHeading />
		</div>
		<!-- uGroup:rightSidebar/-->

		<div class="searchResults" style="margin-left:0px;">
			<sUser:showUserListWrapper />
		</div>
	</div>

	<r:script>
	$(document).ready(function() {
		
		$('.sort_filter_label').click(function() {
			$('.sort_filter_label.active').removeClass('active');
			$(this).addClass('active');
			$('#selected_sort').html($(this).html());
			$("#search").click();
			return false;
		});

		$("#removeQueryFilter").live('click', function(){
           	$( "#searchTextField" ).val('');
          	$("#search").click();
           	return false;
        });
	});
	

	</r:script>
</body>
</html>
