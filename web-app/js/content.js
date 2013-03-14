//bind click event on delete buttons using jquery live
$('.del-ufile').live('click', function() {
	//find the parent div
	var prnt = $(this).parents(".ufile-block");
	//find the deleted hidden input
	var delInput = prnt.find("input[id$=deleted]");
	//check if this is still not persisted
	var newValue = prnt.find("input[id$=new]").attr('value');
	//if it is new then i can safely remove from dom
	if (newValue === 'true') {
		prnt.remove();
	} else {
		//set the deletedFlag to true
		delInput.attr('value', 'true');
		//hide the div
		prnt.hide();

	}
});