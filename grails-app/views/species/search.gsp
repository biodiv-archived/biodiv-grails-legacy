<%@page import="species.utils.Utils"%>
<%@page import="species.Species"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'showobservationstoryfooter.title.species')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="species, species_list" />
<style>
.thumbnail .observation_story {
    width: 784px;
}
</style>
</head>
<body>
	<div class="span12">
		<div class="outer_wrapper">
			<search:searchResultsHeading />
			<s:showSubmenuTemplate />
			<uGroup:rightSidebar />
			<!-- main_content -->
			<div id="searchResults" class="list"
				style="margin-left: 0px; clear: both;">
				<s:speciesFilter />
				<uGroup:objectPostToGroupsWrapper model="['objectType':Species.class.canonicalName, canPullResource:canPullResource]"/>
				<div class="list" style="top: 0px;">
					<s:searchResults />
				</div>
			</div>

		</div>
	</div>

	<r:script>

$(document).ready(function(){

	$(".list_view").show();
	
    $('.list').on('updatedGallery', function(event) {
    	$(".list_view").show();
    });
	
});


</r:script>
</body>
</html>
