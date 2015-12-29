//bind click event on delete buttons using jquery live
$('.del-document').live('click', function() {
	// find the parent div
	var prnt = $(this).parents(".ufile-block");
	// find the deleted hidden input
	var delInput = prnt.find("input[id$=deleted]");
	// check if this is still not persisted.
	var newValue = prnt.find("input[id$=new]").attr('value');

	// set the deletedFlag to true
	delInput.attr('value', 'true');
	// hide the div
	prnt.hide();

});
