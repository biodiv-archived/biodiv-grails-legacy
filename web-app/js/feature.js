function feature(objectId, objectType, url) {
	console.log("feature function called");
    var featureNotes = document.getElementById("notes").value;
    console.log(featureNotes);
    userGroup = getSelectedUserGroups();
	if(userGroup.length === 0){
		alert('Please select at least one group')
		return; 
	}

	/*if(!$('.post-to-groups .select-all').hasClass('active') && selectedObjects.length === 0){
		alert('Please select at least one object');
		return;
	}
	
	userGroups = getSelectedUserGroups();
	if(userGroups.length === 0){
		alert('Please select at least one group')
		return; 
	}
	
	if(submitType === 'post'){
		console.log("posting " + selectedObjects +  ' on groups ' + userGroups);
	}else{
		console.log("unposting " + selectedObjects +  ' on groups ' + userGroups);
	}
	
	var selectionType = $('.post-to-groups .select-all').hasClass('active') ? 'selectAll' : 'reset'
	var objectIds = (selectionType === 'selectAll') ?  rejectedObjects : selectedObjects
	var filterUrl = window.location.href	
	*/
	$.ajax({
 		url: url,
 		type: 'POST',
		dataType: "json",
		data:{'id':objectId, 'type':objectType, 'userGroup': userGroup.join(","), 'notes': featureNotes},
		success: function(data) {
			if(data.success){
				alert("SUCCESS");
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

function unfeature(objectId, objectType, url) {
	console.log("unfeature function called");
        userGroup = getSelectedUserGroups();
	if(userGroup.length === 0){
		alert('Please select at least one group')
		return; 
	}
	/*if(!$('.post-to-groups .select-all').hasClass('active') && selectedObjects.length === 0){
		alert('Please select at least one object');
		return;
	}
	
	userGroups = getSelectedUserGroups();
	if(userGroups.length === 0){
		alert('Please select at least one group')
		return; 
	}
	
	if(submitType === 'post'){
		console.log("posting " + selectedObjects +  ' on groups ' + userGroups);
	}else{
		console.log("unposting " + selectedObjects +  ' on groups ' + userGroups);
	}
	
	var selectionType = $('.post-to-groups .select-all').hasClass('active') ? 'selectAll' : 'reset'
	var objectIds = (selectionType === 'selectAll') ?  rejectedObjects : selectedObjects
	var filterUrl = window.location.href	
	*/
	$.ajax({
 		url: url,
 		type: 'POST',
		dataType: "json",
		data:{'id':objectId, 'type':objectType, 'userGroup':userGroup.join(",")},
		success: function(data) {
			if(data.success){
				alert("SUCCESS");
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

