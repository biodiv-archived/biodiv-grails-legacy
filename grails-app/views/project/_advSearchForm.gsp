
<div  class="block-tagadelic ">

	<form id="advSearchForm" method="get"  title="Search"
		action="${uGroup.createLink(controller:params.controller, action:params.action, userGroup:userGroupInstance) }"
		class="searchbox">
		<label class="control-label" for="name">Title</label>
			<input id="aq.title"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.title" 
			placeholder="Search by Project title" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
			
		<label class="control-label" for="grantee">Grantee</label> <input id="aq.grantee_organization"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.grantee_organization"
			placeholder="Search by Grantee" value="${(queryParams?.get('aq.grantee_organization'))?.encodeAsHTML()}"/>

		<label class="control-label" for="sitename">Site Name</label> <input id="aq.sitename"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.sitename"
			placeholder="Search by Site Name" value="${(queryParams?.get('aq.sitename'))?.encodeAsHTML()}"/>
			
		<label class="control-label" for="corridor">Corridor</label> <input id="aq.corridor"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.corridor"
			placeholder="Search by Corridior" value="${(queryParams?.get('aq.corridor'))?.encodeAsHTML()}" />
			
		<label class="control-label" for="tags">Tags</label> <input id="aq.tag"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.tag"
			placeholder="Search by Tags" value="${(queryParams?.get('aq.tag'))?.encodeAsHTML()}" />
			
					<div style="${params.webaddress?:'display:none;'}">
		<label class="radio inline"> <input type="radio" id="uGroup_ALL" name="uGroup" 
			value="ALL"> Search in all groups </label> <label
			class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
			value="THIS_GROUP"> Search within this group </label>
		</div>

	<%--		<g:hiddenField name="offset" value="0" />--%>
	<%--	<g:hiddenField name="max" value="12" /> --%>
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