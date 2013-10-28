function feature(submitType, objectId, objectType, url) {
    var featureNotes = $("#featureNotes").val();
    userGroup = getSelectedUserGroups($("#featureIn"));
    if(userGroup.length === 0){
        alert('Please select at least one group')
            return; 
    }
    if(submitType === 'feature') {
        if(featureNotes === '') {
            alert('Notes are required while featuring.')
                return;
        }
    }
    $.ajax({
        url: url,
        type: 'POST',
        dataType: "json",
        data:{'id':objectId, 'type':objectType, 'userGroup': userGroup.join(","), 'notes': featureNotes},
        success: function(data) {
            if(data.status == 'success'){
                $(".feature-user-groups button.active").removeClass("btn-success active")
                $(".feature-user-groups i.icon-black").removeClass("icon-black").addClass("icon-white")
                $("#featureNotes").val('')
                $("#remainingC").html("Remaining characters : 400");
                $(".resource_in_groups").replaceWith(data.resourceGroupHtml);
                if($(".resource_in_groups li .featured").size() > 0) {
                    $(".main_heading .badge").addClass("featured")
                }
                if($(".resource_in_groups li .featured").size() === 0) {
                    $(".main_heading .badge").removeClass("featured")
                }
                //$('.show-user-groups').slideToggle("slow");
                /*
                $(".resource_in_groups li .featured").popover();
                */
                $(".resource_in_groups li .featured").popover({ 
                    trigger:(is_touch_device ? "click" : "hover"),
                });
                $('#myTab a:first').tab('show');
                showUpdateStatus(data.msg, data.status, $("#featureMsg"));
            } else {
                showUpdateStatus(data.msg, data.status, $("#featureMsg"));
            }
            updateFeeds();	
            }, error: function(xhr, ajaxOptions, error) {
                var successHandler = this.success, errorHandler = showUpdateStatus;
                handleError(xhr, ajaxOptions, error, successHandler, errorHandler);
            }
    });
}

function loadObjectInGroups() {
    $.ajax({
        url: window.params.action.inGroupsUrl,
        type: 'GET',
        dataType: "json",
        data:{'id':objectId, 'type':objectType},
        success: function(data) {
            if(data.status == 'success'){
                $(".resource_in_groups").replaceWith(data.resourceGroupHtml);
                $(".resource_in_groups li .featured").popover({ 
                    trigger:(is_touch_device ? "click" : "hover"),
                });
                showUpdateStatus(data.msg, data.status, $("#featureMsg"));
            } else {
                showUpdateStatus(data.msg, data.status, $("#featureMsg"));
            }
            }, error: function(xhr, ajaxOptions, error) {
                console.log(error);
            }
    });

}
