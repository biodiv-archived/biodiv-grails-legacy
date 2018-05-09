var dataTable_contributor_autofillUsersComp;
function onDataTableClick(event, dataTableTypeId, datasetId, dataTableId, webaddress) {
    event.preventDefault();
    if(CKEDITOR.instances.summary)  CKEDITOR.instances.summary.destroy();
    if(CKEDITOR.instances.description)  CKEDITOR.instances.description.destroy();
    console.log(dataTableTypeId);
    if(dataTableTypeId != "null") {
        $.ajax({
            url:'/dataset/dataTableTypeChanged',
            type:'POST',
            data:{'dataTableTypeId':dataTableTypeId,'datasetId':datasetId, dataTableId:dataTableId, webaddress:webaddress}, 
            success:function(data,textStatus){
                if(data.success) {
                    $('#addDataTable').html(data.model.tmpl);

                    initObservationCreate();
                    intializesSpeciesHabitatInterest();
                    initLocationPicker();

                    dataTable_contributor_autofillUsersComp = $("#userAndEmailList_contributor_id").autofillUsers({
                        usersUrl : window.params.userTermsUrl
                    });
                    CKEDITOR.replace('summary', config);
                    CKEDITOR.replace('description', descriptionConfig);

                    showSampleDataTable();

                    var contributorId = $('#contributorUserIds').data('contributorid'); 
                    var contributorName = $('#contributorUserIds').data('contributorname'); 
                    if(contributorId != ''  && dataTable_contributor_autofillUsersComp[0])
                        dataTable_contributor_autofillUsersComp[0].addUserId({'item':{'userId':contributorId, 'value':contributorName}});

                    $('.addDataTable #speciesGroupFilter button').click(function(e){
                        console.log('speciesGroupFilter button click');
                        e.preventDefault();
                        loadSpeciesGroupTraits();
                        loadCustomFieldList();
                        showSampleDataTable();
                        return false;
                    });

                    loadSpeciesGroupTraits();
                    loadCustomFieldList();
                }  else {
                    $('#addDataTable').html(data.msg);
                }

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

function loadCustomFieldList() {
}

function showSampleDataTable(){
    var input = $("#dataTableFile_path").val();
    var res = "dataTable";
    var headerMetadata = getHeaderMetadata();
    if(headerMetadata) {
        viewSpeciesGrid();
    } else {
        if(input) {
            $('#createDataTableSubmit').removeAttr('disabled');
            console.log($('#xlsxFileUrl:first').val());
            if($('#xlsxFileUrl:first').val() && !$('#xlsxFileUrl:first').val().endsWith(input) )  {
                parseData(  window.params.content.url + input , {callBack:loadSampleData, res: res});
            }
        }
    }
}

function loadSampleData(data, columns, res, sciNameColumn, commonNameColumn) {
console.log('loadSampleData');
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
    if($('#customFields').val()) {
        var customFieldList = JSON.parse($('#customFields').val());//data('customFieldList');
        if(customFieldList === undefined) {
            //alert("Please click a species group to show respective traits");
        }
    }


    var mappedColumns;
    if($('#mappedColumns').val()) {
        mappedColumns = JSON.parse($('#mappedColumns').val());
    }
    $.each(columns, function(i, n) {
        var mappedColumn = getMappedColumn(n.name, mappedColumns);

        console.log(n.name+"   "+mappedColumn);

       //<div class='btn-group'><a class='btn dropdown-toggle' data-toggle='dropdown' href='#'>Select mapping <span class='caret'></span></a>"
        el += "<th><select class='mapColumns' multiple name='attribute."+n.name+"'>";
        el += "<optgroup label='General'>";
        el += "<option class='generalColumn' value='sciNameColumn' "+((mappedColumn[1] == 'sciNameColumn')?'selected':'')+">Scientific Name</option>"; 
        el += "<option class='generalColumn' value='commonNameColumn' "+((mappedColumn[1] == 'commonNameColumn')?'selected':'')+">Common Name</option>"; 
        el += "<option class='generalColumn' value='language' "+((mappedColumn[1] == 'language')?'selected':'')+">Common Name Language</option>"; 
        el += "<option class='generalColumn' value='observed on' "+((mappedColumn[1] == 'observed on')?'selected':'')+">Date</option>"; 
        el += "<option class='generalColumn' value='date accuracy' "+((mappedColumn[1] == 'date accuracy')?'selected':'')+">Date Accuracy</option>"; 
        el += "<option class='generalColumn' value='group' "+((mappedColumn[1] == 'group')?'selected':'')+">Species Group</option></optgroup>"; 
        el += "<option class='generalColumn' value='habitat' "+((mappedColumn[1] == 'habitat')?'selected':'')+">Habitat</option></optgroup>"; 
        el += "<option class='generalColumn' value='location title' "+((mappedColumn[1] == 'location title')?'selected':'')+">Place Name</option>"; 
        el += "<option class='generalColumn' value='latitude' "+((mappedColumn[1] == 'latitude')?'selected':'')+">Latitude</option>"; 
        el += "<option class='generalColumn' value='longitude' "+((mappedColumn[1] == 'longitude')?'selected':'')+">Longitude</option></optgroup>"; 
        el += "<option class='generalColumn' value='location scale' "+((mappedColumn[1] == 'location scale')?'selected':'')+">Location Scale</option></optgroup>"; 
        el += "<option class='generalColumn' value='geoprivacy' "+((mappedColumn[1] == 'geoprivacy')?'selected':'')+">Geo-privacy</option></optgroup>"; 
        el += "<option class='generalColumn' value='license' "+((mappedColumn[1] == 'license')?'selected':'')+">License</option></optgroup>"; 
        el += "<option class='generalColumn' value='user email' "+((mappedColumn[1] == 'user email')?'selected':'')+">Contributor</option></optgroup>"; 
        el += "<option class='generalColumn' value='attribution' "+((mappedColumn[1] == 'attribution')?'selected':'')+">Attribution</option></optgroup>"; 
        el += "<option class='generalColumn' value='filename' "+((mappedColumn[1] == 'filename')?'selected':'')+">Filenames (CSV)</option></optgroup>"; 
        el += "<option class='generalColumn' value='notes' "+((mappedColumn[1] == 'notes')?'selected':'')+">Notes</option></optgroup>"; 
        //el += "<option class='generalColumn' value='tags' "+((mappedColumn[1] == 'tags')?'selected':'')+">Tags</option></optgroup>"; 
        el += "<option class='generalColumn' value='help identify?' "+((mappedColumn[1] == 'help identify?')?'selected':'')+">Help Identify?</option></optgroup>"; 
        el += "<option class='generalColumn' value='post to user groups' "+((mappedColumn[1] == 'post to user groups')?'selected':'')+">Post to User Groups</option></optgroup>"; 
        el += "<option class='generalColumn' value='comment' "+((mappedColumn[1] == 'comment')?'selected':'')+">Comment</option></optgroup>"; 

        el += "<optgroup label='Traits'>";
        var speciesGroupTraitsList = $('#speciesGroupTraits').data('speciesGroupTraitsList');
        if(speciesGroupTraitsList === undefined) {
            //    alert("Please click a species group to show respective traits");
        } else {
            $.each(speciesGroupTraitsList, function(index, val) {
                el += "<option class='traitColumn' value='trait."+val.id+"' "+((mappedColumn[1] == 'trait.'+val.id)?'selected':'')+">"+val.name+"</option>"; 
            });
        }
        el += "</optgroup><optgroup label='Custom Fields'></optgroup>"
        $('#customFields').val()
        var customFieldList = $('#customFields').val() ? JSON.parse($('#customFields').val()) : undefined;//data('customFieldList');
        if(customFieldList === undefined) {
            //    alert("Please click a species group to show respective traits");
        } else {
            $.each(customFieldList, function(index, val) {
                el += "<option class='customFieldColumn' value='customfield."+val.id+"' "+((mappedColumn[1] == 'customfield.'+val.id)?'selected':'')+">"+val.name+"</option>"; 
            });
        }

        el += "</select>";
        el += "</th>";
    });
    el += "</tr><tr>";

    $.each(columns, function(i, n) {
        var mappedColumn = getMappedColumn(n.name, mappedColumns);
        console.log(mappedColumn);
        el += "<th><textarea class='descColumn' name='descColumn."+n.name+"' placeholder='Add column description' value='' style='min-width:inherit;max-width:inherit;'>"+(mappedColumn[3] ? mappedColumn[3] : '')+"</textarea></th>";
    });

    el += "</tr></thead><tbody><tr>";
    $.each(columns, function(i, n){
        el += "<td>"+data[0][n.name]+"</td>";
    });
    el += "</tr>";
    if(data[1]) {
        el += "<tr>";
        $.each(columns, function(i, n){
            el += "<td>"+data[1][n.name]+"</td>";
        });
        el += "</tr>";
    }

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
            var markedColumn = option.parent().parent().parent().index()+1; 
            if(checked) {
                $('#myGrid table tr th:nth-child('+markedColumn+')').css("background-color","beige");
                $('#myGrid table tr td:nth-child('+markedColumn+')').css("background-color","beige");
            } else {
                $('#myGrid table tr th:nth-child('+markedColumn+')').css("background-color","");
                $('#myGrid table tr td:nth-child('+markedColumn+')').css("background-color","");
            }
        }
    });

    $.each(columns, function(i, n) {
        var mappedColumn = getMappedColumn(n.name, mappedColumns);
 
        if(mappedColumn != '') {
            console.log('ddddddddddddddddddddddddddddddddddddddddddddddddd');
            $('#myGrid table tr th:nth-child('+(i+1)+')').css("background-color","beige");
            $('#myGrid table tr td:nth-child('+(i+1)+')').css("background-color","beige");
        }
    });

    $("#restOfForm").show();
}

function getMappedColumn(cName, mappedColumns) {
    if(!mappedColumns) return "";
    var mapped = false;
    var retMapping;
    for(var i=0; i< mappedColumns.length; i++) {
        retMapping = jQuery.extend({}, mappedColumns[i]);
        if(cName == mappedColumns[i][1]) {
            if(mappedColumns[i][0] == 'http://rs.tdwg.org/dwc/terms/scientificName') {mapped=true; retMapping[1] = 'sciNameColumn'}
            else if(mappedColumns[i][0] == 'http://rs.tdwg.org/dwc/terms/vernacularName') {mapped=true; retMapping[1] = 'commonNameColumn'}
            else if(mappedColumns[i][0] == 'http://rs.tdwg.org/dwc/terms/eventDate') {mapped=true; retMapping[1] = 'observed on'}
            else if(mappedColumns[i][0] == 'http://rs.tdwg.org/dwc/terms/locality') {mapped=true; retMapping[1] = 'location title'}
            else if(mappedColumns[i][0] == 'http://rs.tdwg.org/dwc/terms/decimalLatitude') {mapped=true; retMapping[1] = 'latitude'}
            else if(mappedColumns[i][0] == 'http://rs.tdwg.org/dwc/terms/decimalLongitude') {mapped=true; retMapping[1] = 'longitude'}
            else if(mappedColumns[i][0] == 'http://purl.org/dc/terms/contributor') {mapped=true; retMapping[1] = 'user email'}
            else if(mappedColumns[i][0] == 'http://purl.org/dc/terms/description') {mapped=true; retMapping[1] = 'notes'}
            else if(mappedColumns[i][0] == 'http://rs.tdwg.org/dwc/terms/habitat') {mapped=true; retMapping[1] = 'habitat'}
            else if(mappedColumns[i][0] == 'http://purl.org/dc/terms/rights') {mapped=true; retMapping[1] = 'license'}

            else if(mappedColumns[i][0].startsWith('http://ibp.org/terms/trait/')) {mapped=true; retMapping[1] = 'trait.'+mappedColumns[i][0].substring(mappedColumns[i][0].indexOf('trait/')+6)}
            else if(mappedColumns[i][0].startsWith('http://ibp.org/terms/customfield/')) {mapped=true; retMapping[1] = 'customfield.'+mappedColumns[i][0].substring(mappedColumns[i][0].indexOf('customfield/')+12)}
            else if(mappedColumns[i][0].startsWith('http://ibp.org/terms/observation/')) {mapped=true; retMapping[1] = mappedColumns[i][0].substring(mappedColumns[i][0].indexOf('observation/')+12)}
            else if(mappedColumns[i][0].startsWith('http://ibp.org/terms/observation/annotation/')) {mapped=true; retMapping[1] = mappedColumns[i][0].substring(mappedColumns[i][0].indexOf('annotation')+11)}
            if(mapped) return retMapping;
        }
    }
    return "";
}

function getMarkedColumns() {
    var selectedOptions = $('.mapColumns option:selected');
    return selectedOptions;
}

function viewSpeciesGrid() {
    //$("#myGrid").hide();
    $("#myGrid").empty();
    $("#tagHeaders tr:not(:first-child)").remove();
    var input = $("#dataTableFile_path").val();
    var res = "species";
    if(input){
        $('#createDataTableSubmit').removeAttr('disabled');
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
    var xlsxFileUrl = $('#xlsxFileUrl:first').val();
    params['xlsxFileUrl'] = xlsxFileUrl;

    return params;
}

function getSpeciesUploadParams() {
    getTagsForHeaders();
    var hm = getHeaderMetadata();
    delete hm["undefined"];
    var orderedArray = getColumnOrder();
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
