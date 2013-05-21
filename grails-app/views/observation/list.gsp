<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'observation', action:'list', base:Utils.getIBPServerDomain()])}" />
<g:set var="title" value="List"/>
<g:render template="/common/titleTemplate" model="['title':title, 'description':'', 'canonicalUrl':canonicalUrl, 'imagePath':'']"/>
<title>${title} | ${params.controller.capitalize()} | ${Utils.getDomainName(request)}</title>
<r:require modules="observations_list" />
</head>
<body>

	<div class="span12">
		<obv:showSubmenuTemplate model="['entityName':'Observations']" />
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
