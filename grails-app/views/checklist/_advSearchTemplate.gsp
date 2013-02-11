<%@page import="species.utils.Utils"%>
<div  class="block-tagadelic ">

	<form id="advSearchForm" method="get"  title="Advanced Search"
		action="${uGroup.createLink(controller:(params.controller!='userGroup')?params.controller:'checklist', action:'search') }"
		class="searchbox">
		<label class="control-label" for="name">Species</label> <input id="aq.name"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.name" value="${queryParams['aq.name']?.encodeAsHTML() }"
			placeholder="Search by species name" />
		
		<label class="control-label" for="title">Title</label> <input id="aq.title"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.title" value="${queryParams['aq.title']?.encodeAsHTML() }"
			placeholder="Search by title" />

		<label class="control-label" for="attribution">Attribution</label> <input id="aq.attribution"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.attribution" value="${queryParams['aq.attribution']?.encodeAsHTML() }"
			placeholder="Search by attribution" />

		<label class="control-label" for="location">Location</label> <input id="aq.location"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.location" value="${queryParams['aq.location']?.encodeAsHTML()}" placeholder="Search by location name" />
			
		<label class="control-label" for="text">Content</label> <input id="aq.text"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.text" value="${queryParams['aq.text']?.encodeAsHTML()}" placeholder="Search all text content" /> <label
			class="control-label" for="uploadedOn">Uploaded during</label>
			
		
		
		<div style="${params.webaddress?:'display:none;'}">
		<label class="radio inline"> <input type="radio" id="uGroup_ALL" name="uGroup" 
			value="ALL"> Search in all groups </label> <label
			class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
			value="THIS_GROUP"> Search within this group </label>
		</div>

		<g:hiddenField name="offset" value="0" />
		<g:hiddenField name="max" value="12" />
		<g:hiddenField name="fl" value="id" />

	</form>
	<div class="form-action">
		<button type="submit" id="advSearch"
			class="btn btn-primary pull-right" style="margin-top:10px;">Search</button>
	</div>

	<div class="clearfix"></div>

</div>
<r:script>

$(document).ready(function(){
		$('#advSearchForm :input:not(input[type=hidden])').each(function(index, ele) {
		var field = $(this).attr('name');
		$(this).typeahead({
			source: function (query, process) {
	        	return $.get("${uGroup.createLink(action:'terms', controller:'observation') }"+'?field='+field, { term: query }, function (data) {
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
	
	$("#uGroup_${queryParams.uGroup?:(params.webaddress?'THIS_GROUP':'ALL')}").click();
	
	
});
</r:script>