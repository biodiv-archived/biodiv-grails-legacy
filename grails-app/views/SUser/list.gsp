<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'SUser', action:'list')}" />
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' /></title>
<r:require modules="susers_list"/>
<g:set var="entityName"
	value="${message(code: 'sUser.label', default: 'Users')}" />

<style type="text/css">
.snippet.tablet .figure img {
	height: auto;
}

.figure .thumbnail {
	height: 120px;
	margin: 0 auto;
	text-align: center;
	*font-size: 120px;
	line-height: 120px;
}

.thumbnails > li {
	margin: 0 0 18px 10px;
}
</style>

</head>

<body>

	<div class="span12">
		<div class="page-header">
			<h1>
				<g:message code="default.list.label" args="[entityName]" />
			</h1>
		</div>

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]"/>
		<sUser:showUserListWrapper
			model="['results':results, 'totalCount':totalCount]" />
	</div>

	
</body>
</html>
