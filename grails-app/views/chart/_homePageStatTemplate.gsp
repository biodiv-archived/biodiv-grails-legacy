<%@page import="species.utils.Utils"%>
<%@ page import="org.grails.plugins.google.visualization.data.Cell; org.grails.plugins.google.visualization.util.DateUtil" %>

<div style="clear:both;border-style:groove;border-width:6px;color:green;border-radius:10px;">
	<div style="text-align:center">
		<a  href="${uGroup.createLink(controller:'chart')}"> <h4>Activity Monitor: See top contributor | View observations stats | Explore species pages by group</h4> </a>
	</div>
	<div id="annotatedtimeline_activity" style='width: 870px; height: 180px;'></div>
	<gvisualization:annotatedTimeLine  dynamicLoading="${true}"  elementId="annotatedtimeline_activity" columns="${activityData.columns}" data="${activityData.data}" max="${max?:500}" fill="${30}" displayRangeSelector="${false}"/>
<%----%>
<%--	<br/>--%>
<%--	<div style="clear:both;">--%>
<%--			<div>			--%>
<%--				<gvisualization:table dynamicLoading="${true}" elementId="table_user" width="${400}" height="${405}"--%>
<%--					columns="${userData.htmlColumns}" data="${userData.htmlData}" allowHtml="${true}" />--%>
<%--				<div id="table_user" style="float: left;"></div>--%>
<%--			</div>--%>
<%--			--%>
<%--			<div>			--%>
<%--				<gvisualization:table dynamicLoading="${true}"  elementId="table_all" width="${500}" height="${405}"--%>
<%--					columns="${combineData.htmlColumns}" data="${combineData.htmlData}" allowHtml="${true}"/>--%>
<%--				<div id="table_all" style="float: right;"></div>--%>
<%--			</div>--%>
<%--			<div>			--%>
<%--				<gvisualization:columnCoreChart elementId="columnCoreChart_species"--%>
<%--					width="${300}" height="${405}"--%>
<%--					vAxis="${new Expando(title: 'Count', titleColor: 'red')}" hAxis="${new Expando(title: (hAxisTitle?:'Species Group'), titleColor: 'red')}"--%>
<%--					columns="${speciesData.columns}" data="${speciesData.data}"/>--%>
<%--				<div id="columnCoreChart_species" style="float: left;"></div>--%>
<%--			</div>--%>
<%--	</div>--%>
</div>
