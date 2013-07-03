<%@page
	import="org.grails.plugins.google.visualization.data.Cell; org.grails.plugins.google.visualization.util.DateUtil"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="Dashboard"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="chart" />
<gvisualization:apiImport />
</head>
<body>

	<div class="span12">
		<div class="page-header clearfix">
			<h1>
				<g:message code="default.observation.heading" args="[title]" />
			</h1>
		</div>

		<g:if test="${flash.message}">
			<div class="message alert alert-info">
				${flash.message}
			</div>
		</g:if>
		
		
<%--		<chart:showActivityStats model="['title':'Activity Monitor', columns:activityData.columns, data:activityData.data]"/>--%>
		
		<chart:showStats model="['title':'User Stats : ' + userData.obvCount + ' Observations and top 10 Contributors of last 7 days', columns:userData.columns, data:userData.data, hAxisTitle:'User', htmlData:userData.htmlData, htmlColumns:userData.htmlColumns]"/>
		
		<chart:showStats model="['title':'Observations', columns:obvData.columns, data:obvData.data, htmlData:obvData.htmlData, htmlColumns:obvData.htmlColumns]"/>
		
		<chart:showStats model="['title':'Species Pages', columns:speciesData.columns, data:speciesData.data,, htmlData:speciesData.htmlData, htmlColumns:speciesData.htmlColumns]"/>
	</div>
	<r:script>
	</r:script>
</body>
</html>
