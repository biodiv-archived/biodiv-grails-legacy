
<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.list.label" args="[entityName]" /></title>
<r:require modules="core" />

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
}

.project-list-item {
	margin: 20px;
}
</style>

</head>
<body>
	<div class="nav">
		<span class="menuButton"><g:link class="create" action="create">
				<g:message code="default.new.label" args="[entityName]" />
			</g:link></span>
	</div>
	<div class="body">
		<h1>
			<g:message code="default.list.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		
		<!--
		<project:search model="['queryParams':[]]"/> 
		-->
		<div class="project-list">

			<g:each in="${projectInstanceList}" status="i" var="projectInstance">
				<div class="${(i % 2) == 0 ? 'odd' : 'even'} item">
					<project:projectListItem
						model="['projectInstance':projectInstance]" />
				</div>

			</g:each>

		</div>
		<div class="paginateButtons">
			<g:paginate total="${projectInstanceTotal}" />
		</div>
	</div>
</body>
</html>
