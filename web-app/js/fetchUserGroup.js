function fetchUserGroup(url){
	$.ajax({
 		url: url,
		dataType: "json",
		success: function() {
				var htmlData = $(data.showSuggestedUserGroupsHtml);
			
		}, error: function(xhr, status, error) {
			alert(xhr.responseText);
	   	}
	});
}

