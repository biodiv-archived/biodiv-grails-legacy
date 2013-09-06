<%@page import="species.utils.Utils"%><html>
<head>
<g:set var="title" value="Observations"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="observations_list" />
<style>
    .thumbnails>li {
        margin:2px;
    }
    .map_wrapper {
        margin-bottom: 0px;
    }
</style>

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
