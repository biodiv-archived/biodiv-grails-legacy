<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + uGroup.createLink(controller:'observation', action:'list')}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>
<r:require modules="observations_list" />
</head>
<body>

	<div class="span12">
		<obv:showSubmenuTemplate model="['entityName':entityName]" />
		<uGroup:rightSidebar/>
		<obv:showObservationsListWrapper />
	</div>

	<g:javascript>
		$(document).ready(function() {
			window.params = {
				'offset':"${params.offset}",
				'isGalleryUpdate':"${params.isGalleryUpdate}",	
				"tagsLink":"${uGroup.createLink(controller:'observation', action: 'tags')}",
				"queryParamsMax":"${queryParams?.max}",
				'speciesName':"${params.speciesName }",
				'isFlagged':"${params.isFlagged }"
			}
			alert(" calling init relative time");
			initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
		});
	</g:javascript>
	<r:script>
		$( "#search" ).unbind('click');
		$( "#search" ).click(function() {          
			var target = "${uGroup.createLink(controller:'observation', action:'search')}" + window.location.search;
			updateGallery(target, ${queryParams.max}, 0, undefined, false);
        	return false;
		});
	</r:script>
</body>
</html>
