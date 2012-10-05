
<%@page import="species.utils.Utils"%>
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName" value="${userGroupInstance?userGroupInstance.name:species.utils.Utils.getDomainName(request)}" />
<title><g:message code="default.show.label"
		args="[userGroupInstance?userGroupInstance.name:species.utils.Utils.getDomainName(request)]" /></title>
<r:require modules="userGroups_show" />
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate />
		<div class="userGroup-section">

			<g:include controller="newsletter" action="create"
				id="${newsletterId }" params="['userGroup':userGroupInstance?.webaddress]" />
			<div class="btn-group pull-right" style="z-index: 10;">

				<g:if test="${userGroupInstance}">
					<g:link action="pages" id="${userGroupInstance.id}"
						class="btn btn-large btn-info">< Back to Newsletters</g:link>
				</g:if>
				<g:else>
					<g:link action="pages" class="btn btn-large btn-info">< Back to Newsletters</g:link>
				</g:else>


			</div>

		</div>


	</div>

	<r:script>
		$(document).ready(function(){

		});
	</r:script>
</body>
</html>