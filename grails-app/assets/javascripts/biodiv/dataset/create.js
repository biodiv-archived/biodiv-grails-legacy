var dataset_contributor_autofillUsersComp;
function dataPackageChangedForDataset(event, dataPackageId) {
    console.log(dataPackageId);
    event.preventDefault();
    if(CKEDITOR.instances.description)  CKEDITOR.instances.description.destroy();
    if(dataPackageId != "null") {
        $.ajax({
            url:'/dataset/dataPackageChangedForDataset',
            type:'POST',
            data:'dataPackageId='+dataPackageId, 
            success:function(data,textStatus){
                $('#datasetEditSection').html(data);
                initObservationCreate();
                intializesSpeciesHabitatInterest();
                initLocationPicker();
                var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic' ]]};
                CKEDITOR.replace('description', config);
                dataset_contributor_autofillUsersComp = $("#userAndEmailList_contributor_id").autofillUsers({
                    usersUrl : window.params.userTermsUrl
                });
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
                    $(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg+"<br/>"+data.errors);
                    $.each(data.errors, function(index, value) {
                            $("#createDataset").find('[name='+value.field+']').parents(".control-group").addClass("error");
                            $("#createDataset").find('[name='+value.field+']').nextAll('.help-inline').append("<li>"+value.message+"</li>")
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
    });

});
