var curators_autofillUsersComp;
var contributors_autofillUsersComp;

$('#inviteCuratorsDialog,#inviteContributorsDialog').modal({
    "show" : false,
    "backdrop" : "static"
});

$('#inviteCurators, #inviteContributors').click(function(e){
    var invitetype = $(this).data('invitetype');
    var $dialog, $autofillUsers;
    if(invitetype === 'curator') {
        $dialog = $('#inviteCuratorsDialog');
        $autofillUsers = curators_autofillUsersComp;
    } else {
        $dialog = $('#inviteContributorsDialog');
        $autofillUsers = contributors_autofillUsersComp;
    }
    $.ajax({ 
        url:window.params.isLoggedInUrl,
        success: function(data, statusText, xhr) {
        if(data === "true"){
            $dialog.find('input[name="userIds"]').val('');
            $('#userAndEmailList_'+invitetype).val('');
            $($autofillUsers[0]).parent().children('li').each(function(){
                $($autofillUsers[0]).removeChoice($(this).find('span')[0]);
            });

            $dialog.find('form')[0].reset()
            $dialog.modal('show');
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

$(".inviteButton").click(function(){
    var $dialog = $(this).parent().parent();
    var selectedNodes = $(".taxDefIdCheck:checked").map(function() {return $(this).parent("span").find(".taxDefIdVal").val();}).get().join();
    var invitetype = $dialog.find('input[name="invitetype"]').val();

    var $autofillUsers;
    if(invitetype === 'curator') {
        $autofillUsers = curators_autofillUsersComp[0]
    } else {
        $autofillUsers = contributors_autofillUsersComp[0]
    }
    $dialog.find('input[name="userIds"]').val($autofillUsers.getEmailAndIdsList().join(","));

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

            });
            $dialog.find('form')[0].reset()
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
    else if(type == "pulledSpeciesFieldImage"){
        msgText = "Images succesfully pulled, Please refresh the page to see the changes in gallery!!"
        showUpdateStatus(msgText, 'success',$("#speciesImage-tab2") );
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

$("#pullSpeciesFieldImagesBtn").click(function(){
    $("#pullSpeciesFieldImagesForm").ajaxSubmit({success:onSpeciesImageUploadSuccess("pulledSpeciesFieldImage")});
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


