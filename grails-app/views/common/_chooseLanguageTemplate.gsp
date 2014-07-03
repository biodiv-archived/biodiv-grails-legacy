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
.combobox-container .caret {
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
        if(species_sn_lang == "" || species_sn_lang == null){
            species_sn_lang = Language.getLanguage(null).name;
        }
        if(species_sn_lang == "" || species_sn_lang == null){
            species_sn_lang = "English"
        }
%>

<select class="languageComboBox combobox" style="display:none;" data-defaultlanguage="${species_sn_lang}" >
    <option></option>
    <g:each in="${Language.filteredList()}">
        <option value="${it}"> ${it}</option>
    </g:each>
</select>
