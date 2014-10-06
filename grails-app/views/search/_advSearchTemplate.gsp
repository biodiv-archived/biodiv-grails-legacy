<%@page import="species.utils.Utils"%>
<%@page import="java.text.SimpleDateFormat" %>

<g:set var="modules"  value="[[name:'All'], [name:'Species'], [name:'Observation'], [name:'Document'], [name:'SUser'], [name:'UserGroup']]"/>

<div  class="block-tagadelic ">

	<form id="advSearchForm" method="get"  title="Advanced Search"
		action="${uGroup.createLink(controller:'search', action:params.action?:'index') }"
		class="searchbox">
        
        <label class="control-label" for="module">Module</label>
        <select class="searchFilter moduleFilter" name="aq.object_type" style="width:100%">
            <g:each in="${modules}" var="module">
            <option value="${module.name}">${module.name}</option>
            </g:each>
        </select>

		<label class="control-label" for="name">Species</label> <input id="aq.name"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.name" value="${queryParams?queryParams['aq.name']?.encodeAsHTML():'' }"
			placeholder="Search by species name" />
			
			
		<label class="control-label" for="contributor">Contributor</label> <input id="aq.contributor"
			data-provide="typeahead" type="text" class="input-block-level"
			name="aq.contributor" value="${queryParams?queryParams['aq.contributor']?.encodeAsHTML():'' }"
			placeholder="Field to search all contributors" />

		<label class="control-label" for="location">Location</label> <input id="aq.location"
			data-provide="typeahead" type="text" class="input-block-level"
            name="aq.location" value="${queryParams?queryParams['aq.location']?.encodeAsHTML():''}"
            placeholder="Search by location name" />
    
        <label class="control-label" for="text">Content</label> <input id="aq.text"
			data-provide="typeahead" type="text" class="input-block-level"
            name="aq.text" value="${queryParams?queryParams['aq.text']?.encodeAsHTML():''}"
            placeholder="Search all text content" /> 
    
        <label class="control-label" for="members">Members</label> <input id="aq.members"
			data-provide="typeahead" type="text" class="input-block-level"
            name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
            placeholder="Search all members" /> 

        <label class="control-label" for="text">Tags</label> <input id="aq.tag"
			data-provide="typeahead" type="text" class="input-block-level"
            name="aq.tag" value="${queryParams?queryParams['aq.tag']?.encodeAsHTML():''}"
            placeholder="Search all tags" /> 

        <g:each in="${modules}" var="module">
            <g:if test="${!module.name.equalsIgnoreCase('All') &&!module.name.equalsIgnoreCase('SUser') && !module.name.equalsIgnoreCase('UserGroup') }">

            <div class="aq_modules ${module.name.toLowerCase()}_aq_filters ${activeFilters && activeFilters['aq.object_type'].equalsIgnoreCase(module.name)?'':'hide' }">
                <g:render template="/${module.name.toLowerCase()}/advSearchTemplate"/>
            </div>
            </g:if>
        </g:each>
   
        <label
			class="control-label" for="observedOn">Last updated during</label>
	
		<div id="uploadedOnDatePicker" style="position: relative;overflow:visible">
			<div id="uploadedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
        		<i class="icon-calendar icon-large"></i> <span class="date"></span>
    		</div>
		</div>
		
		<div style="${params.webaddress?:'display:none;'}">
		<label class="radio inline"> <input type="radio" id="uGroup_ALL" name="uGroup" 
			value="ALL"> Search in all groups </label> <label
			class="radio inline"> <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
			value="THIS_GROUP"> Search within this group </label>
		</div>

	</form>
	<div class="form-action">
		<button type="submit" id="advSearch"
			class="btn btn-primary pull-right" style="margin-top:10px;">Search</button>
	</div>

	<div class="clearfix"></div>

</div>
<r:script>

$(document).ready(function(){
	<%
		def df = new SimpleDateFormat('dd/MM/yyyy')
		def startDate = (params.daterangepicker_end)? df.parse(params.daterangepicker_start).getTime()  : null
		def endDate = (params.daterangepicker_end)? df.parse(params.daterangepicker_end).getTime(): null
	%>
	var startDate = "${startDate}";
	var endDate = "${endDate}";
	startDate = startDate? new Date(parseInt(startDate)):new Date(0);
	endDate =  endDate? new Date(parseInt(endDate)) :Date.today();
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
           startDate: startDate,
           endDate: endDate,
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

	
        $('#uploadedOn span.date').html(startDate.toString('dd/MM/yyyy') + ' - ' +endDate.toString('dd/MM/yyyy'));
	
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
		updateGallery($( "#advSearchForm" ).attr('action'), undefined, undefined, undefined, false);
		return false;
	});
	
//	$("#uGroup_${queryParams?queryParams.uGroup?:(params.webaddress?'THIS_GROUP':'ALL'):''}").click();

    $('select.moduleFilter option[value="${activeFilters?activeFilters['aq.object_type']:'' }"]').attr("selected",true);
    $('select.moduleFilter').click(function(e) {
        var val = $(this).val();
        $('.aq_modules').hide();
        $('.aq_modules input').val('');
        $('.'+val.toLowerCase()+'_aq_filters').show()
    });
});
</r:script>
