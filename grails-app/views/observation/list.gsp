<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + uGroup.createLink(controller:'observation', action:'list')}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />


<meta property="og:type" content="article" />
<meta property="og:title" content="${Utils.getDomainName(request)}" />
<meta property="og:url"
	content="${uGroup.createLink(action:params.action, controller:'userGroup', userGroupWebaddress:params.webaddress,absolute:true)}" />
<meta property="og:site_name" content="Observations (${Utils.getDomainName(request)})" />

<g:set var="domain" value="${Utils.getDomain(request)}" />
<%
				String fbAppId;
				if(domain.equals(grailsApplication.config.wgp.domain)) {
					fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
				} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
					fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
				}
		%>

<meta property="fb:app_id" content="${fbAppId }" />
<meta property="fb:admins" content="581308415,100000607869577" />



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
			window.params.tagsLink = "${uGroup.createLink(controller:'observation', action: 'tags')}";
			initRelativeTime("${uGroup.createLink(controller:'activityFeed', action:'getServerTime')}");
		});
	</g:javascript>
</body>
</html>
