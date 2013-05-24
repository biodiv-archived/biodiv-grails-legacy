<%@ page import="species.utils.Utils"%>
<html>

<head>
<meta name='layout' content='main' />
<title>${entityName} | ${Utils.getDomainName(request)}</title>

<r:require modules="susers_list" />
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
		<search:searchResultsHeading />
		<!-- uGroup:rightSidebar/-->

		<div class="searchResults" style="margin-left: 0px;">
			<sUser:showUserListWrapper />
		</div>
	</div>


</body>
</html>
