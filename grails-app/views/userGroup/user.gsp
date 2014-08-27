<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
    <g:set var="title" value="${params.action?.capitalize()}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>

<r:require modules="userGroups_show, susers_list" />
<style>
.thumbnail {
	margin: 0 25px;
}
.list_view .observervation_story {
    width:784px;
}
.observations_list{
    top:0px;
}



</style>
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate model="['entityName':'Members']"/>
		<uGroup:rightSidebar  model="['userGroupInstance':userGroupInstance]"/>
		<div class="userGroup-section">
			<div class="tabbable">
				<ul class="nav nav-tabs">
					<li
						class="${(!params.action || params.action == 'about')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'about', 'userGroup':userGroupInstance)}"> <g:message code="button.about.us" /></a></li>
					<li
						class="${(!params.action || params.action == 'user')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'user', 'userGroup':userGroupInstance)}"><g:message code="button.all.members" /> (${membersTotalCount})</a></li>

					<li class="${(params.action == 'founders')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'founders', 'userGroup':userGroupInstance)}">
							<g:message code="tabs.founder" /> (${foundersTotalCount})</a></li>
							
					<li class="${(params.action == 'experts')?'active':'' }"><a
						href="${uGroup.createLink(mapping:'userGroup', action:'moderators', 'userGroup':userGroupInstance)}">
							<g:message code="tabs.moderators" /> (${expertsTotalCount})</a></li>
				</ul>



				<g:if test="${params.action == 'founders' }">
					<div class="tab-pane" id="founders">
						<sUser:showUserListWrapper
							model="['results':userInstanceList, 'instanceTotal':foundersTotalCount, 'queryParams':queryParams, 'searched':true, 'userGroupInstance':userGroupInstance]" />
					</div>
				</g:if>

				<g:elseif test="${params.action == 'experts' || params.action == 'moderators'}">
					<div class="tab-pane" id="experts">
						<sUser:showUserListWrapper
							model="['results':userInstanceList, 'instanceTotal':expertsTotalCount, 'queryParams':queryParams, 'searched':true, 'userGroupInstance':userGroupInstance]" />
					</div>
				</g:elseif>
				<g:else>
					<div class="tab-pane" id="members">
						<sUser:showUserListWrapper
							model="['results':userInstanceList, 'instanceTotal':membersTotalCount, 'queryParams':queryParams, 'searched':true, 'userGroupInstance':userGroupInstance]" />
					</div>
				</g:else>

			</div>
		</div>

	</div>
</body>
</html>
