<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>

<r:script>
$(document).ready(function() {
	
	
  	$('#taxonHierarchy').jqGrid({
		url:'${uGroup.createLink(controller:'data', action:'listHierarchy', userGroupWebaddress:params.webaddress)}',
		datatype: "xml",
   		colNames:['Id', '_Id_', '', '#Species', 'SpeciesId', 'Class System'],
   		colModel:[
   			{name:'id',index:'id',hidden:true},
   			{name:'_id_',index:'id',hidden:true},
   			{name:'name',index:'name',formatter:heirarchyLevelFormatter},
   			{name:'count', index:'count',hidden:true, width:50},
   			{name:'speciesId',index:'speciesId', hidden:true},
   			{name:'classSystem', index:'classSystem', hidden:true}
   		],   		
   		width: "${width?:'100%'}",
    	height: "${height?:'100%'}", 
    	autowidth:true,   
    	scrollOffset: 0,
    	loadui:'block',
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
	    },
	    loadError : function(xhr, status, error) {
	    	if(xhr.status == 401) {
	    		show_login_dialog();
	    	} else {	    
	    		alert(error);
	    	}
	    	
	    } 
	});

	$("#taxaHierarchy").change(function() {
		var postData = $("#taxonHierarchy").getGridParam('postData');
		postData["expand_species"] = ${expandSpecies?:false};
		postData["expand_all"] = ${expandAll?:false};
		var selectedClassification = $('#taxaHierarchy option:selected').val();
		postData["classSystem"] = $.trim(selectedClassification);
        $('#taxonHierarchy').trigger("reloadGrid");
        $('#cInfo').html($("#c-"+$('#taxaHierarchy option:selected').val()).html());
	});
	 $('#cInfo').html($("#c-"+$('#taxaHierarchy option:selected').val()).html());
	$('.ui-jqgrid-hdiv').hide();
	$('#taxonHierarchy').parents('div.ui-jqgrid-bdiv').css("max-height","425px");
});

var heirarchyLevelFormatter = function(el, cellVal, opts) {
	var cells = $(opts).find('cell')
	var taxonId = $.trim($(cells[0]).text())
	var speciesId = $.trim($(cells[4]).text())
	var level = $(cells[6]).text()
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
		levelTxt = "<span class='rank'>${TaxonomyRank.SUB_FAMILY} </span>"
	} else if(level == ${TaxonomyRank.GENUS.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.GENUS}</span>"
	} else if(level == ${TaxonomyRank.SUB_GENUS.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.SUB_GENUS}</span>"
	} else if(level == ${TaxonomyRank.SPECIES.ordinal()} ) {
		levelTxt = ""
	}

	//el+= taxonId;
	if(level == ${TaxonomyRank.SPECIES.ordinal() }) {
		el = "<a href='${createLink(action:"show")}/"+speciesId+"'>"+el+"</a>";
	} else {
		// el = "<a href='${createLink(action:"taxon")}/"+taxonId+"'
		// class='rank"+level+"'>"+levelTxt+": "+el+"</a>";
		el = levelTxt+": "+"<span class='rank"+level+"'>"+el;
		
		if(${expandAllIcon == false?false:true}) {
			el += "&nbsp;<a class='taxonExpandAll' onClick='expandAll(\"taxonHierarchy\", \""+cellVal.rowId+"\", true)'>+</a>";
		}
		el+= "</span>"
	}
	return el;	   
}			
</r:script>


<div class="taxonomyBrowser sidebar_section" style="position: relative;">
        <h5>Classifications</h5>	
		<g:if test="${speciesInstance}">
			<g:set var="classifications" value="${speciesInstance.classifications()}" />
		</g:if><g:else>
			<g:set var="classifications" value="${Classification.list()}" />
		</g:else>
		<select name="taxaHierarchy" id="taxaHierarchy"
			class="value ui-corner-all" style="margin-bottom:0px;width:100%;background-color:whitesmoke;">
			<g:each in="${classifications}" var="classification">
				<option value="${classification.id}">
					${classification.name}
				</option>
			</g:each>
		</select>
		<div class="attributionBlock">
			<span class="ui-icon-info ui-icon-control " title="Show details"
				style="position: absolute; top: 0; right: 0; margin: 10px;"></span>
			<div class="ui-corner-all toolbarIconContent attribution"
				style="display: none;">
				<a class="ui-icon ui-icon-close" style="float: right;"></a> <span
					id="cInfo"></span>
				<g:each in="${classifications}" var="classification">
					<p id="c-${classification.id}" style="display: none;">
						${classification.citation}
					</p>
				</g:each>

			</div>
		</div>
	
	<table id="taxonHierarchy"></table>
</div>
