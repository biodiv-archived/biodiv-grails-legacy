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
	$('#ajaxLogin').modal({'keyboard':true, 'show':true});
}

function cancelLogin() {
	$('#ajaxLogin').modal('hide');
}

function handleError(xhr, textStatus, errorThrown, successHandler, errorHandler) {
	if (xhr.status == 401) {
		show_login_dialog(successHandler, errorHandler);
		//window.location.href = "/biodiv/login?spring-security-redirect="+window.location.href;
	} else {
		if (errorHandler)
			errorHandler();
		else
			console.log(errorThrown);
	}
}

function adjustHeight() {
	$(".ellipsis").ellipsis();
	$('.snippet .observation_story_image').each(function() {
		$(this).css({
	    	'height': $(this).next().height()
	    });
	});
}
// Callback to execute whenever ajax login is successful.
// Todo some thing meaningful with the response data
var ajaxLoginSuccessCallbackFunction, ajaxLoginErrorCallbackFunction;

var reloadLoginInfo = function() {
	$.ajax({
		url : window.appContext+"/SUser/login",
		success : function(data) {
			$('.header_userInfo').replaceWith(data);
		}, error: function (xhr, ajaxOptions, thrownError){
			alert("Error while getting login information : "+xhr.responseText);
		}
	});
}
		
var ajaxLoginSuccessHandler = function(json, statusText, xhr, $form) {
	$('#ajaxLogin').modal('hide');
	$('#loginMessage').html('').removeClass().hide();
	reloadLoginInfo();
	
	if (json.success) {
		if (ajaxLoginSuccessCallbackFunction) {
			ajaxLoginSuccessCallbackFunction(json,
					statusText, xhr);
			ajaxLoginSuccessCallbackFunction = undefined;
		}
	} else if (json.error) {
		$('#loginMessage').html(json.error).removeClass().addClass('alter alert-error').show();
	} else {
		$('#loginMessage').html(json).removeClass().addClass('alter alert-info').show();
	}
}

jQuery(document).ready(function($) {
	var domain = document.domain.replace('http://','').replace('www.','').replace(':8080','');
//	if (domain == appWGPDomain){
//        $('#ibp-header').hide();
//        $('#wgp-header').show();
//        $('#ibp-footer').hide();
//        $('#wgp-footer').show();
//    }
//
//    if (domain == appIBPDomain){
//        $('#wgp-header').hide();
//        $('#ibp-header').show();
//        $('#wgp-footer').hide();
//        $('#ibp-footer').show();
//    }

	$("#menu .navigation li").hover(
  		function () {
    		$(".subnavigation", this).show();
  		}, 
  		function () {
    		$(".subnavigation", this).hide();
  		}
	);
	$.widget( "custom.catcomplete", $.ui.autocomplete, {
		_renderMenu: function( ul, items ) {
			var self = this,
				currentCategory = "";
			$.each( items, function( index, item ) {
				if ( item.category != currentCategory ) {
					ul.append( "<li class='ui-autocomplete-category'>" +item.category + "</li>" );
					currentCategory = item.category;
				}
				self._renderItem( ul, item );
			});
		}
	});
	
	

	
});
