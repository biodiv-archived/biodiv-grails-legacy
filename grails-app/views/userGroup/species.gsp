
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName" value="${userGroupInstance.name}" />
<title><g:message code="default.show.label"
		args="[userGroupInstance.name]" />
</title>
<r:require modules="species" />
</head>
<body>
	<g:include controller="species" action="list" model="['userGroup':userGroupInstance, 'params':params]"/>
	<g:javascript>
		$(document).ready(function(){
			window.params.tagsLink = "${uGroup.createLink(controller:'species', action: 'tags')}";
			$('#speciesGallerySort').change(function(){
				updateGallery(window.location.pathname + window.location.search, ${params.limit?:40}, 0, undefined, false);
				return false;
			});
		});
		
	</g:javascript>
	<r:script>
		$(document).ready(function(){
			$(".grid_view").show();
		});
		
	</r:script>
</body>
</html>