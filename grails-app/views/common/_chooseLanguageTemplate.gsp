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
<g:javascript src="bootstrap-combobox.js"></g:javascript>
<g:javascript src="bootstrap-typeahead.js"></g:javascript>
<%
	def species_sn_lang = null
	//showing vote added by creator of the observation
	if(params.action == 'edit' || params.action == 'update'){
		def tmp_cn_reco = observationInstance?.fetchOwnerRecoVote()?.commonNameReco
		if(tmp_cn_reco){
			species_sn_lang = Language.read(tmp_cn_reco.languageId).name
		}
	}
%>
<script>
$(document).ready(function() {
	function doCustomization(langCombo){
		var inputTextEle = langCombo.data('combobox').$element
		inputTextEle.unbind('blur');
	    inputTextEle.attr('name', 'languageName');
	    inputTextEle.attr('autocomplete', 'off');
		inputTextEle.on('blur', $.proxy(myBlur, langCombo.data('combobox')));
	}

	function myBlur(e){
		var oldVal = this.$element.val();
		this.blur(e);
		this.$element.val(oldVal);
	}

	$('#languageComboBox').combobox();
	var langCombo = $("#languageComboBox");
	doCustomization(langCombo);
	var defaultLang = "${species_sn_lang}";
	if(defaultLang === ""){
		defaultLang = "${Language.getLanguage(null).name}";
	}
	langCombo.val(defaultLang).attr("selected",true);
	langCombo.data('combobox').refresh();
});

function updateCommonNameLanguage(){
	var langCombo = $("#languageComboBox");
	var inputVal = $.trim(langCombo.data('combobox').$element.val());
	if(inputVal.toLowerCase() !== langCombo.val().toLowerCase()){
		langCombo.append($('<option></option>').val(inputVal).html(inputVal));
		langCombo.data('combobox').refresh();
	}
}

</script>


<select id="languageComboBox" class="combobox" style="display:none;" >
<option></option>
	<g:each in="${Language.filteredList()}">
		<option value="${it}"> ${it}</option>
	</g:each>
</select>
