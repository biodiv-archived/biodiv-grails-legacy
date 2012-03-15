<%@ page import="species.auth.SUser"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'SUser.label', default: 'SUser')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<style>
.userCreatedObservations .jcarousel-skin-ie7 .jcarousel-clip-horizontal {
	width: 100%;
}
.userCreatedObservations .jcarousel-skin-ie7 .jcarousel-container-horizontal {
	width: 100%;
}
</style>
</head>
<body>
	<div class="container_16 big_wrapper">
		<div class="observation  grid_16">
			<div class="body">
				<h1>
					${fieldValue(bean: SUserInstance, field: "username")}
				</h1>
				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>
			</div>
		</div>
		<div class="observation_links">
			<a
				href="${createLink(controller:'observation', action: 'list', params: [userId: fieldValue(bean: SUserInstance, field: "id")])}">All
				observations</a>
		</div>
		<div class="grid_15 userCreatedObservations" style="clear: both">
			<obv:showRelatedStory
				model="['controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'user', 'filterPropertyValue':SUserInstance.id, 'id':'a']" />
		</div>

	</div>
</body>
</html>
