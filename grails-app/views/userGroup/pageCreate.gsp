
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
		args="[userGroupInstance.name]" /></title>
<r:require modules="userGroups_show" />
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate />
		<div class="userGroup-section">

			<g:include controller="newsletter" action="create"
				id="${newsletterId }" params="['userGroupId':userGroupInstance.id]" />
			<g:link action="pages" id="${userGroupInstance.id}">< Back to Newsletters</g:link>
		</div>


	</div>

	<r:script>
		$(document).ready(function(){

		});
	</r:script>
</body>
</html>