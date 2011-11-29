<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Taxonomy Browser</title>

<link rel="stylesheet" type="text/css" media="screen"
	href="${resource(dir:'js/jquery/jquery.jqGrid-4.1.2/css',file:'ui.jqgrid.css', absolute:true)}" />

<g:javascript src="jquery/jquery.jqGrid-4.1.2/js/i18n/grid.locale-en.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript src="jquery/jquery.jqGrid-4.1.2/js/jquery.jqGrid.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript src="jquery/jquery.jqDock-1.8/jquery.jqDock.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

<g:javascript>

$(document).ready(function(){
 
	$("#grid").jqGrid({
   		url:'${createLink(controller:'data', action:'listHierarchy')}',
		datatype: "xml",
   		colNames:['Id', 'Name','#Species', 'SpeciesId'],
   		colModel:[
   			{name:'id',index:'id',hidden:true},
   			{name:'name',index:'name',formatter:heirarchyLevelFormatter},
   			{name:'count',index:'count',width:50},
   			{name:'speciesId',index:'speciesId', hidden:true}
   		],
   		width: '100%',
    	height: '100%',
    	autowidth:true,
    	scrollOffset: 0,
   		treeGrid: true,
   		ExpandColumn : 'name',
   		ExpandColClick : true,
   		treeGridModel: 'adjacency',
        postData:{n_level:-1, expand_all:false},
        sortable:false,
        loadComplete:function(data) {
        	var grid = $("#grid");
        	var postData = grid.getGridParam('postData');
        	postData.expand_all = false;
        }	        
	});
	
});


heirarchyLevelFormatter = function(el, cellVal, opts) {
	var cells = $(opts).find('cell')
	var taxonId = $.trim($(cells[0]).text())
	var speciesId = $.trim($(cells[3]).text())
	var level = $.trim($(cells[4]).text())
	var levelTxt;
	if(level == ${TaxonomyRank.KINGDOM.ordinal()} ) {
		levelTxt = "<span class='rank'> ${TaxonomyRank.KINGDOM} </span>"
	} else if(level == ${TaxonomyRank.PHYLUM.ordinal()} ) {
		levelTxt = "<span class='rank'> ${TaxonomyRank.PHYLUM} </span>"
	} else if(level == ${TaxonomyRank.CLASS.ordinal() }) {
		levelTxt = "<span class='rank'> ${TaxonomyRank.CLASS} </span>"
	} else if(level == ${TaxonomyRank.ORDER.ordinal() }) {
		levelTxt = "<span class='rank'> ${TaxonomyRank.ORDER} </span>"
	} else if(level == ${TaxonomyRank.FAMILY.ordinal() }) {
		levelTxt = "<span class='rank'> ${TaxonomyRank.FAMILY} </span>"
	} else if(level == ${TaxonomyRank.SUB_FAMILY.ordinal()} ) {
		levelTxt = "<span class='rank'> ${TaxonomyRank.SUB_FAMILY} </span>"
	} else if(level == ${TaxonomyRank.GENUS.ordinal() }) {
		levelTxt = "<span class='rank'> ${TaxonomyRank.GENUS} </span>"
	} else if(level == ${TaxonomyRank.SUB_GENUS.ordinal()} ) {
		levelTxt = "<span class='rank'> ${TaxonomyRank.SUB_GENUS} </span>"
	} else if(level == ${TaxonomyRank.SPECIES.ordinal()} ) {
		levelTxt = ""
	}
	
	if(level == ${TaxonomyRank.SPECIES.ordinal() }) {
		el = "<a href='${createLink(action:"show")}/"+speciesId+"'>"+el+"</a>";
	} else {
		// el = "<a // href='${createLink(action:"taxon")}/"+taxonId+"'	class='rank"+level+"'>"+levelTxt+": // "+el+"</a>";
		el = levelTxt+": "+"<span class='rank"+level+"'>"+el+"&nbsp;<a class='taxonExpandAll' onClick='expandAll(\""+cellVal.rowId+"\")'>+</a></span>"
	}
	return el;	   
}

function expandAll(rowId) {
	var grid = $("#grid");
	var rowData = grid.getRowData(rowId);
	if(!grid.isNodeLoaded(rowData) || grid.isNodeLoaded(rowData) == 'false') {
		var postData = grid.getGridParam('postData');
		postData.expand_all = true;
		$("#"+rowId+" div.treeclick").trigger('click');
	}		
}
</g:javascript>
</head>
<body>
	<div class="container_12">

		<div class="grid_12">
			<div>
				<table id="grid"></table>
			</div>

		</div>
	</div>

</body>
</html>
