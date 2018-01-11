var dataset_contributor_autofillUsersComp;
function dataPackageChangedForDataset(event, dataPackageId) {
    console.log(dataPackageId);
    event.preventDefault();
    if(CKEDITOR.instances.summary)  CKEDITOR.instances.summary.destroy();
    if(CKEDITOR.instances.description)  CKEDITOR.instances.description.destroy();
    if(dataPackageId != "null") {
        $.ajax({
            url:'/dataset/dataPackageChangedForDataset',
            type:'POST',
            data:'dataPackageId='+dataPackageId, 
            success:function(data,textStatus){
                if(data.success) {
                    $('#datasetEditSection').html(data.model.tmpl);
                    initObservationCreate();
                    intializesSpeciesHabitatInterest();
                    initLocationPicker();
                    var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
                    CKEDITOR.replace('summary', config);
                    CKEDITOR.replace('description', descriptionConfig);
                    dataset_contributor_autofillUsersComp = $("#userAndEmailList_contributor_id").autofillUsers({
                        usersUrl : window.params.userTermsUrl
                    });
                } else {
                    $('#datasetEditSection').html(data.msg);
                }
            },
            error:function(XMLHttpRequest,textStatus,errorThrown){
                console.log('error onDatasetClick');
            }
        });
    }
}

$(document).ready(function() {	
    $(document).on('click', "#createDatasetSubmit",function(){

        var speciesGroups = getSelectedGroupArr();

        $("#createDataset input.group").remove();
        $.each(speciesGroups, function(index, element){
            var input = $("<input>").attr("type", "hidden").attr("name", "group."+index).attr('class','group').val(element);
            $("#createDataset").append($(input));	
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


        for ( instance in CKEDITOR.instances ) {
            CKEDITOR.instances[instance].updateElement();
        }

        if(dataset_contributor_autofillUsersComp.length > 0) {
            $('input[name="contributorUserIds"]').val(dataset_contributor_autofillUsersComp[0].getEmailAndIdsList().join(","));
        }

        $("#createDataset").ajaxSubmit({ 
            dataType: 'json', 
            success: function(data, statusText, xhr) {
                console.log(data);
                if(data.success) {
                    $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
                    window.location.href = data.url;
                    $(".datasetEditSection").hide();
                    $(".datasetShowSection").slideDown();
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
                                $("#createDataset").find('#speciesGroupFilter').parents(".control-group").addClass("error");
                                $("#createDataset").find('#speciesGroupFilter').nextAll('.help-inline').html("<li>"+value.message+"</li>")
                            } else if(value.field == 'uFile'){
                                $("#createDataset").find('#au-dataTableFile_uploader').parents(".control-group").addClass("error");
                                $("#createDataset").find('#au-dataTableFile_uploader').nextAll('.help-inline').html("<li>"+value.message+"</li>")
                            } else {
                                $("#createDataset").find('[name='+value.field+']').parents(".control-group").addClass("error");
                                $("#createDataset").find('[name='+value.field+']').nextAll('.help-inline').html("<li>"+value.message+"</li>")
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
    });

});
