/**
 * 
 */


(function($) {

	var ajaxLoginFormHandler = function(event) {
		$(this)
				.ajaxSubmit(
						{
							type : 'POST',
							target : '.loginMessage',
							dataType : 'json',
							beforeSubmit : function(formData, jqForm, options) {
								$('.loginMessage').html('Logging in ...')
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
												$('.loginMessage')
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
		event.stopPropagation();
		return false;
	}
	
    $('#ajaxLogin form.isParentGroup').on('submit', ajaxLoginFormHandler);
	
	
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
	
	$('#ajaxLogin').on('show', function () {
	});

	$(".s2ui_hidden_button").hide();

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

/*function oauth_login(app) {
    var redirectUrl = 'http://localhost.indiabiodiversity.org/login/' + app.toLowerCase();
    if (window.opener) {
        window.location = redirectUrl;
    } else {
        var oAuthWndow = window.open(redirectUrl, "Biodiversity Portal", "width=800, height=600, top=100, left=300");
        var interval = window.setInterval(function() {
            console.log('sdfsdfdf');
            if (oAuthWndow.location.href.indexOf('biodiv') != -1) {
                oAuthWndow.close();
                location.reload();
            }
        }, 1000);
    }
}*/

