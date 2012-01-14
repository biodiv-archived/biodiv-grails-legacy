<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>

<g:javascript>
$(document).ready(function() {
	
	
  	$('#taxonHierarchy').jqGrid({
		url:'${createLink(controller:'data', action:'listHierarchy')}',
		datatype: "xml",
   		colNames:['Id', '','#Species', 'SpeciesId'],
   		colModel:[
   			{name:'id',index:'id',hidden:true},
   			{name:'name',index:'name',formatter:heirarchyLevelFormatter, width:300},
   			{name:'count', index:'count', width:50, hidden:true},
   			{name:'speciesId',index:'speciesId', hidden:true}
   		],   		
   		width: "${width?:'100%'}",
    	height: "${height?:'100%'}",
    	autowidth:true,
    	scrollOffset: 0,     	
    	scrollOffset: 0,
   		treeGrid: true,
   		ExpandColumn : 'name',
   		ExpandColClick  : true,
   		treeGridModel: 'adjacency',
        postData:{n_level:-1, expand_species:'${expandSpecies?:false}', expand_all:'${expandAll?:false}', speciesid:'${speciesId}', classSystem:$.trim($('#taxaHierarchy option:selected').val())},
        sortable:false,
        loadComplete:function(data) {
        	var postData = $("#taxonHierarchy").getGridParam('postData');
			postData["expand_species"] = false;
        	postData["expand_all"] = false;
	    }
	});

	$("#taxaHierarchy").change(function() {
		var postData = $("#taxonHierarchy").getGridParam('postData');
		postData["expand_species"] = ${expandSpecies?:false};
		postData["expand_all"] = ${expandAll?:false};
		postData["classSystem"] = $.trim($('#taxaHierarchy option:selected').val());
        $('#taxonHierarchy').trigger("reloadGrid");
	});
	
	$('.ui-jqgrid-hdiv').hide();
});

var heirarchyLevelFormatter = function(el, cellVal, opts) {
	var cells = $(opts).find('cell')
	var taxonId = $(cells[0]).text().trim()
	var speciesId = $(cells[3]).text().trim()
	var level = $(cells[4]).text()
	var levelTxt;
	if(level == ${TaxonomyRank.KINGDOM.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.KINGDOM}</span>"
	} else if(level == ${TaxonomyRank.PHYLUM.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.PHYLUM}</span>"
	} else if(level == ${TaxonomyRank.CLASS.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.CLASS}</span>"
	} else if(level == ${TaxonomyRank.ORDER.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.ORDER}</span>"
	} else if(level == ${TaxonomyRank.FAMILY.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.FAMILY}</span>"
	} else if(level == ${TaxonomyRank.SUB_FAMILY.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.SUB_FAMILY}</span>"
	} else if(level == ${TaxonomyRank.GENUS.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.GENUS}</span>"
	} else if(level == ${TaxonomyRank.SUB_GENUS.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.SUB_GENUS}</span>"
	} else if(level == ${TaxonomyRank.SPECIES.ordinal()} ) {
		levelTxt = ""
	}

	if(level == ${TaxonomyRank.SPECIES.ordinal() }) {
		el = "<a href='${createLink(action:"show")}/"+speciesId+"'>"+el+"</a>";
	} else {
		// el = "<a href='${createLink(action:"taxon")}/"+taxonId+"'
		// class='rank"+level+"'>"+levelTxt+": "+el+"</a>";
		el = levelTxt+": "+"<span class='rank"+level+"'>"+el+"&nbsp;<a class='taxonExpandAll' onClick='expandAll(\"taxonHierarchy\", \""+cellVal.rowId+"\")'>+</a> </span>"
	}
	return el;	   
}			
</g:javascript>


<div class="taxonomyBrowser">
	
	<select name="taxaHierarchy" id="taxaHierarchy"
		class="value ui-corner-all">
		<g:each in="${Classification.list()}" var="classification">
			<option value="${classification.id}">
				${classification.name}
			</option>
		</g:each>
	</select>
	
	<table id="taxonHierarchy"></table>
</div>
