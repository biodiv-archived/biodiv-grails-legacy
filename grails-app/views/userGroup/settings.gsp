
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
<r:require modules="userGroups_show" />
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate />
		<div class="userGroup-section">
			<ul class="nav nav-tabs" data-tabs="tabs">
				<li class="active"><a href="#general">General</a>
					<uGroup:showGeneralSettings model="['userGroupInstance':userGroupInstance]"/>
				</li>
				<li class="active"><a href="#userPermissions">User
						Permissions</a>
				</li>
				<li><a href="#">...</a></li>
				<li><a href="#">...</a></li>
			</ul>

			<div id="my-tab-content" class="tab-content">
				<div class="tab-pane active" id="general"></div>

				<div class="tab-pane" id="userPermissions"></div>
			</div>
		</div>
	</div>

	<r:script>
		$(document).ready(function(){

		});
	</r:script>
</body>
</html>