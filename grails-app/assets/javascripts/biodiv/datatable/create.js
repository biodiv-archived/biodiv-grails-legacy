var dataTable_contributor_autofillUsersComp;
function onDataTableClick(event, dataTableTypeId, datasetId, dataTableId) {
    event.preventDefault();
    if(CKEDITOR.instances.description)  CKEDITOR.instances.description.destroy();
    console.log(dataTableTypeId);
    if(dataTableTypeId != "null") {
        $.ajax({
            url:'/dataset/dataTableTypeChanged',
            type:'POST',
            data:{'dataTableTypeId':dataTableTypeId,'datasetId':datasetId, dataTableId:dataTableId}, 
            success:function(data,textStatus){
                $('#addDataTable').html(data);
                initObservationCreate();
                intializesSpeciesHabitatInterest();
                initLocationPicker();
                dataTable_contributor_autofillUsersComp = $("#userAndEmailList_contributor_id").autofillUsers({
                    usersUrl : window.params.userTermsUrl
                });
                CKEDITOR.replace('description', config);
                showSampleDataTable();
                var contributorId = $('#contributorUserIds').data('contributorid'); 
                var contributorName = $('#contributorUserIds').data('contributorname'); 
                if(contributorId != '' )
                    dataTable_contributor_autofillUsersComp[0].addUserId({'item':{'userId':contributorId, 'value':contributorName}});
                loadSpeciesGroupTraits();
            },
            error:function(XMLHttpRequest,textStatus,errorThrown){
                console.log('error onDataTableClick');
            }
        });
    }
    return false;
}

function loadSpeciesGroupTraits() {
    var params = {'isObservationTrait':true, 'displayAny':false, 'max':-1, 'offset':0, 'format':'json'};
    params['sGroup'] = getSelectedGroup(); 
    if(params['sGroup']) {
        $.ajax({
            url:window.params.trait.listUrl,
            method:'GET',
            data:params,
            type:'json',
            success: function(data) {   
                $('#speciesGroupTraits').data('speciesGroupTraitsList', data.model.instanceList);
                showSampleDataTable();
            }
        });
    }
    return false;
}

function showSampleDataTable(){
    var input = $("#dataTableFile_path").val();
    var res = "dataTable";
    if(input) {
        parseData(  window.params.content.url + input , {callBack:loadSampleData, res: res});
    }
}

function loadSampleData(data, columns, res, sciNameColumn, commonNameColumn) {

    var cols = '', d = '';

    var colStr = '';
    var el = "<table class='table table-striped table-bordered'><thead><tr>";
    $.each(columns, function(i, n){
        el += "<th>"+n.name+"</th>";
        colStr += n.name+',';
    });
    el += "</tr><tr>"
        $("#columns").val(colStr);
    var speciesGroupTraitsList = $('#speciesGroupTraits').data('speciesGroupTraitsList');
    if(speciesGroupTraitsList === undefined) {
        //alert("Please click a species group to show respective traits");
    } 
    $.each(columns, function(i, n){
        //<div class='btn-group'><a class='btn dropdown-toggle' data-toggle='dropdown' href='#'>Select mapping <span class='caret'></span></a>"
        el += "<th><select class='mapColumns' multiple name='attribute."+n.name+"'>";
        el += "<optgroup label='General'>";
        el += "<option class='generalColumn' value='sciNameColumn'>Scientific Name</option>"; 
        el += "<option class='generalColumn' value='commonNameColumn'>Common Name</option>"; 
        el += "<option class='generalColumn' value='language'>Common Name Language</option>"; 
        el += "<option class='generalColumn' value='observed on'>Date</option>"; 
        el += "<option class='generalColumn' value='date accuracy'>Date Accuracy</option>"; 
        el += "<option class='generalColumn' value='group'>Species Group</option></optgroup>"; 
        el += "<option class='generalColumn' value='habitat'>Habitat</option></optgroup>"; 
        el += "<option class='generalColumn' value='location title'>Place Name</option>"; 
        el += "<option class='generalColumn' value='latitude'>Latitude</option>"; 
        el += "<option class='generalColumn' value='longitude'>Longitude</option></optgroup>"; 
        el += "<option class='generalColumn' value='location scale'>Location Scale</option></optgroup>"; 
        el += "<option class='generalColumn' value='geoprivacy'>Geo-privacy</option></optgroup>"; 
        el += "<option class='generalColumn' value='license'>License</option></optgroup>"; 
        el += "<option class='generalColumn' value='user email'>Contributor</option></optgroup>"; 
        el += "<option class='generalColumn' value='attribution'>Attribution</option></optgroup>"; 
        el += "<option class='generalColumn' value='filename'>Filenames (CSV)</option></optgroup>"; 
        el += "<option class='generalColumn' value='notes'>Notes</option></optgroup>"; 
        el += "<option class='generalColumn' value='tags'>Tags</option></optgroup>"; 
        el += "<option class='generalColumn' value='help identify?'>Help Identify?</option></optgroup>"; 
        el += "<option class='generalColumn' value='post to user groups'>Post to User Groups</option></optgroup>"; 
        el += "<option class='generalColumn' value='comment'>Comment</option></optgroup>"; 

        el += "<optgroup label='Traits'>"
            var speciesGroupTraitsList = $('#speciesGroupTraits').data('speciesGroupTraitsList');
        if(speciesGroupTraitsList === undefined) {
            //    alert("Please click a species group to show respective traits");
        } else {
            $.each(speciesGroupTraitsList, function(index, val) {
                el += "<option class='traitColumn' value='trait."+val.id+"'>"+val.name+"</option>"; 
            });
        }
        //el += "</optgroup><optgroup label='Custom Fields'></optgroup>"
        el += "</select>";
        el += "</th>";
    });
    el += "</tr></thead><tbody><tr>";
    $.each(columns, function(i, n){
        el += "<td>"+data[0][n.name]+"</td>";
    });
    el += "</tr><tr>"
        $.each(columns, function(i, n){
            el += "<td>"+data[1][n.name]+"</td>";
        });
    el += "</tr>";

    el += "</tbody></table>";
    $("#myGrid").html(el);
    $("#gridSection").show();
    $('.mapColumns').multiselect({
        nonSelectedText: "Mark Columns",
        maxHeight:300,
        buttonWidth:200,
        enableCaseInsensitiveFiltering: false,
        onChange: function(option, checked, select) {
            var values = [];
            option.parent().parent().find('option').each(function() {
                if ($(this).val() !== option.val()) {
                    values.push($(this).val());
                }
            });                                                                                             
            option.parent().parent().multiselect('deselect', values);
            $('.mapColumns').next().removeClass('open');
        }
    });

    $("#restOfForm").show();
}

function getMarkedColumns() {
    var selectedOptions = $('.mapColumns option:selected');
    return selectedOptions;
}

function viewSpeciesGrid() {
    var input = $("#dataTableFile_path").val();
    var res = "species";
    if(input){
        parseData(  window.params.content.url + input , {callBack:loadSpeciesDataToGrid, res: res });
    }
}

function getUploadParams() {

    var speciesGroups = getSelectedGroupArr();
    $(".addDataTable input.group").remove();
    $.each(speciesGroups, function(index, element){
        var input = $("<input>").attr("type", "hidden").attr("name", "group."+index).attr('class','group').val(element);
        $(".addDataTable").append($(input));	
    })

    var locationpicker = $(".map_class").data('locationpicker'); 
    if(locationpicker && locationpicker.mapLocationPicker.drawnItems) {
        var areas = locationpicker.mapLocationPicker.drawnItems.getLayers();
        if(areas.length > 0) {
            var wkt = new Wkt.Wkt();
            wkt.fromObject(areas[0]);
            $("input.areas").val(wkt.write());
        }
    }

    //checklist related data
    if(grid){
        $("#dataTableFilePath").val($("#dataTableFile_path").val());
        var markedColumns = getMarkedColumns();
        if(markedColumns.length > 0) {
            $("#dataTableColumns").val(JSON.stringify(markedColumns));
        }
    }

    for ( instance in CKEDITOR.instances ) {
        CKEDITOR.instances[instance].updateElement();
    }

    if(dataTable_contributor_autofillUsersComp.length > 0) {
        $('input[name="contributorUserIds"]').val(dataTable_contributor_autofillUsersComp[0].getEmailAndIdsList().join(","));
    }

        //if species type 
    var params = {};
    if($('#dataTableType').val() == 1) {
        params = getSpeciesUploadParams();
    } 
    var xlsxFileUrl = $('#xlsxFileUrl').val();
    params['xlsxFileUrl'] = xlsxFileUrl;

    return params;
}

function getSpeciesUploadParams() {
    getTagsForHeaders();
    var hm = getHeaderMetadata();
    delete hm["undefined"];
    var orderedArray = $('#columnOrder').val();
    orderedArray = JSON.stringify(orderedArray);
    var headerMarkers = JSON.stringify(hm);

    var params = {};
    params['headerMarkers'] = headerMarkers;
    params['orderedArray'] = orderedArray;
    //params['imagesDir'] = $("#imagesDir").val();
    params['writeContributor'] = 'true';
    return params;
}

$(document).ready(function() {	

    $(document).on('click', '.addDataTable #speciesGroupFilter button', function(e){
        console.log('speciesGroupFilter button click');
        e.preventDefault();
        loadSpeciesGroupTraits();
        showSampleDataTable();
        return false;
    });

    $(document).on('click', "#createDataTableSubmit", function(){
        if($(this).hasClass('disabled')) {
            alert(window.i8ln.observation.bulkObvCreate.up);
            event.preventDefault();
            return false; 		 		
        }

        if (document.getElementById('agreeTerms').checked) {
            //$(this).addClass("disabled");
            $(".addDataTable").ajaxSubmit({ 
                dataType: 'json',
                data: getUploadParams(),
                beforeSubmit: function(arr, $form, options) { 
                },
                success: function(data, statusText, xhr) {
                    console.log(data);
                    $(".addDataTable").find(".control-group").removeClass("error");
                    $(".addDataTable").find('.help-inline').html('');

                    if(data.success) {
                        $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
                        //TODO:show dataTable snippet and remove form
                        //redirect to show page
                        window.location.href = data.url;
                    } else {
                        window.scrollTo(0, 0);
                        var errorMsg = data.msg+'<br/>';
                        $.each(data.errors, function(index, value) {
                 //           errorMsg += value.message+'<br/>';
                            if(value.field == 'party.contributorId') value.field = 'contributorUserIds';
                            if(value.field == 'party.attributions') value.field = 'attributions';
                            if(value.field == 'temporalCoverage') value.field = 'fromDate';
                            if(value.field == 'geographicalCoverage') value.field = 'placeName';
                            if(value.field == 'taxonomicCoverage') {
                                 $(".addDataTable").find('#speciesGroupFilter').parents(".control-group").addClass("error");
                                $(".addDataTable").find('#speciesGroupFilter').nextAll('.help-inline').html("<li>"+value.message+"</li>")
                            } else if(value.field == 'uFile'){
                                $(".addDataTable").find('#au-dataTableFile_uploader').parents(".control-group").addClass("error");
                                $(".addDataTable").find('#au-dataTableFile_uploader').nextAll('.help-inline').html("<li>"+value.message+"</li>")
                            } else {
                                $(".addDataTable").find('[name='+value.field+']').parents(".control-group").addClass("error");
                                $(".addDataTable").find('[name='+value.field+']').nextAll('.help-inline').html("<li>"+value.message+"</li>")
                            }
                        });
                        $(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(errorMsg);
                    }    
                }, error:function (xhr, ajaxOptions, thrownError){
                    //successHandler is used when ajax login succedes
                    var successHandler = this.success;
                    handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
                        var response = $.parseJSON(xhr.responseText);
                        console.log(response);
                    });
                } 
            });	

            return false;
        } else {
            alert(window.i8ln.observation.bulkObvCreate.agree) 
        }

        return false;
    });

});
