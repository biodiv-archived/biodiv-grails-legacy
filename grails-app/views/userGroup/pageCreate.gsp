
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
				id="${newsletterId }" params="['userGroup':userGroupInstance?:null, 'webaddress':userGroupInstance?.webaddress]" />
				
			<div class="btn-group pull-right" style="z-index: 10;clear:both;margin-top:5px;">

				<g:if test="${userGroupInstance}">
					<g:link url="${uGroup.createLink(mapping:'userGroup', action:'pages', id:userGroupInstance.id, userGroup:userGroupInstance)}"
						class="btn btn-info">< Back to Pages</g:link>
				</g:if>
				<g:else>
					<g:link url="${uGroup.createLink(mapping:'userGroupGeneric', action:'pages')}" class="btn btn-info">< Back to Pages</g:link>
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