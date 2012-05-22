<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' /></title>
<g:javascript src="jquery/jquery.autopager-1.0.0.js"></g:javascript>
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
</style>

</head>

<body>
	<div class="container outer-wrapper">
		<div class="row">
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

				<sUser:showUserListWrapper model="['results':results, 'totalCount':totalCount]"/>
			</div>
		</div>
</div>
	
</body>
</html>
