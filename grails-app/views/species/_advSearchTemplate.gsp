<%@page import="species.utils.Utils"%>
<div class="block-tagadelic">
	<form id="advSearchForm" method="get" 		
		action="${uGroup.createLink(controller:(params.controller!='userGroup')?params.controller:'species', action:'search') }"
		title="Advanced Search" class="searchbox">
	 <label
			class="control-label" for="aq.taxon">Taxon Hierarchy</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.taxon" value=""
			placeholder="Search using taxon hierarchy" />
			
			<label
			class="control-label" for="aq.contributor">Contributor</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.contributor" value="" 
			placeholder="Field to search all contributors" /> <label
			class="control-label" for="aq.attribution">Attributions</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.attribution" value=""
			placeholder="Field to search all attributions" />
			
			<!-- label
			class="control-label" for="aq.author">Species Author</label> <input data-provide="typeahead"
			type="text" name="aq.author" class="input-block-level"
			placeholder="Search using species author or basionym author" /> <label
			class="control-label" for="aq.year">Year</label> <input data-provide="typeahead" type="text"
			class="input-block-level" name="aq.year"
			placeholder="Search using year of finding the species and basionym year" /-->

		<label class="control-label" for="aq.text">Content</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.text" value=""
			placeholder="Search all text content" />  <!-- label
			class="control-label" for="aq.reference">References</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.reference" value=""
			placeholder="Field to search all references" /-->
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

$(document).ready(function(){
	
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