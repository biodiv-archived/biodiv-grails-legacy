
<div class="block-tagadelic ">

	<form id="advSearchForm" method="get" title="Search"
		action="${uGroup.createLink(controller:'document', action:'search') }"
		class="searchbox">


		<label class="control-label" for="title">Title</label> <input
			id="title" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.title"
			placeholder="Search by Document title" value="${params.title}" /> <label
			class="control-label" for="grantee">Description</label> <input
			id="description" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.descriptionn"
			placeholder="Search by Description" value="${params.description}" />



		<label class="control-label" for="keywords">Keywords</label> <input
			id="keywords" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.tag"
			placeholder="Search by Keywords" value="${params.keywords}" />

		<g:hiddenField name="offset" value="0" />
		<g:hiddenField name="max" value="12" />
		<g:hiddenField name="fl" value="id" />


		<div class="form-action">
			<button type="submit" id="search-btn"
				class="btn btn-primary pull-right" style="margin-top: 10px;">Search</button>
		</div>
	</form>
	<div class="clearfix"></div>

</div>

<r:script>

$(document).ready(function(){
	
	$('#advSearchForm :input:not(input[type=hidden])').each(function(index, ele) {
		var field = $(this).attr('name');
		$(this).typeahead({
			source: function (query, process) {
	        	return $.get("${uGroup.createLink(action:'terms', controller:'document') }"+'?field='+field, { term: query }, function (data) {
	            	return process(data);
	        	});
    		}
		});
	});

	$("#advSearch").click(function() {
		$( "#advSearchForm" ).submit();
	});
	$( "#advSearchForm" ).submit(function() {
		updateGallery($( "#advSearchForm" ).attr('action'), undefined, undefined, undefined, true);
		return false;
	});
	$("#uGroup_${(queryParams && queryParams.uGroup)?queryParams.uGroup:(params.webaddress?'THIS_GROUP':'ALL')}").click();

});
</r:script>