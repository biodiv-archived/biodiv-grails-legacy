<%@ page import="species.utils.Utils"%>
<html>

<head>
<g:set var="title" value="User"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="susers_list" />
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
		<search:searchResultsHeading />
		<!-- uGroup:rightSidebar/-->

		<div class="searchResults" style="margin-left: 0px;">
			<sUser:showUserListWrapper />
		</div>
	</div>


</body>
</html>
