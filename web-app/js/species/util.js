/**
 * 
 */

$(function() {
	var spt = $('span.mailme');
	var at = /\(at\)/;
	var dot = /\(dot\)/g;
	$(spt).each(function() {
		var addr = $(this).text().replace(at, "@").replace(dot, ".");
		$(this).after(
				'<a href="mailto:' + addr + '" title="Send an email">' + addr
						+ '</a>').hover(function() {
			window.status = "Send an email!";
		}, function() {
			window.status = "";
		});
		$(this).remove();	
	});
	
});