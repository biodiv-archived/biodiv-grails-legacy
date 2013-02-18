<div style="clear:both;">
	<h3>${title}</h3>
	<div>			
			<gvisualization:columnCoreChart elementId="columnCoreChart_${title}"
				width="${550}" height="${400}"
				vAxis="${new Expando(title: 'Count', titleColor: 'red')}" hAxis="${new Expando(title: 'Species Group', titleColor: 'red')}"
				columns="${columns}" data="${data}" />
			<div id="columnCoreChart_${title}" style="float: left;"></div>	
			
			<gvisualization:pieCoreChart elementId="piechart_${title}"
				width="${350}" height="${400}"
				columns="${columns}" data="${data}" />
			<div id="piechart_${title}" style="float:right;"></div>
		</div>
		
		<gvisualization:table elementId="table_${title}" width="${500}" height="${300}"
			columns="${columns}" data="${data}" select="selectHandler" />
		<h5>Table Data</h5>
		<div id="table_${title}" style="float: left;"></div>
</div>