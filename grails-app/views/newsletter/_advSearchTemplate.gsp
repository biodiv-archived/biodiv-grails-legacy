<div class="block-tagadelic">
	<form id="advSearchForm" method="get" action="${uGroup.createLink(controller:'newsletter', action:'search')}"
		title="Advanced Search" class="searchbox">
			
		<label class="control-label" for="aq.name">Title</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.name" value="${params['aq.name'] }"
			placeholder="Search all titles" />
		<label class="control-label" for="aq.text">Content</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.text" value="${params['aq.text'] }"
			placeholder="Search all text content" />
		<label class="control-label" for="aq.group">Group</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.group" value="${params['aq.group']}"
			placeholder="Search within group" />

				
		<g:hiddenField name="start" value="0" />
		<g:hiddenField name="rows" value="10" />
		<g:hiddenField name="sort" value="score" />
		<g:hiddenField name="fl" value="id" />



	</form>
	<div class="form-action">
		<button type="submit" id="advSearch"
			class="btn btn-primary pull-right">Search</button>
	</div>



</div>
<g:javascript>

$(document).ready(function(){
	
	$('#advSearchForm :input:not(input[type=hidden])').each(function(index, ele) {
		var field = $(this).attr('name');
		$(this).typeahead({
			source: function (query, process) {
	        	return $.get("${uGroup.createLink(action:'terms', controller:'newsletter') }"+'?field='+field, { term: query }, function (data) {
	            	return process(data);
	        	});
    		}
		});
	});

	
	$( "#advSearch" ).button().click(function() {
		updateGallery(undefined, undefined, undefined, undefined, false, undefined);
    	return false;
	});

});
</g:javascript>