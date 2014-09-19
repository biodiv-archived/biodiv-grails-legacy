<%@page import="species.utils.Utils"%>
<div class="block-tagadelic">
	<form id="advSearchForm" method="get"
		action="${uGroup.createLink(controller:(params.controller!='userGroup')?params.controller:'newsletter', action:'search', userGroup:userGroupInstance) }"
		title="${g.message(code:'title.advanced.search')}" class="searchbox">

		<label class="control-label" for="aq.name"><g:message code="msg.Title" /> </label> <input id="aq.name"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.name" value="${params['aq.name']?.encodeAsHTML() }"
			placeholder="${g.message(code:'placeholder.search.titles')}" /> <label class="control-label"
			for="aq.text"><g:message code="msg.Content" /> </label> <input data-provide="typeahead" id="aq.text"
			type="text" class="input-block-level" name="aq.text"
			value="${params['aq.text']?.encodeAsHTML() }" placeholder="${g.message(code:'placeholder.search.content')}" />
	
		<div id="uGroupFilter" style="${params.webaddress?:'display:none;'}">	
		<label class="radio inline"> <input type="radio" id="uGroup_ALL" name="uGroup" 
			value="ALL"> <g:message code="msg.msg.Search.in.all.groups" />  </label> <label
			class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
			value="THIS_GROUP"> <g:message code="msg.msg.Search.within.this.group" /> </label>
		</div>
		<g:hiddenField name="start" value="0" />
		<g:hiddenField name="rows" value="10" />
		<g:hiddenField name="sort" value="score" />
		<g:hiddenField name="fl" value="id" />



	</form>
	<div class="form-action">
		<button type="submit" id="advSearch"
			class="btn btn-primary pull-right"><g:message code="msg.Search" /></button>
	</div>
	<div class="clearfix"></div>
</div>
<script type="text/javascript">
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
	
	$("#advSearch").click(function() {
		$( "#advSearchForm" ).submit();
	});
	$( "#advSearchForm" ).submit(function() {
		if($('#uGroup_ALL').is(':checked')) {
			$( "#advSearchForm" ).attr('action', "${Utils.getIBPServerDomain()}"+$( "#advSearchForm" ).attr('action'));
			updateGallery($( "#advSearchForm" ).attr('action'), undefined, undefined, undefined, false);
			return false;
		} 
		updateGallery($( "#advSearchForm" ).attr('action'), undefined, undefined, undefined, false);
		return false;
	});
	
	$("#uGroup_${queryParams.uGroup?:(params.webaddress?'THIS_GROUP':'ALL')}").click();
});
</script>
