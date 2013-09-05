<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<html>
<head>
<g:set var="title" value="Observations"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="observations_list" />
<style>
    .thumbnails>li {
        margin:0 2px 2px 0px;
        padding:0px;
    }
    .map_wrapper {
        margin-bottom: 0px;
    }
</style>
</head>
<body>

	<div class="span12">
		<obv:showSubmenuTemplate model="['entityName':title]" />
		<uGroup:rightSidebar/>
		<obv:showObservationsListWrapper />
	</div>

	<g:javascript>
		$(document).ready(function() {
			window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}";
			initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
		});
	</g:javascript>
</body>
</html>
