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
