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
					//alert("FLAG SUCCESS");
					//alert()
					//$(".alertMsg").removeClass('alert alert-error').addClass('alert alert-success').html(data.msg);
				}else{
					alert("FAILED MESSAGE");
					//$(".alertMsg").removeClass('alert alert-success').addClass('alert alert-error').html(data.msg);
				}
				//$("html, body").animate({ scrollTop: 0 });
				return false;
			}, error: function(xhr, status, error) {
				alert("AJAX FAILED, Fatal Error");
				//alert(xhr.responseText);
		   	}
		});

}


