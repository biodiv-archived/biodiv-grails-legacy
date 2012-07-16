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

function getBackgroundPos(obj) {
	var pos = obj.css("background-position");
	if (pos == 'undefined' || pos == null) {
		pos = [obj.css("background-position-x"),obj.css("background-position-y")];//i hate IE!!
	} else {
		pos = pos.split(" ");
	}
	return {
		x: parseFloat(pos[0]),
		xUnit: pos[0].replace(/[0-9-.]/g, ""),
		y: parseFloat(pos[1]),
		yUnit: pos[1].replace(/[0-9-.]/g, "")
	};
}