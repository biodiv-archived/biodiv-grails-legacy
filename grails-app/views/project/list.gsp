
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


.paginateButtons { 
    margin: 3px 0px 3px 0px; 
} 

.paginateButtons a { 
    padding: 2px 4px 2px 4px; 
    background-color: #A4A4A4; 
    border: 1px solid #EEEEEE; 
    text-decoration: none; 
    font-size: 10pt; 
    font-variant: small-caps; 
    color: #EEEEEE; 
} 

.paginateButtons a:hover { 
    text-decoration: underline; 
    background-color: #888888; 
    border: 1px solid #AA4444; 
    color: #FFFFFF; 
} 


</style>

</head>
<body>
	<div class="body span8" style="padding-left: 20px;">
		<h1>
		Western Ghats CEPF Projects
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
<br />
		<div class="project-list tab-content span">

			<g:each in="${projectInstanceList}" status="i" var="projectInstance">
				<div class="${(i % 2) == 0 ? 'odd' : 'even'} item">
					<project:projectListItem
						model="['projectInstance':projectInstance, 'pos':i]" />
				</div>

			</g:each>


			<div class="paginateButtons">
				<g:paginate total="${projectInstanceTotal}" />
			</div>
		</div>
	</div>
	<g:render template="/project/projectSidebar" />

</body>
</html>
