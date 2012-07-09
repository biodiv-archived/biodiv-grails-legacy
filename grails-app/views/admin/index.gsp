

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="admin" />
<g:set var="entityName"
	value="${message(code: 'name.label', default: 'Name')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>
</head>
<body>
	<div class="container_16">
		<g:if test="${flash.message}">
			<div
				class="ui-state-highlight ui-corner-all grid_10 prefix_3 suffix_3">
				<span class="ui-icon-info" style="float: left; margin-right: .3em;"></span>
				${flash.message}
			</div>
		</g:if>

		<div class="grid_16">
			<div>
			<ul>
							
			</ul>
			</div>
		</div>
	</div>
</body>
</html>
