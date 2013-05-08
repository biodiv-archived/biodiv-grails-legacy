
<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.list.label" args="[entityName]" /></title>
<r:require modules="content_view" />

<style>
<!--
.project-list .odd {
	background-color: ghostwhite;
}

-->
.item {
	border-top: 5px solid #c2c2c2;
	border-bottom: 2px solid #c2c2c2;
	background-color: #ffffff;
	width: 590px;
}

.project-list-item {
	margin: 20px;
}
</style>

</head>
<body>

	<div class="span12">
		<g:render template="/project/projectSubMenuTemplate"
			model="['entityName':'Western Ghats CEPF Projects']" />
		<uGroup:rightSidebar />
		<div class="span8 right-shadow-box"
			style="margin: 0px; padding-right: 5px;">
			<g:render template="/project/search" model="['params':params]" />

			<div class="observations_list" style="clear: both; top: 0px;">
				<div class="project-list tab-content span">

					<g:each in="${projectInstanceList}" status="i"
						var="projectInstance">
						<div class="${(i % 2) == 0 ? 'odd' : 'even'} item">
							<project:projectListItem
								model="['projectInstance':projectInstance, 'pos':i]" />
						</div>

					</g:each>
				</div>

				<% params['isGalleryUpdate'] = false; %>
				<div class="paginateButtons centered">
					<p:paginate controller="project" action="list"
						total="${projectInstanceTotal}" userGroup="${userGroup}"
						userGroupWebaddress="${userGroupWebaddress}" params="${params}"
						max="${queryParams.max }" maxsteps="10" />
				</div>

			</div>
		</div>
		<g:render template="/project/projectSidebar" />
	</div>
</body>
</html>
