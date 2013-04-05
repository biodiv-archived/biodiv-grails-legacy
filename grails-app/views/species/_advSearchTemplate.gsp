<%@page import="species.utils.Utils"%>
<div class="block-tagadelic">
	<form id="advSearchForm" method="get" 		
		action="${uGroup.createLink(controller:(params.controller!='userGroup')?params.controller:'species', action:'search') }"
		title="Advanced Search" class="searchbox">
		<label class="control-label" for="name">Species</label> <input id="aq.name"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.name" value="${(queryParams?.get('aq.name'))?.encodeAsHTML() }"
			placeholder="Search by species name" />
	 <label
			class="control-label" for="aq.taxon">Taxon Hierarchy</label> <input data-provide="typeahead" id="aq.taxon"
			type="text" class="input-block-level" name="aq.taxon" value="${(queryParams?.get('aq.taxon'))?.encodeAsHTML()}"
			placeholder="Search using taxon hierarchy" />
			
			<label
			class="control-label" for="aq.contributor">Contributor</label> <input data-provide="typeahead" id="aq.contributor"
			type="text" class="input-block-level" name="aq.contributor" value="${(queryParams?.get('aq.contributor'))?.encodeAsHTML()}" 
			placeholder="Field to search all contributors" /> <label
			class="control-label" for="aq.attribution">Attributions</label> <input data-provide="typeahead" id="aq.attribution"
			type="text" class="input-block-level" name="aq.attribution" value="${(queryParams?.get('aq.attribution'))?.encodeAsHTML() }"
			placeholder="Field to search all attributions" />
			
			<!-- label
			class="control-label" for="aq.author">Species Author</label> <input data-provide="typeahead"
			type="text" name="aq.author" class="input-block-level"
			placeholder="Search using species author or basionym author" /> <label
			class="control-label" for="aq.year">Year</label> <input data-provide="typeahead" type="text"
			class="input-block-level" name="aq.year"
			placeholder="Search using year of finding the species and basionym year" /-->

		<label class="control-label" for="aq.text">Content</label> <input data-provide="typeahead" id="aq.text"
			type="text" class="input-block-level" name="aq.text" value="${(queryParams?.get('aq.text'))?.encodeAsHTML() }"
			placeholder="Search all text content" />  <!-- label
			class="control-label" for="aq.reference">References</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.reference" value=""
			placeholder="Field to search all references" /-->
			
		<div id="uploadedOnDatePicker" style="position: relative;overflow:visible">
			<div id="uploadedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
        		<i class="icon-calendar icon-large"></i> <span class="date"></span>
    		</div>
		</div>
		
		<div style="${params.webaddress?:'display:none;'}">
		<label class="radio inline"> <input type="radio" id="uGroup_ALL" name="uGroup" 
			value="ALL"> Search in all groups </label> <label
			class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
			value="THIS_GROUP"> Search within this group </label>
		</div>

<%--		<g:hiddenField name="start" value="0" />--%>
<%--		<g:hiddenField name="rows" value="10" />--%>
<%--		<g:hiddenField name="sort" value="score" />--%>
<%--		<g:hiddenField name="fl" value="id" />--%>



	</form>
	<div class="form-action">
		<button type="submit" id="advSearch"
			class="btn btn-primary pull-right">Search</button>
	</div>


<div class="clearfix"></div>
</div>
<r:script>

$(document).ready(function() {
	var startDate = "${params.daterangepicker_start}";
	var endDate = "${params.daterangepicker_end}";
	startDate = Date.parse(startDate?startDate:(new Date(0)).toString('dd/MM/yyyy'));
	endDate =  (endDate?Date.parse(endDate):Date.today());
	
	$("#uploadedOn").daterangepicker({
	     ranges: {
              'Today': ['today', 'today'],
              'Yesterday': ['yesterday', 'yesterday'],
              'Last 7 Days': [Date.today().add({ days: -6 }), 'today'],
              'This Month': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
              'Last Month': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })],
              'From beginning of time' : [new Date(0), 'now']
           },
           format: 'dd/MM/yyyy',
           startDate: startDate,
           endDate: endDate,
           maxDate: Date.today(),
           parentEl:$("#uploadedOnDatePicker"),
           clickApply: function (e) {
            	this.hide();
            	return false;
        	}
        }, 
        function(start, end) {
           $('#uploadedOn span.date').html(start.toString('dd/MM/yyyy') + ' - ' + end.toString('dd/MM/yyyy'));
        });

	
    $('#uploadedOn span.date').html(startDate.toString('dd/MM/yyyy') + ' - ' +endDate.toString('dd/MM/yyyy'));
	
	$('#advSearchForm :input:not(input[type=hidden])').each(function(index, ele) {
		var field = $(this).attr('name');
		$(this).typeahead({
			source: function (query, process) {
	        	return $.get("${uGroup.createLink(action:'terms', controller:'species') }"+'?field='+field, { term: query }, function (data) {
	            	return process(data);
	        	});
    		}
		});
	});

	$("#advSearch").click(function() {
		$( "#advSearchForm" ).submit();
	});
	$( "#advSearchForm" ).submit(function() {
		if($('#uGroup_ALL').is(':checked')) {
			$( "#advSearchForm" ).attr('action', "${Utils.getIBPServerDomain()}"+$( "#advSearchForm" ).attr('action'));
			updateGallery($( "#advSearchForm" ).attr('action'), undefined, undefined, undefined, false);
			return false;
		} 
		updateGallery($( "#advSearchForm" ).attr('action'), undefined, undefined, undefined, true);
		return false;
	});
	$("#uGroup_${(queryParams && queryParams.uGroup)?queryParams.uGroup:(params.webaddress?'THIS_GROUP':'ALL')}").click();

});
</r:script>