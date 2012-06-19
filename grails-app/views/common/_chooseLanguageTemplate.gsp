<%@ page import="species.Language"%>
<link rel="stylesheet"
	href="${resource(dir:'css',file:'bootstrap-combobox.css')}"
	type="text/css" media="all" />
<style>
.combobox-container {
	position: absolute;
	top: 0;
	left: 210px;
	
}
.combobox-container input:first-child {
	width:56px;
} 

.combobox-container .add-on {
	position: absolute;
	right: -26px;
}

#addObservation .combobox-container .add-on {
	height:36px;
}
.caret {
	vertical-align: middle;
}
</style>
<g:javascript src="bootstrap-combobox.js"></g:javascript>
<g:javascript src="bootstrap-typeahead.min.js"></g:javascript>

<script>
$(document).ready(function() {
	var langDropdown = $('#languageComboBox').combobox();
	var defaultLang = "${Language.getLanguage(null).name}";
	$("#languageComboBox").val(defaultLang).attr("selected",true);
	$("#languageComboBox").data('combobox').refresh();
	
});
</script>

<select id="languageComboBox" class="combobox" style="display:none;" name="languageName">
<option></option>
	<g:each in="${Language.filteredList()}">
		<option value="${it}"> ${it}</option>
	</g:each>
</select>
