<div id="advSearchBox" class="block-tagadelic">
	<form id="advSearchForm" method="get" action="${uGroup.createLink(controller:'species', action:'search')}"
		title="Advanced Search" class="searchbox">
		<!-- label class="control-label" for="name">Name</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="name"
			id="advSearchTextField" placeholder="Search using species name" /--> <label
			class="control-label" for="taxon">Taxon Hierarchy</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="taxon" value=""
			placeholder="Search using taxon hierarchy" />
			
			<label
			class="control-label" for="contributor">Contributor</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="contributor" value="" 
			placeholder="Field to search all contributors" /> <label
			class="control-label" for="attribution">Attributions</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="attribution" value=""
			placeholder="Field to search all attributions" />
			
			<!-- label
			class="control-label" for="author">Species Author</label> <input data-provide="typeahead"
			type="text" name="author" class="input-block-level"
			placeholder="Search using species author or basionym author" /> <label
			class="control-label" for="year">Year</label> <input data-provide="typeahead" type="text"
			class="input-block-level" name="year"
			placeholder="Search using year of finding the species and basionym year" /-->

		<label class="control-label" for="text">Content</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="text" value=""
			placeholder="Search all text content" />  <!-- label
			class="control-label" for="reference">References</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="reference" value=""
			placeholder="Field to search all references" /-->

				
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
	        	return $.get("${uGroup.createLink(action:'terms', controller:'species') }"+'?field='+field, { term: query }, function (data) {
	            	return process(data);
	        	});
    		}
		});
	});

	$( "#advSearch" ).button().click(function() {
		$( "#advSearchForm" ).ajaxSubmit({
               url:"${uGroup.createLink(controller:'species', action:'search')}",
               dataType: 'html',
               type: 'POST',
               data: {'query':$('#searchTextField').val()},
               success: function(data, statusText, xhr, form) {
					$("#searchResults").html(data);
          			return false;
       			},
       			error:function (xhr, ajaxOptions, thrownError){
           			var successHandler = this.success;
           			handleError(xhr, ajaxOptions, thrownError, successHandler, function() { return false;});
            			return false;
           		}
   		});
	});

});
</g:javascript>