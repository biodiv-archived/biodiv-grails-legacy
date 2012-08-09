
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
	<div class="container outer-wrapper">
		<div class="row">
			<div class="observation span12">


				<div class="page-header clearfix">
					<div style="width: 100%;">
						<uGroup:showHeader model=[ 'userGroupInstance':userGroupInstance] />
					</div>
				</div>

				<g:if test="${flash.message }">
					<div class="message alert">
						${flash.message}
					</div>
				</g:if>

				<div>
					<uGroup:showSidebar/>
					<div class="super-section userGroup-section">
						<div class="description notes_view">
							${userGroupInstance.aboutUs}
						</div>
					</div>
					
					<div class="super-section userGroup-section">
						<div class="description notes_view" name="contactEmail">
							Contact us by filling in the following feedback form.
						</div>
					</div>
				</div>

			</div>
		</div>
	</div>
	<r:script>
		$(document).ready(function(){

		});
	</r:script>
</body>
</html>