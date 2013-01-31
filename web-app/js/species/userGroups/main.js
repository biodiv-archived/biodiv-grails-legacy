/**
 * 
 */
function reloadActionsHeader() {
	$.ajax({
        	url: window.reloadActionsHeaderUrl,
        	 method: "POST",
            dataType: "html",
            success: function(data) {
            	$("#actionsHeader").html(data);
            }
    });
}


$('.home_page_label').click(function(){
	var caret = '<span class="caret"></span>'
	if($.trim(($(this).html())) == $.trim($("#homePageSelector").html().replace(caret, ''))){
		return true;
	}
	$('.home_page_label.active').removeClass('active');
	$(this).addClass('active');
	var homePageVal = $(this).attr("value");
	$(this).closest(".prop").children('input[name="homePage"]').val(homePageVal);
    $("#homePageSelector").html($(this).html() + caret);
    return true;   
});

$('.theme_label').click(function(){
	var caret = '<span class="caret"></span>'
	if($.trim(($(this).html())) == $.trim($("#themeSelector").html().replace(caret, ''))){
		return true;
	}
	$('.theme_label.active').removeClass('active');
	$(this).addClass('active');
	var themeVal = $(this).attr("value");
	$(this).closest(".prop").children('input[name="theme"]').val(themeVal);
    $("#themeSelector").html($(this).html() + caret);
    return true;   
});


function getSelectedVal(labelClass) {
	var retVal = ''; 
	$('.' + labelClass).each (function() {
		if($(this).hasClass('active')) {
			retVal += $(this).attr('value') + ',';
        }
	});
	retVal = retVal.replace(/\s*\,\s*$/,'');
	return retVal;
} 


