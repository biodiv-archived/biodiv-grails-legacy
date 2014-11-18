<%@page import="species.utils.Utils"%>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="species.License.LicenseType"%>

<g:set var="modules"  value="[[name:'All',displayName:g.message(code:'default.all.label') ], [name:'Species', template:'species',displayName:g.message(code:'default.species.label')], [name:'Observation', template:'observation',displayName:g.message(code:'observation.label')], [name:'Document', template:'document',displayName:g.message(code:'feature.part.document')], [name:'SUser', template:'SUser',displayName:g.message(code:'search.suser')], [name:'UserGroup', template:'userGroup',displayName:g.message(code:'userGroup.label')], [name:'Resource', template:'resource',displayName:g.message(code:'resource.label')]]"/>

<div  class="block-tagadelic">

    <form id="advSearchForm" method="get"  title="${g.message(code:'button.advanced.search')}"
        action="${uGroup.createLink(controller:'search', action:'select', userGroup:userGroupInstance) }"
        class="searchbox form-horizontal">

        <div class="control-group">
            <label class="control-label" for="module">${g.message(code:"default.label.module")}</label>
            <div class="controls">
                <select class="searchFilter moduleFilter btn" name="aq.object_type" style="width:100%">
                    <g:each in="${modules}" var="module">
                   <option value="${module.name}">${module.displayName}</option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="name">${g.message(code:"default.species.label")}</label> 
            <div class="controls nameContainer">
                <input id="aq.name"
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.name" value="${queryParams?queryParams['aq.name']?.encodeAsHTML():'' }"
                placeholder="${g.message(code:'placeholder.search.species.name')}" />
                    <div class='nameSuggestions' style='display: block;'></div>

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
            <label class="control-label" for="text">${g.message(code:"default.content.label")}</label> 
            <div class="controls">
                <input id="aq.text"
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.text" value="${queryParams?queryParams['aq.text']?.encodeAsHTML():''}"
                placeholder="${g.message(code:'placeholder.search.all.content')}" /> 
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="text">${g.message(code:'default.tags.label')}</label> 
            <div class="controls">
                <input
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.tag" value="${queryParams?queryParams['aq.tag']?.encodeAsHTML():''}"
                placeholder="${g.message(code:'placeholder.search.all.tags')}" /> 
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
            <label class="control-label" for="members">${g.message(code:'default.members.label')}</label> 
            <div class="controls">
                <input
                data-provide="typeahead" type="text" class="input-block-level"
                name="aq.members" value="${queryParams?queryParams['aq.members']?.encodeAsHTML():''}"
                placeholder="${g.message(code:'placeholder.search.members')}" /> 
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="license">${g.message(code:"default.licenses.label")}</label> 
            <div class="controls">
                <select name="aq.license" multiple="multiple" class="multiselect licenseFilter input-block-level">
                    <g:each in="${LicenseType.toList()}" var="license">
                    <option value="${license.name()}"> ${g.message(error:license)} </option>
                    </g:each>
                </select>

            </div>
        </div>
        <div class="control-group">
            <label
                class="control-label" for="observedOn">${g.message(code:'label.createdon')}</label>

            <div class="controls">
                <div id="uploadedOnDatePicker" class="dropdown" style="position: relative;overflow:visible">
                    <div id="uploadedOn" class="btn pull-left" style="text-align:left;padding:5px;" >
                        <i class="icon-calendar icon-large"></i> <span class="date"></span>
                    </div>
                </div>
            </div>
        </div>

        <div class="control-group">
            <div style="${params.webaddress?:'display:none;'}">
                <label class="radio inline"> 
                    <input type="radio" id="uGroup_ALL" name="uGroup" 
                    value="ALL"> ${g.message(code:'default.search.in.all.groups')} </label> <label
                    class="radio inline"> 
                   

                    <input type="radio" id="uGroup_THIS_GROUP" name="uGroup" 
                    value="${userGroupInstance?.id}"> ${g.message(code:'default.search.within.this.group')} </label>

            </div>
        </div>




        <g:each in="${modules}" var="module">
        <g:if test="${!module.name.equalsIgnoreCase('All')}">
        <div class="aq_modules ${module.name.toLowerCase()}_aq_filters ${activeFilters && activeFilters['aq.object_type']?.equalsIgnoreCase(module.name)?'':'hide' }">
            <br>
            <b>${module.displayName} specific search options</b>
            <br>
            <g:render template="/${module.template}/advSearchTemplate"/>
        </div>
        </g:if>
        </g:each>
    
        <g:render template="/search/advSearchCommonFooterOptionsTemplate"/>

    <div class="form-action">
        <button id="advSearch"
            class="btn btn-primary pull-right" style="margin-top:10px;">${g.message(code:"default.search")}</button>
    </div>
    </form>

    <div class="clearfix"></div>

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
            parentEl:$("#uploadedOnDatePicker")
        }, 
        function(start, end, label) {
            $('#uploadedOn span.date').html(start.format('DD/MM/YYYY') + ' - ' + end.format('DD/MM/YYYY'));
        }
    );

    $('#uploadedOn span.date').html(startDate.toString('dd/MM/yyyy') + ' - ' + endDate.toString('dd/MM/yyyy'));

    $('#uploadedOn').on('apply.daterangepicker', function(ev, picker) {
        console.log(picker.startDate.format('YYYY-MM-DD'));
        console.log(picker.endDate.format('YYYY-MM-DD'));
        ev.stopPropagation();
        ev.preventDefault();
    });

    $('#uploadedOn').on('hide.daterangepicker', function(ev, picker) {
        console.log(picker.startDate.format('YYYY-MM-DD'));
        console.log(picker.endDate.format('YYYY-MM-DD'));
        ev.stopPropagation();
        ev.preventDefault();
    });


    $('#advSearchForm :input:not(input[type=hidden])').each(function(index, ele) {
        var field = $(this).attr('name');
        if(field == 'aq.name') {
        $('#aq.name').autofillNames({
        });
 
        } else {
        $(this).typeahead({
            source: function (query, process) {
                return $.get("${uGroup.createLink(action:'terms', controller:'observation') }"+'?field='+field, { term: query }, function (data) {
                    return process(data);
                });
            }
        });
        }
    });

    $("#advSearch").click(function() {
    console.log('advSearch click submit');
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
        $('select[name="aq.license"],select[name="aq.type"]').val('').parent().parent().show();
        $('select.multiselect').multiselect('deselectAll',false).multiselect('updateButtonText');
        hideAqSearchControls(val);
    });

    function hideAqSearchControls(val) {

        if(val == 'Observation') {
            $('input[name="aq.attribution"]').val('').parent().parent().hide();
        } else if (val == 'SUser') {
            $('input[name="aq.name"],input[name="aq.contributor"],input[name="aq.attribution"],input[name="aq.members"],input[name="aq.tag"]').val('').parent().parent().hide();
            $('select.multiselect').multiselect('deselectAll',false).multiselect('updateButtonText').parent().parent().hide()
        } else if (val == 'UserGroup') {
            $('input[name="aq.name"],input[name="aq.contributor"],input[name="aq.attribution"],input[name="aq.license"],input[name="aq.text"],input[name="aq.tag"],input[name="aq.uGroup"]').val('').parent().parent().hide();
            $('select.multiselect').multiselect('deselectAll',false).multiselect('updateButtonText').parent().parent().hide()
        }

        $('.'+val.toLowerCase()+'_aq_filters').show()
    }

    hideAqSearchControls($('select.moduleFilter').val());

    var licenses = "${queryParams?queryParams['aq.license']:''}".split(/AND|OR/);
    $.each(licenses, function(index, value) {
        $('select.multiselect.licenseFilter option[value="'+value.trim()+'"]').attr("selected",true);
    });

    var types = "${queryParams?queryParams['aq.type']:''}".split(/AND|OR/);
    $.each(types, function(index, value) {
        $('select.multiselect.typeFilter option[value="'+value.trim()+'"]').attr("selected",true);
    });
    $('select.multiselect').multiselect({
        buttonWidth:'100%'
    });

    $('.daterangepicker').parent().width('230%');
    $('button.multiselect').css('text-align','left'); 

    $('#advSearchDropdownA').on('click', function (event) {
        $(this).parent().toggleClass("open");
    });

    $('body').on('click', function (e) {
        if (!$('#advSearchDropdown').is(e.target) && $('#advSearchDropdown').has(e.target).length === 0 && $('.open').has(e.target).length === 0) {
            $('#advSearchDropdown').removeClass('open');
        }
    });
});
</r:script>
