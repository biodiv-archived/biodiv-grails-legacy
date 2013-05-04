<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<r:require modules="observations_list" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Search Results')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
</head>
<body>

	<div class="span12">
		<obv:showSubmenuTemplate />
		<search:searchResultsHeading />
		<uGroup:rightSidebar />
		<obv:showObservationsListWrapper />
	</div>
</body>
</html>
