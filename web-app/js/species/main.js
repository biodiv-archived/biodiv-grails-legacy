
function handleError(xhr, textStatus, errorThrown, callback) {
	if(xhr.status == 401 || xhr.status == 200) {
		show_login_dialog();
	} else {
		if(callback) 
			callback();
		else 
			alert(errorThrown);
	}
}

