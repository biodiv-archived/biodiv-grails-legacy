
<div class="block-tagadelic ">
	<form id="advSearchForm" method="get" title="Search"
		action="${uGroup.createLink(controller:params.controller, action:params.action)}"
		class="searchbox">


		<label class="control-label" for="title">Title</label> <input
			id="aq.title" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.title"
			placeholder="Search by Document title" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
		
		<label
			class="control-label" for="grantee">Type</label> <input
			id="aq.type" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.type"
			placeholder="Search by Description" value="${(queryParams?.get('aq.type'))?.encodeAsHTML()}" />
			
		<label
			class="control-label" for="grantee">Description</label> <input
			id="aq.description" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.description"
			placeholder="Search by Description" value="${(queryParams?.get('aq.description'))?.encodeAsHTML()}" />

		<label
			class="control-label" for="aq.contributor">Contributor</label> <input data-provide="typeahead" id="aq.contributor"
			type="text" class="input-block-level" name="aq.contributor" value="${(queryParams?.get('aq.contributor'))?.encodeAsHTML()}" 
			placeholder="Field to search all contributors" />
		
		<label
			class="control-label" for="aq.attribution">Attributions</label> <input data-provide="typeahead" id="aq.attribution"
			type="text" class="input-block-level" name="aq.attribution" value="${(queryParams?.get('aq.attribution'))?.encodeAsHTML() }"
			placeholder="Field to search all attributions" />

		<label class="control-label" for="keywords">Tags</label> <input
			id="aq.tag" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.tag"
			placeholder="Search by Keywords" value="${(queryParams?.get('aq.tag'))?.encodeAsHTML()}" />
			
		<div style="${params.webaddress?:'display:none;'}">
		<label class="radio inline"> <input type="radio" id="uGroup_ALL" name="uGroup" 
			value="ALL"> Search in all groups </label> <label
			class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
			value="THIS_GROUP"> Search within this group </label>
		</div>

<%--		<g:hiddenField name="offset" value="0" />--%>
<%--		<g:hiddenField name="max" value="12" />--%>
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