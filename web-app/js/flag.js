function flag(objectId, objectType, url) {
	console.log("FLAG function called");
	var flagNotes = $('.comment-textbox').value;
	var flagType = $('input[name=obvFlag]:checked', '#flag-form').val()
	console.log(flagType);
	$.ajax({
	 		url: url,
	 		type: 'POST',
			dataType: "json",
			data:{'id':objectId, 'type':objectType, 'notes': flagNotes, 'obvFlag': flagType},
			success: function(data) {
				if(data.success){
                                        $(".flag-list-users").replaceWith(data.flagListUsersHTML);
                                        $("#flag-action>i").addClass("icon-red");
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


