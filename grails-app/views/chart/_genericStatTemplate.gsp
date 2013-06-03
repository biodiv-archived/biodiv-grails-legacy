<%@ page import="org.grails.plugins.google.visualization.data.Cell; org.grails.plugins.google.visualization.util.DateUtil" %>
<%--<style>--%>
<%--td {--%>
<%--	max-width:40px;--%>
<%--}--%>
<%--</style>--%>

<div>
	<g:if test="${!hideTitle}">			
		<h6>${title}</h6>
	</g:if>
        <g:if test="${data}">
        <div>
        <g:if test="${!hideBarChart}">			
			<gvisualization:columnCoreChart elementId="columnCoreChart_${title}"
				width="${width?:570}" height="${height?:415}"
				vAxis="${new Expando(title: 'Count', titleColor: 'red')}" hAxis="${new Expando(title: (hAxisTitle?:'Species Group'), titleColor: 'red')}"
                                columns="${columns}" data="${data}" legend="bottom"/>
			<div id="columnCoreChart_${title}" style="float: left;"></div>
		</g:if>	
			
<%--			<gvisualization:pieCoreChart elementId="piechart_${title}"--%>
<%--				width="${width?:350}" height="${hright?:400}"--%>
<%--				columns="${columns}" data="${data}" />--%>
<%--			<div id="piechart_${title}" style="float:right;"></div>--%>


            <g:if test="${!hideTable}">
			    <gvisualization:table elementId="table_${title}" width="${width?:350}" height="${height?:415}"
				columns="${htmlColumns?:columns}" data="${htmlData?:data}" allowHtml="${true}" />
<%--			    <h5>Table Data</h5>--%>
			    <div id="table_${title}" style="float: right;"></div>
            </g:if>
		</div>
        </g:if>
        <g:else>
            <div class="alert alert-info">
                No ${title}
            </div>
        </g:else>
		
</div>
