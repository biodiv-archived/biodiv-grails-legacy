<%@ page import="species.participation.RecommendationVote.ConfidenceType"%>
<%@ page import="species.Language"%>

<g:hasErrors bean="${recommendationVoteInstance}">
	<div class="alert alert-error">
		<g:renderErrors bean="${observationInstance}" as="list" />
	</div>
</g:hasErrors>
	<%
		def species_sn_name = ""
		def species_cn_name = ""
		def species_call_comment = ""
		def species_canonical_name = "" 
		//showing vote added by creator of the observation
		if(params.action == 'edit' || params.action == 'update'){
			def tmp_reco_vote = observationInstance?.fetchOwnerRecoVote()
			def tmp_cn_reco	= tmp_reco_vote?.commonNameReco
			
			species_call_comment =  tmp_reco_vote?.comment
			species_cn_name = (tmp_cn_reco)? tmp_cn_reco.name : ""
			
			if(tmp_reco_vote && tmp_reco_vote.recommendation.isScientificName){
				species_sn_name = tmp_reco_vote.recommendation.name
				species_canonical_name = tmp_reco_vote.recommendation.taxonConcept?.canonicalForm
			}
		}
		if(params.action == 'save'){
			species_sn_name = params?.recoName
			species_cn_name =  params?.commonName
			species_call_comment = params?.recoComment
			species_canonical_name = params?.canName
		}
	%>

        <div class="control-group commonNameDiv">
            <label for="recommendationVote" class="control-label"> <g:message
                code="observation.recommendationVote.label" default="${g.message(code:'default.common.name.label')}" />
            </label>
            <div class="controls">
                <div class="nameContainer textbox" style="position:relative;">

                    <input type="text" name="commonName" class="commonName input-block-level" style="width:87%"
                        value="${species_cn_name}" placeholder='${g.message(code:"editrecomendation.placeholder.suggest")}'
                        class="input-block-level ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}" />
                    <input type="hidden" class="mappedRecoNameForcanName" />

                    <div style="width:90px;">
                        <s:chooseLanguage />
                    </div>
                    <div class='nameSuggestions' style='display: block;'></div>

                    </div>
            </div>
        </div>



            <div class="control-group sciNameDiv" style="margin-top:5px;">
                <label for="recommendationVote" class="control-label"> <g:message
                    code="observation.recommendationVote.label" default="${g.message(code:'default.scientific.name.label')}" />
                </label>
                <div class="controls">
                    <div class="textbox nameContainer">

                        <g:set var="species_sn_lang" value="${species_sn_lang}" />
                        <input type="text" name="recoName" class="recoName input-block-level" value="${species_sn_name}" rel="${g.message(code:'placeholder.suggest.species.name')}"
                            placeholder='${g.message(code:"editrecomendation.placeholder.scientific")}'
                            class="input-block-level ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}"/>
                        <div class='nameSuggestions' style='display: block;'></div>
                        <input type="hidden" name="canName" class="canName" value="${species_canonical_name }"/>
                        

                    </div>
                </div>
            </div>


            <div class="control-group recoCommentDiv"  style="margin-top:5px;">
                <label for="recommendationVote" class="control-label"> <g:message
                    code="observation.recommendationVote.label" default="${g.message(code:'default.comment.label')}" />
                </label>
                <div class="controls">
                    <div class="nameContainer textbox">
                        <input type="text" name="recoComment" id="recoComment" value="${species_call_comment}"
                        class="input-block-level ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')} ${hasErrors(bean: recommendationVoteInstance, field: 'recommendation', 'errors')}"
                        placeholder="${g.message(code:'editrecomendation.placeholder.comment')}"/>
                    </div>
                </div>
            </div>



<r:script>
</r:script>
