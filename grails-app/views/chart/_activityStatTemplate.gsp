<%@ page import="org.grails.plugins.google.visualization.data.Cell; org.grails.plugins.google.visualization.util.DateUtil" %>
<div style="clear:both;">
	<h3>${title}</h3>
	<gvisualization:lineCoreChart elementId="linechart_${title}" width="${700}" height="${300}" title="Activity" columns="${columns}" data="${data}" />
	<div id="linechart_${title}"></div>
</div>