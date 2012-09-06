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
		args="[userGroupInstance.name]" />
</title>
<r:require modules="userGroups_show, list_utils" />
<style>
.thumbnail {
	margin: 0 25px;
}
</style>
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



				<div class="super-section userGroup-section">
					<div class="section tabbable">
						<h5>Members</h5>

						<ul class="nav nav-tabs">
							<li
								class="${(!params.action || params.action == 'members')?'active':'' }"><a
								href="${createLink(action:'members', id:params.id)}"> All
									Members (${membersTotalCount})</a>
							</li>

							<li class="${(params.action == 'founders')?'active':'' }"><a
								href="${createLink(action:'founders', id:params.id)}">
									Founders (${foundersTotalCount})</a>
							</li>
							<li class="${(params.action == 'experts')?'active':'' }"><a
								href="${createLink(action:'experts', id:params.id)}">
									Experts (${expertsTotalCount})</a>
							</li>
						</ul>



						<g:if test="${params.action == 'founders' }">
							<div class="tab-pane" id="founders">
								<sUser:showUserListWrapper
									model="['results':founders, 'totalCount':foundersTotalCount, 'queryParams':queryParams, 'searched':true]" />
							</div>
						</g:if>

						<g:elseif test="${params.action == 'experts' }">
							<div class="tab-pane" id="experts">
								<sUser:showUserListWrapper
									model="['results':experts, 'totalCount':expertsTotalCount, 'queryParams':queryParams, 'searched':true]" />
							</div>
						</g:elseif>
						<g:else>
							<div class="tab-pane" id="members">
								<sUser:showUserListWrapper
									model="['results':members, 'totalCount':membersTotalCount, 'queryParams':queryParams, 'searched':true]" />
							</div>
						</g:else>

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
