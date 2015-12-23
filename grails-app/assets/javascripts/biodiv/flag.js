function flag(objectId, objectType, url) {
    var flagNotes = document.getElementById("flagNotes").value;
    var flagType = $('input[name=obvFlag]:checked', '#flag-form').val()
        var options = {
            url: url,
            type: 'POST',
            dataType: "json",
            data:{'id':objectId, 'type':objectType, 'notes': flagNotes, 'obvFlag': flagType},
            success: function(data) {
                if(data.status == 401) {
                    $.ajax(options);
                }
                else if(data.success){
                    $(".flag-list-users").replaceWith(data.flagListUsersHTML);
                    $("#flag-action>i").addClass("icon-red");
                    $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
                    $("html, body").animate({ scrollTop: 0 });
                    $("#flagNotes").val("");
                }else{
                    alert("FAILED MESSAGE");
                }
                updateFeeds();
                return false;
            }, error:function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                var successHandler = this.success, errorHandler = undefined;
                handleError(xhr, ajaxOptions, thrownError, successHandler, function(){
                });
            } 	
        }
    $.ajax(options);
}


