
<div  class="block-tagadelic ">

	<form id="advSearchForm" method="get"  title="Search"
		action="${uGroup.createLink(controller:'project', action:'search') }"
		class="searchbox">
		<label class="control-label" for="name">Title</label> <input id="title"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.title" 
			placeholder="Search by Project title" value="${params.title}" />
			
		<label class="control-label" for="grantee">Grantee</label> <input id="grantee"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.grantee_organization"
			placeholder="Search by Grantee" value="${params.grantee}"/>

		<label class="control-label" for="sitename">Site Name</label> <input id="sitename"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.sitename"
			placeholder="Search by Site Name" value="${params.sitename}"/>
			
					<label class="control-label" for="corridor">Corridor</label> <input id="corridor"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.corridor"
			placeholder="Search by Corridior" value="${params.corridor}" />
			
					<label class="control-label" for="keywords">Keywords</label> <input id="keywords"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.tag"
			placeholder="Search by Keywords" value="${params.keywords}" />

		<g:hiddenField name="offset" value="0" />
		<g:hiddenField name="max" value="12" />
		<g:hiddenField name="fl" value="id" />


	<div class="form-action">
		<button type="submit" id="search-btn"
			class="btn btn-primary pull-right" style="margin-top:10px;">Search</button>
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
	        	return $.get("${uGroup.createLink(action:'terms', controller:'project') }"+'?field='+field, { term: query }, function (data) {
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