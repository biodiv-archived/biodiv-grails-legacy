<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<g:set var="canonicalUrl" value="${uGroup.createLink(action:params.action, controller:'userGroup', userGroup:userGroupInstance,absolute:true)}"/>
<g:set var="title" value="${userGroupInstance.name}"/>
<%def imagePath = userGroupInstance.mainImage()?.fileName;%>
<g:set var="description" value="${userGroupInstance.description.replaceAll(/<.*?>/, '').trim() }" />

<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':imagePath, 'siteName':userGroupInstance.name]"/>
<title>${title} | ${params.controller.capitalize()} | ${Utils.getDomainName(request)}</title>

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
