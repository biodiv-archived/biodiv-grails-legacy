function feature(objectId, objectType, url) {
        var featureNotes = $("#featureNotes").val();
        userGroup = getSelectedUserGroups($("#featureIn"));
	if(userGroup.length === 0){
		alert('Please select at least one group')
		return; 
	}

	$.ajax({
 		url: url,
 		type: 'POST',
		dataType: "json",
		data:{'id':objectId, 'type':objectType, 'userGroup': userGroup.join(","), 'notes': featureNotes},
		success: function(data) {
			if(data.success){
			    $(".is-featured").replaceWith(data.freshUGListHTML);
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

function unfeature(objectId, objectType, url) {
        userGroup = getSelectedUserGroups($("#featureIn"));
	if(userGroup.length === 0){
		alert('Please select at least one group')
		return; 
	}
		$.ajax({
 		url: url,
 		type: 'POST',
		dataType: "json",
		data:{'id':objectId, 'type':objectType, 'userGroup':userGroup.join(",")},
		success: function(data) {
			if(data.success){
                            $(".is-featured").replaceWith(data.freshUGListHTML);
			   
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

