<%@ page import="species.Language"%>

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
	height: 20px;
}

#addObservation .combobox-container .add-on {
	height:36px;
}
.caret {
	vertical-align: middle;
}
</style>

<r:script>
$(document).ready(function() {
	var langDropdown = $('#languageComboBox').combobox();
	var defaultLang = "${Language.getLanguage(null).name}";
	$("#languageComboBox").val(defaultLang).attr("selected",true);
	$("#languageComboBox").data('combobox').refresh();
	
});
</r:script>

<select id="languageComboBox" class="combobox" style="display:none;" name="languageName">
<option></option>
	<g:each in="${Language.filteredList()}">
		<option value="${it}"> ${it}</option>
	</g:each>
</select>
