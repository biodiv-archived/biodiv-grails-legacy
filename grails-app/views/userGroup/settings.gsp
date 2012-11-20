
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
		
			<div class="super-section"><a 
				href="${uGroup.createLink(mapping:'userGroup', action:'edit', userGroup:userGroupInstance)}"> <i
				class="icon-edit"></i>Edit Group </a></div>
			<div class="super-section" style="clear: both;">
			
				<div class="section" style="position: relative; overflow: visible;">
				<h3>Display Settings</h3>
				<uGroup:showGeneralSettings
						model="['userGroupInstance':userGroupInstance]" /></div></div>
			
					</div>
	</div>

	<r:script>
		$(document).ready(function(){

		});
	</r:script>
</body>
</html>