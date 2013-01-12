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
											}
										});
							}
						});
		event.preventDefault();
		return false;
	}

	$('#ajaxLogin form').bind('submit', ajaxLoginFormHandler);
	
	$('.googleConnect').click(function() { 
		googleOpener.popup(450,500);
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
	})
})(jQuery);