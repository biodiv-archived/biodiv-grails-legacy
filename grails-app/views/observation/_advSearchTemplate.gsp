<%@page import="species.utils.Utils"%>
<%@page import="java.text.SimpleDateFormat" %>

<div class="control-group">
    <label class="control-label" for="members">Members</label> 
    <div class="controls">
        <input id="aq.members"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
        placeholder="Search all members" /> 
    </div>
</div>


<div class="control-group">
    <label class="control-label" for="text">Tags</label> 
    <div class="controls">
        <input id="aq.tag"
        data-provide="typeahead" type="text" class="input-block-level"
        name="aq.tag" value="${queryParams?queryParams['aq.tag']?.encodeAsHTML():''}"
        placeholder="Search all tags" /> 
    </div>
</div>


<div class="control-group">
    <label
        class="control-label" for="observedOn">Last updated during</label>
    <div class="controls">

        <div id="uploadedOnDatePicker" style="position: relative;overflow:visible">
            <div id="uploadedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
                <i class="icon-calendar icon-large"></i> <span class="date"></span>
            </div>
        </div>
    </div>
</div>


<br/>	
<div class="control-group">
    <label
        class="control-label" for="observedOn">Observed during</label>

    <div class="controls">
        <div id="observedOnDatePicker" style="position: relative;overflow:visible">
            <div id="observedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
                <i class="icon-calendar icon-large"></i> <span class="date"></span>
            </div>
        </div>
    </div>

</div>

<div style="${params.webaddress?:'display:none;'}">

    <div class="control-group">
        <label class="radio inline"> 
            <input type="radio" id="uGroup_ALL" name="uGroup" 
            value="ALL"> Search in all groups </label> <label
            class="radio inline"> 
            <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
            value="THIS_GROUP"> Search within this group </label>
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
/*  ranges: {
'Today': ['today', 'today'],
'Yesterday': ['yesterday', 'yesterday'],
'Last 7 Days': [Date.today().add({ days: -6 }), 'today'],
'This Month': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
'Last Month': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })],
'From beginning of time' : [new Date(0), 'now']
},
*/  format: 'dd/MM/yyyy',
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
