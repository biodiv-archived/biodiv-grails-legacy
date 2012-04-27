if (typeof (console) == "undefined") {
	console = {};
}
if (typeof (console.log) == "undefined") {
	console.log = function() {
		return 0;
	}
}


function show_login_dialog(successHandler, errorHandler) {
	ajaxLoginSuccessCallbackFunction = successHandler;
	ajaxLoginErrorCallbackFunction = errorHandler;
	$('#ajaxLogin').modal('show');
}

function cancelLogin() {
	$('#ajaxLogin').modal('hide');
}

function handleError(xhr, textStatus, errorThrown, successHandler, errorHandler) {
	if (xhr.status == 401) {
		//show_login_dialog(successHandler, errorHandler);
		console.log(xhr);
		window.location.href = "/biodiv/login?spring-security-redirect="+window.location.href;
	} else {
		if (errorHandler)
			errorHandler();
		else
			alert(errorThrown);
	}
}
