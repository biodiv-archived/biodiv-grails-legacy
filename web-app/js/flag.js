function flag(objectId, objectType, url) {
	var flagNotes = document.getElementById("flagNotes").value;
	var flagType = $('input[name=obvFlag]:checked', '#flag-form').val()
	$.ajax({
	 		url: url,
	 		type: 'POST',
			dataType: "json",
			data:{'id':objectId, 'type':objectType, 'notes': flagNotes, 'obvFlag': flagType},
			success: function(data) {
				if(data.success){
                                        $(".flag-list-users").replaceWith(data.flagListUsersHTML);
                                        $("#flag-action>i").addClass("icon-red");
                                        $(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				        $("html, body").animate({ scrollTop: 0 });
				}else{
					alert("FAILED MESSAGE");
				}
                                updateFeeds();
				return false;
			}, error: function(xhr, status, error) {
				alert("AJAX FAILED, Fatal Error");
		   	}
		});

}


