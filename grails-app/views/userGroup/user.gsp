<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />

<link rel="image_src"
	href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.observations.serverURL)}" />

<g:set var="entityName" value="${userGroupInstance.name}" />
<title><g:message code="default.show.label"
		args="[userGroupInstance.name]" /></title>
<r:require modules="userGroups_show, susers_list" />
<style>
.thumbnail {
	margin: 0 25px;
}
</style>
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate model="['entityName':'Members']"/>
		<uGroup:rightSidebar  model="['userGroupInstance':userGroupInstance]"/>
		<div class="userGroup-section center_panel">
			<div class="tabbable">
				<ul class="nav nav-tabs">
					<li
						class="${(!params.action || params.action == 'user')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'user', 'userGroup':userGroupInstance)}"> All
							Members (${membersTotalCount})</a></li>

					<li class="${(params.action == 'founders')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'founders', 'userGroup':userGroupInstance)}">
							Founders (${foundersTotalCount})</a></li>
					
				</ul>



				<g:if test="${params.action == 'founders' }">
					<div class="tab-pane" id="founders">
						<sUser:showUserListWrapper
							model="['results':founders, 'totalCount':foundersTotalCount, 'queryParams':queryParams, 'searched':true, 'userGroupInstance':userGroupInstance]" />
					</div>
				</g:if>

				<g:elseif test="${params.action == 'experts' }">
					<div class="tab-pane" id="experts">
						<sUser:showUserListWrapper
							model="['results':experts, 'totalCount':expertsTotalCount, 'queryParams':queryParams, 'searched':true, 'userGroupInstance':userGroupInstance]" />
					</div>
				</g:elseif>
				<g:else>
					<div class="tab-pane" id="members">
						<sUser:showUserListWrapper
							model="['results':members, 'totalCount':membersTotalCount, 'queryParams':queryParams, 'searched':true, 'userGroupInstance':userGroupInstance]" />
					</div>
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
