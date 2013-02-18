<%@page
	import="org.grails.plugins.google.visualization.data.Cell; org.grails.plugins.google.visualization.util.DateUtil"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<%--<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'activityFeed', action:'list')}" />--%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'charts.label', default: 'Stats')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
<r:require modules="chart" />
<gvisualization:apiImport />
</head>
<body>

	<div class="span12">
		<div class="page-header clearfix">
			<h1>
				<g:message code="default.observation.heading" args="[entityName]" />
			</h1>
		</div>

		<g:if test="${flash.message}">
			<div class="message alert alert-info">
				${flash.message}
			</div>
		</g:if>
		<chart:showStats model="['title':'Obsrvations', columns:obvData.columns, data:obvData.data]"/>
		<chart:showStats model="['title':'Speices', columns:speciesData.columns, data:speciesData.data]"/>
	</div>
	<r:script>
	</r:script>
</body>
</html>
