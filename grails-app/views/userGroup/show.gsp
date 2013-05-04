<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<meta property="og:type" content="article" />
<meta property="og:title" content="${userGroupInstance.name}" />
<meta property="og:url"
	content="${uGroup.createLink(action:params.action, controller:'userGroup', userGroup:userGroupInstance,absolute:true)}" />

<meta property="og:image"
	content="${userGroupInstance.mainImage()?.fileName}" />
<meta property="og:site_name" content="${userGroupInstance.name?:Utils.getDomainName(request)}" />
<g:set var="description" value="" />
<g:set var="domain" value="${Utils.getDomain(request)}" />
<%
				String fbAppId;
				if(domain.equals(grailsApplication.config.wgp.domain)) {
					fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
				} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
					fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
				}
				
				description = userGroupInstance.description.replaceAll(/<.*?>/, '').trim() ;
				
		%>

<meta property="fb:app_id" content="${fbAppId }" />
<meta property="fb:admins" content="581308415,100000607869577" />
<meta property="og:description" content="${description?:''}" />
<meta name="description" content="${description?:''}">

<link rel="image_src"
	href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />

<g:set var="entityName" value="${userGroupInstance.name}" />
<title><g:message code="default.show.label"
		args="[userGroupInstance.name]" />
</title>
<r:require modules="userGroups_show,userGroups_list,comment" />
<style>
.comment-textbox {
	width: 100%;
}
.homepage-content .value.date {
	display:none;
}
</style>
</head>
<body>
	<div class="homepage-content" style="margin-left:20px;">
	</div>
	<g:javascript>
		$(document).ready(function() {
			window.params.tagsLink = "${g.createLink(action: 'tags')}";
			var url = "${userGroupInstance.homePage ?: uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'about', userGroup:userGroupInstance)}";
			$.get(url, function(data) {
				$('.homepage-content').append($(data).find('.bodymarker'));
			});
		});
	</g:javascript>
	
	<r:script>
		$(document).ready(function(){
			//showMapView();
		});
	</r:script>
</body>
</html>
