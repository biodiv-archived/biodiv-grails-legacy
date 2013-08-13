<%@ page import="org.grails.plugins.google.visualization.data.Cell; org.grails.plugins.google.visualization.util.DateUtil" %>
<div style="clear:both;">
	<h3>${title}</h3>
	<gvisualization:annotatedTimeLine elementId="annotatedtimeline_${title}" columns="${columns}" data="${data}" />
	<div id="annotatedtimeline_${title}" style='width: 940px; height: 350px;'></div>
</div>