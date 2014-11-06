<%@page import="species.utils.Utils"%>
<%@page import="java.text.SimpleDateFormat" %>
<g:set var="modules"  value="[[name:'All',displayName:g.message(code:'default.all.label') ], [name:'Species', template:'species',displayName:g.message(code:'default.species.label')], [name:'Observation', template:'observation',displayName:g.message(code:'observation.label')], [name:'Document', template:'document',displayName:g.message(code:'feature.part.document')], [name:'SUser', template:'SUser',displayName:g.message(code:'search.suser')], [name:'UserGroup', template:'userGroup',displayName:g.message(code:'userGroup.label')]]"/>

<div  class="block-tagadelic">

    <form id="advSearchForm" method="get"  title="${g.message(code:'button.advanced.search')}"
        action="${uGroup.createLink(controller:'search', action:'select') }"
        class="searchbox form-horizontal">

        <div class="control-group">
            <label class="control-label" for="module">${g.message(code:"default.label.module")}</label>
            <div class="controls">
                <select class="searchFilter moduleFilter" name="aq.object_type" style="width:100%">
                    <g:each in="${modules}" var="module">
                   <option value="${module.name}">${module.displayName}</option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="name">${g.message(code:"default.species.label")}</label> 
            <div class="controls">
                <input id="aq.name"
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.name" value="${queryParams?queryParams['aq.name']?.encodeAsHTML():'' }"
                placeholder="${g.message(code:'placeholder.search.species.name')}" />
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="location">${g.message(code:"default.location.label")}</label> 
            <div class="controls">
                <input id="aq.location"
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.location" value="${queryParams?queryParams['aq.location']?.encodeAsHTML():''}"
                placeholder="${g.message(code:'placeholder.search.location.name')}" />
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="contributor">${g.message(code:"placeholder.contributor")}</label> 
            <div class="controls">
                <input id="aq.contributor"
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.contributor" value="${queryParams?queryParams['aq.contributor']?.encodeAsHTML():'' }"
                placeholder="${g.message(code:'placeholder.all.search.contributors')}" />
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="attribution">${g.message(code:"default.attribution.label")}</label> 
            <div class="controls">
                <input id="aq.attribution"
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.attribution" value="${queryParams?queryParams['aq.attribution']?.encodeAsHTML():'' }"
                placeholder="${g.message(code:'placeholder.all.search.attributions')}" />
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="license">${g.message(code:"default.licenses.label")}</label> 
            <div class="controls">
                <input id="aq.license"
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.license" value="${queryParams?queryParams['aq.license']?.encodeAsHTML():'' }"
                placeholder="${g.message(code:'placeholder.license')}" />
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="text">${g.message(code:"default.content.label")}</label> 
            <div class="controls">
                <input id="aq.text"
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.text" value="${queryParams?queryParams['aq.text']?.encodeAsHTML():''}"
                placeholder="${g.message(code:'placeholder.search.all.content')}" /> 
            </div>
        </div>

        <g:each in="${modules}" var="module">
        <g:if test="${!module.name.equalsIgnoreCase('All')}">

        <div class="aq_modules ${module.name.toLowerCase()}_aq_filters ${activeFilters && activeFilters['aq.object_type']?.equalsIgnoreCase(module.name)?'':'hide' }">
            <g:render template="/${module.template}/advSearchTemplate"/>
        </div>
        </g:if>
        </g:each>

    <div class="form-action">
        <button type="submit" id="advSearch"
            class="btn btn-primary pull-right" style="margin-top:10px;">${g.message(code:"default.search")}</button>
    </div>
    </form>

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
        /*ranges: {
        'Today': ['today', 'today'],
        'Yesterday': ['yesterday', 'yesterday'],
        'Last 7 Days': [Date.today().add({ days: -6 }), 'today'],
        'This Month': [Date.today().moveToFirstDayOfMonth(), Date.today().moveToLastDayOfMonth()],
        'Last Month': [Date.today().moveToFirstDayOfMonth().add({ months: -1 }), Date.today().moveToFirstDayOfMonth().add({ days: -1 })],
        'From beginning of time' : [new Date(0), 'now']
        },
        */format: 'dd/MM/yyyy',
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
    resetSearchFilters();
    updateGallery($( "#advSearchForm" ).attr('action'), undefined, undefined, undefined, false);
    return false;
    });

    //	$("#uGroup_${queryParams?queryParams.uGroup?:(params.webaddress?'THIS_GROUP':'ALL'):''}").click();

    $('select.moduleFilter option[value="${activeFilters?activeFilters['aq.object_type']:'' }"]').attr("selected",true);
    $('select.moduleFilter').click(function(e) {
        var val = $(this).val();
        $('.aq_modules').hide();
        $('input[name*="aq."]').val('').parent().parent().show();
        hideAqSearchControls(val);
    });

    function hideAqSearchControls(val) {

        if(val == 'Observation') {
            $('input[name="aq.attribution"]').val('').parent().parent().hide();
        } else if (val == 'SUser') {
            $('input[name="aq.name"],input[name="aq.contributor"],input[name="aq.attribution"],input[name="aq.license"],input[name="aq.tag"]').val('').parent().parent().hide();
        } else if (val == 'UserGroup') {
            $('input[name="aq.name"],input[name="aq.contributor"],input[name="aq.attribution"],input[name="aq.license"],input[name="aq.text"],input[name="aq.tag"],input[name="aq.uGroup"]').val('').parent().parent().hide();
        }

        $('.'+val.toLowerCase()+'_aq_filters').show()
    }

    hideAqSearchControls($('select.moduleFilter').val());
});
</r:script>
