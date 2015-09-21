<%@page import="org.springframework.security.acls.domain.BasePermission"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.Utils"%>
<%@ page import="species.groups.UserGroup"%>
<html>
<head>
<g:set var="entityName"
	value="${(userGroupInstance)?userGroupInstance.name:Utils.getDomainName(request)}" />

<g:set var="title" value="${g.message(code:'ugroup.value.pages')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="userGroups_show" />
</head>
<body>
	<div class="observation span12 bodymarker">
		<uGroup:showSubmenuTemplate model="['entityName':'Pages']" />
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]" />
		<div class="">
			<div class="userGroup-section">
				<div class="btn-group pull-right" style="z-index: 10;">
					<g:if test="${userGroupInstance}">
						<sec:permitted className='species.groups.UserGroup'
							id='${userGroupInstance.id}'
							permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>

							<a
								href="${uGroup.createLink(mapping:"userGroup", action:"pageCreate", 'userGroup':userGroupInstance)}"
								class="btn btn-info"> <i class="icon-plus"></i><g:message code="default.add.page.label" /></a>
						</sec:permitted>
					</g:if>
					<g:else>
						<sUser:isAdmin>
							<g:link
								url="${uGroup.createLink(mapping:'userGroupGeneric', controller:'userGroup', action:'pageCreate')}"
								class="btn  btn-info">
								<i class="icon-plus"></i><g:message code="default.add.page.label" /></g:link>
						</sUser:isAdmin>
					</g:else>
				</div>
			</div>


			<g:include controller="newsletter" action="show"
				id="${params.newsletterId}" />
			<div class="btn-group pull-right"
				style="z-index: 10; clear: both; margin-top: 5px;">

				<g:if test="${userGroupInstance}">
					<g:link
						url="${uGroup.createLink(mapping:'userGroup', controller:'userGroup', action:'pages', 'userGroup':userGroupInstance)}"
						class="btn btn-info">< ${g.message(code:'ugroup.pages.back')} </g:link>
				</g:if>
				<g:else>
					<g:link
						url="${uGroup.createLink(mapping:'userGroupGeneric', action:'pages')}"
						class="btn btn-info">< ${g.message(code:'ugroup.pages.back')}</g:link>
				</g:else>


			</div>
		</div>

	</div>
	<script>$.fn.cycle.defaults.autoSelector = '.cycle-slideshow';</script>

</body>
</html>
