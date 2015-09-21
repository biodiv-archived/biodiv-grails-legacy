var curators_autofillUsersComp;
var contributors_autofillUsersComp;
var taxon_curators_autofillUsersComp;
var taxon_editors_autofillUsersComp;

function onSpeciesImageUploadSuccess(type){
    $("body").css("cursor", "progress");
    var msgText
    if(type == "imageUpload"){
        msgText = window.i8ln.species.speciesPermission.ius
        showUpdateStatus(msgText, 'success',$("#speciesImage-tab1") );
    }
    else if(type == "pulledSpeciesFieldImage"){
        msgText = window.i8ln.species.speciesPermission.pul
        showUpdateStatus(msgText, 'success',$("#speciesImage-tab2") );
    }
    else{
        msgText = window.i8ln.species.speciesPermission.pul
        $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(msgText);
        $('html, body').animate({
            scrollTop: 0
    }, 'slow');
    }
    setTimeout(function(){location.reload();}, 2000);
    $("body").css("cursor", "default");
    return true;
}

function getNextRelatedObvImages(speciesId, url, resourceListType, context){
    var imagesListWrapper = $(context).closest(".imagesListWrapper");
    var offset = imagesListWrapper.find(".relatedImagesOffset").val();
    $.ajax({
        url: url,
        dataType: "json",
        data: {speciesId:speciesId, offset: offset ,resourceListType: resourceListType},	
        success: function(data) {
            var addPhotoHtmlData = $(data.addPhotoHtml);
            if(data.relatedObvCount == 0){
                $(context).replaceWith('<a class="btn disabled" style="margin-right: 5px;">No More</a>');
                $(context).remove();
                return;
            } 
            imagesListWrapper.find(".imagesList" ).append(addPhotoHtmlData);
            imagesListWrapper.find(".relatedImagesOffset").val(parseInt(offset) + parseInt(data.relatedObvCount));
            imagesListWrapper.append($(context));
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}


$(document).ready(function() {
    $('#inviteCurators, #inviteContributors, #inviteTaxonCurators, #requestPermission').click(function(e){
        var invitetype = $(this).data('invitetype');
        var $dialog, $autofillUsers;
        if(invitetype === 'curator') {
            $dialog = $('#inviteCuratorsDialog');
            $autofillUsers = curators_autofillUsersComp;
        } else if(invitetype === 'contributor') {
            $dialog = $('#inviteContributorsDialog');
            $autofillUsers = contributors_autofillUsersComp;
        } else if(invitetype === 'taxon_curator') {
            $dialog = $('#inviteTaxonCuratorsDialog');
            $autofillUsers = taxon_curators_autofillUsersComp;
        } else if(invitetype === 'taxon_editor') {
            $dialog = $('#inviteTaxonEditorsDialog');
            $autofillUsers = taxon_editors_autofillUsersComp;
        } else if(invitetype === 'requestPermission') {
            $dialog = $('#requestPermissionDialog');
        }

        $.ajax({ 
            url:window.params.isLoggedInUrl,
            success: function(data, statusText, xhr) {
                if(data === "true") {
                    $dialog.find('input[name="userIds"]').val('');
                    $('#userAndEmailList_'+invitetype).val('');
                    if($autofillUsers) {
                        $($autofillUsers[0]).parent().children('li').each(function(){
                            $autofillUsers[0].removeChoice($(this).find('span')[0]);
                        });
                    }
                    $dialog.find(".inviteMsg_status").removeClass('alert alert-success alert-error').html('');

                    $dialog.find('form')[0].reset();

                    $('#inviteCuratorsDialog, #inviteContributorsDialog, #requestPermissionDialog').modal({
                        "show" : false,
                        "backdrop" : "static"
                    });

                    $dialog.modal('show');
                    return false;
                } else {
                    $dialog.modal('hide');
                    window.location.href = window.params.loginUrl+"?spring-security-redirect="+window.location.href;
                }
            },
            error:function (xhr, ajaxOptions, thrownError){
                return false;
            } 
        });
    });

    $(".inviteButton").click(function(){
        var $dialog = $(this).parent().parent();
        var selectedNodes = $.map($('#taxonHierarchy').jstree(true).get_checked(true), function(val) { return val.original.taxonid; }).join()
        console.log(selectedNodes);
        //$(".taxDefIdCheck:checked").map(function() {return $(this).parent("span").find(".taxDefIdVal").val();}).get().join();
        var invitetype = $dialog.find('input[name="invitetype"]').val();

        var $autofillUsers;
        if(invitetype === 'curator') {
            $autofillUsers = curators_autofillUsersComp[0]
        } else if(invitetype === 'contributor') {
            $autofillUsers = contributors_autofillUsersComp[0]
        } else if(invitetype === 'taxon_curator') {
            $autofillUsers = taxon_curators_autofillUsersComp[0]
        } else if(invitetype === 'taxon_editor') {
            $autofillUsers = taxon_editors_autofillUsersComp[0]
        }else {
            $autofillUsers = contributors_autofillUsersComp[0]
        }
        $dialog.find('input[name="userIds"]').val($autofillUsers.getEmailAndIdsList().join(","));

        var data = {message:$dialog.find('.inviteMsg').val(), selectedNodes : selectedNodes}
        $dialog.find('form').ajaxSubmit({ 
            url: window.params.inviteFormUrl,
            dataType: 'json', 
            clearForm: true,
            resetForm: true,
            type: 'POST',
            data:data,
            success: function(data, statusText, xhr) {
                if(data.statusComplete) {
                    $dialog.modal('hide');
                    $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
                } else {
                    $dialog.find(".inviteMsg_status").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg).show();
                }    
                $dialog.find('form')[0].reset();
            }, error:function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                var successHandler = this.success;
                handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
                    var response = $.parseJSON(xhr.responseText);

                });
                //$dialog.find('form')[0].reset()
            } 
        });	
    });

    $(".requestButton").click(function(){
        var $dialog = $(this).parent().parent();
//        var selectedNodes = $(".taxDefIdCheck:checked").map(function() {return $(this).parent("span").find(".taxDefIdVal").val();}).get().join();
        var selectedNodes = $.map($('#taxonHierarchy').jstree(true).get_checked(true), function(val) { return val.original.taxonid; }).join()
        var invitetype = $dialog.find('input[name="invitetype"]').val();

        var data = {message:$dialog.find('.inviteMsg').val(), selectedNodes : selectedNodes}
        $dialog.find('form').ajaxSubmit({ 
            url: window.params.requestPermissionFormUrl,
            dataType: 'json', 
            clearForm: true,
            resetForm: true,
            type: 'POST',
            data:data,
            success: function(data, statusText, xhr) {
                if(data.statusComplete) {
                    $dialog.modal('hide');
                    $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
                } else {
                    $dialog.find(".inviteMsg_status").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg).show();
                }    
                $dialog.find('form')[0].reset();
            }, error:function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                var successHandler = this.success;
                handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
                    var response = $.parseJSON(xhr.responseText);
                    //$dialog.find('form')[0].reset()
                });
            } 
        });	
    });


    $("#addSpeciesImagesBtn").click(function(){
        uploadResource = new $.fn.components.UploadResource($('#speciesImage-tab1'));
        uploadResource.POLICY = $("input[name='policy']").val();
        uploadResource.SIGNATURE = $("input[name='signature']").val();
        $(".speciesImage-wrapper").toggle();
        $('html, body').animate({
            scrollTop: $(".speciesImage-wrapper").offset().top
        }, 1000);
    });
    
    $("#uploadSpeciesImagesBtn").click(function(){
        $("#uploadSpeciesImagesForm").ajaxSubmit({success:onSpeciesImageUploadSuccess("imageUpload")});
        return false;
    });

    $("#pullObvImagesBtn").click(function(){
        $("#pullObvImagesForm").ajaxSubmit({success:onSpeciesImageUploadSuccess("pulledImage")});
        return false;
    });

    $("#pullSpeciesFieldImagesBtn").click(function(){
        $("#pullSpeciesFieldImagesForm").ajaxSubmit({success:onSpeciesImageUploadSuccess("pulledSpeciesFieldImage")});
        return false;
    });
    
    $("#pullObvImagesSpFieldBtn").click(function(){
        $("#pullObvImagesSpFieldForm").ajaxSubmit({success:onSpeciesImageUploadSuccess("pulledImageInSpField")});
    });

    $("#uploadSpeciesFieldImagesBtn").click(function(){
        $("#uploadSpeciesFieldImagesForm").ajaxSubmit({success:onSpeciesImageUploadSuccess("imageUploadInSpField")});
        return false;
    });

});



function getSpeciesFieldMedia(spId, spFieldId, resourceListType, url){
    $.ajax({
        url: url,
        dataType: "json",
        data: {speciesId:spId, spFieldId: spFieldId,resourceListType: resourceListType},	
        success: function(data) {
            if(data.statusComplete){
                var addPhotoHtmlData = $(data.addPhotoHtml);
                $("#speciesFieldImage-tab1 .imagesList .addedResource").remove();
                $("#speciesFieldImage-tab1 .imagesList").append(addPhotoHtmlData);
                $("#addSpFieldResourcesModal").modal("toggle");
                $("#addSpFieldResourcesModal").data("spfieldid", spFieldId);
                uploadResource = new $.fn.components.UploadResource($('#speciesFieldImage-tab1'));
                uploadResource.POLICY = $("input[name='policy']").val();
                uploadResource.SIGNATURE = $("input[name='signature']").val();
                $("input[name='speciesFieldId']").val(spFieldId);
            }
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}

$("#addSpFieldResourcesModalSubmit").click(function(){
    var spfid = $("#addSpFieldResourcesModal").data("spfieldid");
    $("#addSpFieldResourcesModal").modal("toggle");
    $(".speciesField[data-pk='"+spfid+"']").find(".editable-submit").trigger("click");
    return;
});
