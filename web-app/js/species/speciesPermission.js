var curators_autofillUsersComp;

$('#inviteCuratorsDialog').modal({
    "show" : false,
    "backdrop" : "static"
});

$('#inviteCurators').click(function(){
    $.ajax({ 
        url:window.params.isLoggedInUrl,
        success: function(data, statusText, xhr, form) {
        if(data === "true"){
            $('#curatorUserIds').val('');
            $('#userAndEmailList_3').val('');
            $(curators_autofillUsersComp[0]).parent().children('li').each(function(){
                $(curators_autofillUsersComp[0]).removeChoice($(this).find('span')[0]);
            });
            $('#inviteCuratorsForm')[0].reset()
            $('#inviteCuratorsDialog').modal('show');
            return false;
        }else{
            window.location.href = window.params.loginUrl+"?spring-security-redirect="+window.location.href;
        }
    },
    error:function (xhr, ajaxOptions, thrownError){
        return false;
    } 
    });
});

$("#inviteCuratorButton").click(function(){
    $('#curatorUserIds').val(curators_autofillUsersComp[0].getEmailAndIdsList().join(","));
    var selectedNodes = $(".taxDefIdCheck:checked").map(function() {return $(this).parent("span").find(".taxDefIdVal").val();}).get().join();
    $('#inviteCuratorsForm').ajaxSubmit({ 
        url:window.params.inviteCuratorsFormUrl,
        dataType: 'json', 
        clearForm: true,
        resetForm: true,
        type: 'POST',
        data:{message:$('#inviteCuratorMsg').val(), selectedNodes : selectedNodes},
        success: function(data, statusText, xhr, form) {
            if(data.statusComplete) {
                $('#inviteCuratorsDialog').modal('hide');
                $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            } else {
                $("#invite_CuratorMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
            }
            $('#inviteCuratorsForm')[0].reset()
        }, error:function (xhr, ajaxOptions, thrownError){
            //successHandler is used when ajax login succedes
            var successHandler = this.success;
            handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
                var response = $.parseJSON(xhr.responseText);

            });
            $('#inviteCuratorsForm')[0].reset()
        } 
    });	
});

$("#addSpeciesImagesBtn").click(function(){
    $(".speciesImage-wrapper").toggle();
    $('html, body').animate({
        scrollTop: $(".speciesImage-wrapper").offset().top
    }, 1000);
});

function onSpeciesImageUploadSuccess(type){
    var msgText
    if(type == "imageUpload"){
        msgText = "Images uploaded/edited succesfully, Please refresh the page to see the changes in gallery!!"
        showUpdateStatus(msgText, 'success',$("#speciesImage-tab1") );
    }
    else{
        msgText = "Images succesfully pulled, Please refresh the page to see the changes in gallery!!"
        $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(msgText);
        $('html, body').animate({
            scrollTop: $(".alertMsg").offset().top
        }, 1000);

    }
        return true;
}

$("#uploadSpeciesImagesBtn").click(function(){
    $("#uploadSpeciesImagesForm").ajaxSubmit({success:onSpeciesImageUploadSuccess("imageUpload")});
    return false;
});

$("#pullObvImagesBtn").click(function(){
    $("#pullObvImagesForm").ajaxSubmit({success:onSpeciesImageUploadSuccess("pulledImage")});
    return false;
});

function getNextRelatedObvImages(speciesId, url, resourceListType){
    var offset = $("#relatedImagesOffset").val();
    $.ajax({
        url: url,
        dataType: "json",
        data: {speciesId:speciesId, offset: offset ,resourceListType: resourceListType},	
        success: function(data) {
            var addPhotoHtmlData = $(data.addPhotoHtml);
            if(data.relatedObvCount == 0){
                $("#relatedObvLoadMore").replaceWith('<a class="btn disabled" style="margin-right: 5px;">No More</a>');
            } 
            $("#speciesImage-tab0 .imagesList" ).append(addPhotoHtmlData);
            $("#relatedImagesOffset").val(parseInt(offset) + parseInt(data.relatedObvCount));
            $("#relatedObvLoadMore").insertBefore($("#pullObvImagesBtn"));
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}


