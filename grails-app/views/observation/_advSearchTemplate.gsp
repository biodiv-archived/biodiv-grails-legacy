<div id="advSearchBox" class="block-tagadelic ">
	<form id="advSearchForm" method="get" action="" title="Advanced Search"
		class="searchbox">
		<label class="control-label" for="contributor">Contributor</label> <input
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.contributor" value="${params['aq.contributor'] }"
			placeholder="Field to search all contributors" />

		<!-- label
			class="control-label" for="maxvotedspeciesname">Species Name</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.maxvotedspeciesname" value="" 
			placeholder="Search by scientific name" />
			
		<label class="control-label" for="common_name">Common Name</label> <input data-provide="typeahead"
			type="text" class="input-block-level" name="aq.common_name"
			placeholder="Search using common name" /-->

		<label class="control-label" for="location">Location</label> <input
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.location" value="${params['aq.location']}" placeholder="Search by location name" />
		<label class="control-label" for="text">Content</label> <input
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.text" value="${params['aq.text']}" placeholder="Search all text content" /> <label
			class="control-label" for="uploadedOn">Uploaded during</label>
			
		<div id="uploadedOnDatePicker" style="position: relative;overflow:visible">
			<div id="uploadedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
        		<i class="icon-calendar icon-large"></i> <span class="date"></span>
    		</div>
		</div>


		<g:hiddenField name="offset" value="0" />
		<g:hiddenField name="max" value="9" />
		<g:hiddenField name="sort" value="score" />
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
	
	$("#uploadedOn").daterangepicker({
	     ranges: {
              'Today': ['today', 'today'],
              'Yesterday': ['yesterday', 'yesterday'],
              'Last 7 Days': [Date.today().add({ days: -6 }), 'today'],
              'This Month': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
              'Last Month': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })],
              'From beginning of time' : [new Date(0), 'now']
           },
           format: 'dd/MM/yyyy',
           maxDate: Date.today(),
           parentEl:$("#uploadedOnDatePicker"),
           clickApply: function (e) {
            	this.hide();
            	return false;
        	}
        }, 
        function(start, end) {
           $('#uploadedOn span.date').html(start.toString('dd/MM/yyyy') + ' - ' + end.toString('dd/MM/yyyy'));
        });

	var startDate = "${params.daterangepicker_start}";
	var endDate = "${params.daterangepicker_end}";
    $('#uploadedOn span.date').html((startDate?startDate:(new Date(0)).toString('dd/MM/yyyy')) + ' - ' + (endDate?endDate:Date.today().toString('dd/MM/yyyy')));
	
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

	
	$( "#advSearch" ).button().click(function() {
		updateGallery();
    	return false;
	});
	
});
</r:script>