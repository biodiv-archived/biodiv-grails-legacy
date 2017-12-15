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
        alert("Please click a species group to show respective traits");
    } 
    $.each(columns, function(i, n){
        //<div class='btn-group'><a class='btn dropdown-toggle' data-toggle='dropdown' href='#'>Select mapping <span class='caret'></span></a>"
        el += "<th><select class='mapColumns' multiple name='attribute."+n.name+"'>";
        el += "<optgroup label='General'>"
            el += "<option class='generalColumn' value='sciNameColumn'>Scientific Name</option>"; 
        el += "<option class='generalColumn' value='commonNameColumn'>Common Name</option>"; 
        el += "<option class='generalColumn' value='obvDate'>Date</option>"; 
        el += "<option class='generalColumn' value='placeName'>Place Name</option>"; 
        el += "<option class='generalColumn' value='latitude'>Latitude</option>"; 
        el += "<option class='generalColumn' value='longitude'>Longitude</option></optgroup>"; 
        el += "<option class='generalColumn' value='license'>License</option></optgroup>"; 
        el += "<option class='generalColumn' value='user email'>Contributor</option></optgroup>"; 
        el += "<option class='generalColumn' value='attribution'>Attribution</option></optgroup>"; 

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
        nonSelectedText: "Select Mapping",
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
    console.log(selectedOptions);
    $
        return selectedOptions;
}

$(document).ready(function() {	

    $(document).on('click', '#addDataTable #speciesGroupFilter button', function(e){
        console.log('speciesGroupFilter button click');
        e.preventDefault();
        loadSpeciesGroupTraits();
        showSampleDataTable();
        return false;
    });

    $(document).on('click', "#createDataTableSubmit", function(){
        /*if($(this).hasClass('disabled')) {
          alert(window.i8ln.observation.bulkObvCreate.up);
          event.preventDefault();
          return false; 		 		
          }*/

        if (document.getElementById('agreeTerms').checked) {
            //$(this).addClass("disabled");

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
                $("#dataTableColumns").val(JSON.stringify(getMarkedColumns()));
            }

            for ( instance in CKEDITOR.instances ) {
                CKEDITOR.instances[instance].updateElement();
            }

            if(dataTable_contributor_autofillUsersComp.length > 0) {
                $('input[name="contributorUserIds"]').val(dataTable_contributor_autofillUsersComp[0].getEmailAndIdsList().join(","));
            }
            $(".addDataTable").ajaxSubmit({ 
                dataType: 'json', 
                beforeSubmit: function(arr, $form, options) { 
                    // The array of form data takes the following form: 
                    // [ { name: 'username', value: 'jresig' }, { name: 'password', value: 'secret' } ] 
                    /*$('#addDataTable').hide();
                      var str = "<div id='' class='dataTable sidebar_section observation_story'><h5>Uploading dataTable : </h5>";
                      $.each(arr, function(index, p) {
                      str += "<div class='prop'><span class='name'>"+p.name+"</span><div class='value'>"+p.value+"</div></div>";
                      });
                      $('#workspace').prepend(str+"</div>");
                      return true;*/
                },
                success: function(data, statusText, xhr) {
                    console.log(data);
                    if(data.success) {
                        $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
                        //TODO:show dataTable snippet and remove form
                        //redirect to show page
                        window.location.href = data.url;
                    } else {
                        window.scrollTo(0, 0);
                        $(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
                        $.each(data.errors, function(index, value) {
                            $(".addDataTable").find('[name='+value.field+']').parents(".control-group").addClass("error");
                            $(".addDataTable").find('[name='+value.field+']').nextAll('.help-inline').append("<li>"+value.message+"</li>")
                        });
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


