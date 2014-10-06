<%@page import="species.utils.Utils"%>
<%@page import="java.text.SimpleDateFormat" %>

        <label
			class="control-label" for="observedOn">Observed during</label>
			
		<div id="observedOnDatePicker" style="position: relative;overflow:visible">
			<div id="observedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
        		<i class="icon-calendar icon-large"></i> <span class="date"></span>
    		</div>
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
	
	$("#observedOn").daterangepicker({
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
           parentEl:$("#observedOnDatePicker"),
           clickApply: function (e) {
            	this.hide();
            	return false;
        	}
        }, 
        function(start, end) {
           $('#observedOn span.date').html(start.toString('dd/MM/yyyy') + ' - ' + end.toString('dd/MM/yyyy'));
        });

	
        $('#observedOn span.date').html(startDate.toString('dd/MM/yyyy') + ' - ' +endDate.toString('dd/MM/yyyy'));

    });
</r:script>
