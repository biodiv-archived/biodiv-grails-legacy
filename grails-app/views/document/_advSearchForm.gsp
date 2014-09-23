
<div class="block-tagadelic ">
	<form id="advSearchForm" method="get" title="${g.message(code:'default.search')}"
		action="${uGroup.createLink(controller:params.controller, action:params.action, userGroup:userGroupInstance)}"
		class="searchbox">


		<label class="control-label" for="title"><g:message code="default.title.label" /></label> <input
			id="aq.title" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.title"
			placeholder="${g.message(code:'placeholder.search.document')}" value="${(queryParams?.get('aq.title'))?.encodeAsHTML() }" />
		
		<label
			class="control-label" for="grantee"><g:message code="default.type.label" /></label> <input
			id="aq.type" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.type"
			placeholder="${g.message(code:'placeholder.search.description')}" value="${(queryParams?.get('aq.type'))?.encodeAsHTML()}" />
			
		<label
			class="control-label" for="grantee"><g:message code="default.description.label" /></label> <input
			id="aq.description" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.description"
			placeholder="${g.message(code:'placeholder.search.description')}" value="${(queryParams?.get('aq.description'))?.encodeAsHTML()}" />

		<label
			class="control-label" for="aq.contributor"><g:message code="default.contributors.label" /></label> <input data-provide="typeahead" id="aq.contributor"
			type="text" class="input-block-level" name="aq.contributor" value="${(queryParams?.get('aq.contributor'))?.encodeAsHTML()}" 
			placeholder="${g.message(code:'placeholder.species.field.search')}" />
		
		<label
			class="control-label" for="aq.attribution"><g:message code="default.attributions.label" /></label> <input data-provide="typeahead" id="aq.attribution"
			type="text" class="input-block-level" name="aq.attribution" value="${(queryParams?.get('aq.attribution'))?.encodeAsHTML() }"
			placeholder="${g.message(code:'placeholder.species.search.attributions')}" />

		<label class="control-label" for="tags"><g:message code="default.tags.label" /></label> <input
			id="aq.tag" data-provide="typeahead" type="text"
			class="input-block-level" name="aq.tag"
			placeholder="${g.message(code:'placeholder.search.tags')}" value="${(queryParams?.get('aq.tag'))?.encodeAsHTML()}" />
			
		<div style="${params.webaddress?:'display:none;'}">
		<label class="radio inline"> <input type="radio" id="uGroup_ALL" name="uGroup" 
			value="ALL"> <g:message code="default.search.in.all.groups" /> </label> <label
			class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
			value="THIS_GROUP"> <g:message code="default.search.within.this.group" /> </label>
		</div>

<%--		<g:hiddenField name="offset" value="0" />--%>
<%--		<g:hiddenField name="max" value="12" />--%>
		<g:hiddenField name="fl" value="id" />


		<div class="form-action">
			<button type="submit" id="search-btn"
				class="btn btn-primary pull-right" style="margin-top: 10px;"><g:message code="default.search" /></button>
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
