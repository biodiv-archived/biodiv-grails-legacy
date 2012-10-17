
<%@page import="org.springframework.security.acls.domain.BasePermission"%>

<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName" value="${(userGroupInstance)?userGroupInstance.name:Utils.getDomainName(request)}" />
<title><g:message code="default.show.label"
		args="[(userGroupInstance)?userGroupInstance.name:Utils.getDomainName(request)]" /></title>
<r:require modules="userGroups_show" />
</head>
<body>

	<div class="observation span12">
		<uGroup:showSubmenuTemplate model="['entityName':'Pages']"/>
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]"/>
		<div class="userGroup-section center_panel">
			
				<div class="btn-group pull-right" style="z-index: 10;margin-bottom:10px;">
				<g:if test="${userGroupInstance}">
					<sec:permitted className='species.groups.UserGroup'
						id='${userGroupInstance.id}'
						permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

						<a href="${uGroup.createLink(mapping:"userGroup", action:"pageCreate", 'userGroupWebaddress':userGroupInstance.webaddress)}"
							class="btn btn-large btn-info">
							<i class="icon-plus"></i>Add a Newsletter</a>
					</sec:permitted>
				</g:if>
				<g:else>
					<sUser:isAdmin>
						<a href="${uGroup.createLink(mapping:"userGroupGeneric", action:"pageCreate") }" class="btn btn-large btn-info">
							<i class="icon-plus"></i>Add a Newsletter</a>
					</sUser:isAdmin>
				</g:else>
			</div>
			
			<div class="list">
				<table class="table table-striped">
					<thead>
						<tr>

							<g:sortableColumn property="title"
								title="${message(code: 'newsletter.title.label', default: 'Title')}" />

							<g:sortableColumn property="date"
								title="${message(code: 'newsletter.date.label', default: 'Date')}" />


						</tr>
					</thead>
					<tbody>
					
						<g:each in="${newsletters}"
							var="newsletterInstance" status="i">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

								<td>
								<g:if test="${userGroupInstance}">
								<a
									href="${uGroup.createLink('mapping':'userGroup', 'action':'page', 'id':newsletterInstance.id, 'userGroup':userGroupInstance) }">
										${fieldValue(bean: newsletterInstance, field: "title")} </a>
								</g:if>
								<g:else>
								<a
									href="${uGroup.createLink(controller:'userGroup', action:'page', id:newsletterInstance.id) }">
										${fieldValue(bean: newsletterInstance, field: "title")} </a>
								</g:else>
								</td>
								<td><g:formatDate date="${newsletterInstance.date}"
										type="date" style="MEDIUM" />
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
	</div>
	
	
	<r:script>
		$(document).ready(function(){

		});
	</r:script>
</body>
</html>