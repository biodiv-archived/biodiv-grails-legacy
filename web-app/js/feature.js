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
                if($(".resource_in_groups li.featured").size() > 0) {
                    $(".badge").addClass("featured")
                }
                if($(".resource_in_groups li.featured").size() === 0) {
                    $(".badge").removeClass("featured")
                }
                $('.show-user-groups').slideToggle("slow");
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
