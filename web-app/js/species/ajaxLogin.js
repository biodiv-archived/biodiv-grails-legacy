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
							success : function(json, statusText, xhr, $form) {
								if (json.success) {
									$('#ajaxLogin').modal('hide');
									$('#loginMessage').html('').removeClass()
											.hide();
									reloadLoginInfo();
									if (ajaxLoginSuccessCallbackFunction) {
										ajaxLoginSuccessCallbackFunction(json,
												statusText, xhr);
										ajaxLoginSuccessCallbackFunction = undefined;
									}
								} else if (json.error) {
									$('#loginMessage').html(json.error)
											.removeClass().addClass(
													'alter alert-error')
											.show();
								} else {
									$('#loginMessage').html(json).removeClass()
											.addClass('alter alert-info')
											.show();
								}
							},
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
	/*
	 * $('.googleConnect').click(function() { googleOpener.popup(450,500);
	 * return true; });
	 */
	$('#ajaxLogin').modal({
		"show" : false,
		"backdrop" : "static"
	});
})(jQuery);