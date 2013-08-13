<%@ page import="species.Language"%>

<style>
.combobox-container {
	position: absolute;
	top: 0;
	right:4%;
	
}
.combobox-container input:first-child {
	width:56px;
} 

.combobox-container .add-on {
	position: absolute;
	height: 22px;
}

/*#addObservation .combobox-container .add-on {
	height:36px;
}*/
.caret {
	vertical-align: middle;
}
</style>
<%
	def species_sn_lang = null
	//showing vote added by creator of the observation
	if(params.action == 'edit' || params.action == 'update'){
		def tmp_cn_reco = observationInstance?.fetchOwnerRecoVote()?.commonNameReco
		if(tmp_cn_reco && (tmp_cn_reco.languageId != null)){
			species_sn_lang = Language.read(tmp_cn_reco.languageId).name
		}
	}
	if(params.action == 'save'){
		species_sn_lang = saveParams?.languageName
	}
%>
<r:script>
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
	var langComboVal = langCombo.val();
	if(langComboVal != null){
		langComboVal = langComboVal.toLowerCase()
	}
	
	var inputVal = $.trim(langCombo.data('combobox').$element.val());
	if(inputVal.toLowerCase() !== langComboVal){
		langCombo.append($('<option></option>').val(inputVal).html(inputVal));
		langCombo.data('combobox').refresh();
	}
}

</r:script>


<select id="languageComboBox" class="combobox" style="display:none;" >
<option></option>
	<g:each in="${Language.filteredList()}">
		<option value="${it}"> ${it}</option>
	</g:each>
</select>
