/**
 * 
 */


(function($) {

	var ajaxLoginFormHandler = function(event) {
		$(this)
				.ajaxSubmit(
						{
							type : 'POST',
							target : '#loginMessage',
							dataType : 'json',
							beforeSubmit : function(formData, jqForm, options) {
								$('#loginMessage').html('Logging in ...')
										.removeClass().addClass(
												'alter alert-info').show();
								return true;
							},
							success : ajaxLoginSuccessHandler,
							error : function(xhr, ajaxOptions, thrownError) {
								//some uncaught server error during authentication or processing of save request
								handleError(
										xhr,
										ajaxOptions,
										thrownError,
										undefined,
										function() {
											if (ajaxLoginErrorCallbackFunction) {
												ajaxLoginErrorCallbackFunction();
												ajaxLoginErrorCallbackFunction = undefined;
											} else {
												$('#loginMessage')
														.html(xhr.responseText)
														.removeClass()
														.addClass(
																'alter alert-error')
														.show();
												alert(window.i8ln.species.ajaxLogin.ewp);
											}
											updateLoginInfo();
										});
							}
						});
		event.preventDefault();
		return false;
	}

	$('#ajaxLogin form').bind('submit', ajaxLoginFormHandler);
	
	$('.googleConnect').click(function(e) { 
		//googleOpener.popup(450,500);
        handleAuthClick(e);
		return true; 
	});
	
	$('.yahooConnect').click(function() { 
		yahooOpener.popup(450,500);
		return true; 
	});
	
	$('#ajaxLogin').modal({
		"show" : false,
		"backdrop" : "static"
	});

	$('#ajaxLogin').on('hidden', function () {
		$('#ajaxLogin form')[0].reset();
                if(ajaxLoginCancelCallbackFunction) {
                    ajaxLoginCancelCallbackFunction();
                }
	})
	
	$(".s2ui_hidden_button").hide();

    function handleAuthResult(authResult) {
        console.log(authResult);
        if (authResult && !authResult.error) {
        $('#loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
        var authParams = {'response': JSON.stringify(authResult).replace(/:/g,' : ')};
            $.ajax({
                url: window.params.login.googleOAuthSuccessUrl,
                method:"POST",
                data:authParams,
                success: function(data, statusText, xhr) {
                    ajaxLoginSuccessHandler(data, statusText, xhr);
                },  error: function(xhr, ajaxOptions, thrownError) {
                    $('#loginMessage').html(xhr.responseText).removeClass().addClass('alter alert-error').show();
                }
            });

        } else {
            authorizeButton.onclick = handleAuthClick;
            alert('Failed to connect to Google');
        }
    }
    function handleAuthClick(event) {
        gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: true}, handleAuthResult);
        return false;
    }


    var remember = $.cookie('remember');
	//alert(remember);
    if (remember == 'true') 
    {        	
        var email = $.cookie('email');
        var password = $.cookie('password');
        // autofill the fields
        $('.username').val(email);
        //alert(email);
        //alert($('.username').html());
        $('.password').val(password);
        $('.remember_me').attr('checked',true);
    }
    $(document).on('click','#login',function(event) {    	  	
        if ($(this).parent().find('#remember_me').is(':checked')) {        	
            var email = $(this).parent().parent().parent().find('#username').val();
            var password = $(this).parent().parent().parent().find('#password').val();
            // set cookies to expire in 14 days
            $.cookie('email', email, { expires: 14 });
            $.cookie('password', password, { expires: 14});
            $.cookie('remember', true, { expires: 14});  
            $(this).submit();            
        }
        else
        {
            // reset cookies
            $.cookie('email', null);
            $.cookie('password', null);
            $.cookie('remember', null);
         	$(this).submit();
        }

        
  	});

})(jQuery);
