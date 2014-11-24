<%@page import="species.utils.Utils"%>
<%@page import="java.text.SimpleDateFormat" %>


<div class="control-group">
    <label
        class="control-label" for="observedOn">${g.message(code:'label.observed.during')}</label>

    <div class="controls">
        <div id="observedOnDatePicker" class="dropdown" style="position: relative;overflow:visible">
            <div id="observedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
                <i class="icon-calendar icon-large"></i> <span class="date"></span>
            </div>
        </div>
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
            /*ranges: {
                'Today': [moment(), moment()],
                'Yesterday': [moment().subtract('days', 1), moment().subtract('days', 1)],
                'Last 7 Days': [moment().subtract('days', 6), new Date()],
                'Last 30 Days': [moment().subtract('days', 29), new Date()],
                'This Month': [moment().startOf('month'), moment().endOf('month')],
                'Last Month': [moment().subtract('month', 1).startOf('month'), moment().subtract('month', 1).endOf('month')]
            },*/
            format: 'DD/MM/YYYY',
            startDate: startDate,
            endDate: endDate,
            maxDate: moment(),
            parentEl:$("#observedOnDatePicker")
        }, 
        function(start, end, label) {
            $('#observedOn span.date').html(start.format('DD/MM/YYYY') + ' - ' + end.format('DD/MM/YYYY'));
        }
    );

    $('#observedOn span.date').html(startDate.toString('dd/MM/yyyy') + ' - ' + endDate.toString('dd/MM/yyyy'));
});
</r:script>



