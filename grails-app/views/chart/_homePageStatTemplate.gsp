<%@page import="species.utils.Utils"%>
<%@ page import="org.grails.plugins.google.visualization.data.Cell; org.grails.plugins.google.visualization.util.DateUtil" %>
<div style="clear:both;">
	<div id="annotatedtimeline_activity" style='width: 900px; height: 180px;'></div>
	<gvisualization:annotatedTimeLine dynamicLoading="${true}" elementId="annotatedtimeline_activity" columns="${activityData.columns}" data="${activityData.data}"/>
	
</div>
<a href="${uGroup.createLink(controller:'chart')}"> more...</a>
<%--<div style="clear:both;">--%>
<%--		<div>			--%>
<%--			<gvisualization:table elementId="table_user" width="${450}" height="${200}"--%>
<%--				columns="${userData.columns}" data="${userData.htmlData}" showRowNumber="${true}" allowHtml="${true}" />--%>
<%--			<div id="table_user" style="float: left;"></div>--%>
<%--		</div>--%>
<%--		--%>
<%--		<div>			--%>
<%--			<gvisualization:columnCoreChart elementId="columnCoreChart_obv"--%>
<%--				width="${450}" height="${200}"--%>
<%--				vAxis="${new Expando(title: 'Count', titleColor: 'red')}" hAxis="${new Expando(title: (hAxisTitle?:'Species Group'), titleColor: 'red')}"--%>
<%--				columns="${obvData.columns}" data="${obvData.data}"/>--%>
<%--			<div id="columnCoreChart_obv" style="float: left;"></div>--%>
<%--		</div>--%>

<%--		<div>			--%>
<%--			<gvisualization:columnCoreChart elementId="columnCoreChart_species"--%>
<%--				width="${300}" height="${150}"--%>
<%--				vAxis="${new Expando(title: 'Count', titleColor: 'red')}" hAxis="${new Expando(title: (hAxisTitle?:'Species Group'), titleColor: 'red')}"--%>
<%--				columns="${speciesData.columns}" data="${speciesData.data}"/>--%>
<%--			<div id="columnCoreChart_species" style="float: left;"></div>--%>
<%--		</div>--%>
<%--</div>--%>
