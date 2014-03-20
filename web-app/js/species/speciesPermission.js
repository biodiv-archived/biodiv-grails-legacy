var curators_autofillUsersComp;

$('#inviteCuratorsDialog').modal({
    "show" : false,
    "backdrop" : "static"
});

$('#inviteCurators').click(function(){
    //console.log("checking logged in");
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
                console.log("====RETURNED MSG ======" + data.msg);
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

var uploadSpeciesImageOptions = { 
    success: onSpeciesImageUploadSuccess  // post-submit callback 
};

$("#uploadSpeciesImagesBtn").click(function(){
    console.log("called upload images form");
    $("#uploadSpeciesImagesForm").ajaxSubmit({
        success: onSpeciesImageUploadSuccess
    });
    $("#uploadSpeciesImagesForm").replaceWith( "<span>Images Uploaded Succesfully, Please refresh the page to see the newly uploaded image in the gallery!!</span>" );
    $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html("Images Uploaded Succesfully, Please refresh the page to see the newly uploaded image in the gallery!!");
    return false;
});

function onSpeciesImageUploadSuccess(responseText, statusText, xhr, $form){
    console.log("call back called");
    $("#uploadSpeciesImagesForm").replaceWith( "<span>Loaded Succesfully</span>" ); 
    console.log("DONE DONE ");
    return true;
}
