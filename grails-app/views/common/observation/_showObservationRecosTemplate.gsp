<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.Recommendation"%>

<g:if test="${result.size() > 0 }">
	<g:each in="${result}" var="r">
		<li class="reco_block">
			<div>
				<div class="users">
					<div class="iAgree ">
						<g:if test="${customsecurity.hasPermissionToMakeSpeciesCall([object:observationInstance,
										permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()}">
							<a href="#" class="btn btn-primary btn-small" onclick="addAgreeRecoVote(${r.obvId}, ${r.recoId}, ${r.noOfVotes}, $(this).closest('li'), '${createLink(controller:'observation', action:'addAgreeRecommendationVote')}'); return false;">Agree</a>
						</g:if><g:else>
							<a href="#"
								title="Protected to group members/experts. Need to join any of the user groups this observation belongs to inorder to add a species call" class="btn btn-primary btn-small disabled">Join Groups / Be an expert</a>
						</g:else>
					</div>
					<g:each in="${r.authors}" var="author">
						<g:link controller="SUser" action="show" id="${author?.id}">
							<img class="very_small_profile_pic"
								src="${author?.icon(ImageType.VERY_SMALL)}"
								title="${author.name}" />
						</g:link>
					</g:each>
				</div>

				<g:if test="${r.observationImage}">
					<g:link controller="observation" action="show" id="${r.obvId}">
						<img style="width: 75px; height: 75px;"
							src="${r.observationImage}">
					</g:link>
				</g:if>

				<span class="voteCount"><span id="votes_${r.recoId}">
						${r.noOfVotes} </span> <g:if test="${r.noOfVotes <= 1}"> user thinks</g:if>
					<g:else> users think</g:else> it is:</span><span class="highlight ellipsis multiline">
					<g:if test="${r.canonicalForm}">
						<g:link controller="species" action="show" id="${r.speciesId}">
							<i> ${r.canonicalForm} </i>
						</g:link>
					</g:if>
					<g:elseif test="${r.isScientificName}">
						<i>${r.name}</i>
					</g:elseif>
					<g:else>
						${r.name}
					</g:else>${r.commonNames} </span>
				<comment:showCommentPopup model="['commentHolder':Recommendation.read(r.recoId), 'rootHolder':observationInstance]" />
<%--				<obv:showRecoComment--%>
<%--					model="['recoComments':r.recoComments, 'recoId': r.recoId]" />--%>

			</div> <g:javascript>
                        $(document).ready(function(){
                                $('#voteCountLink_${r.recoId}').click(function() {
                                        $('#voteDetails_${r.recoId}').show();
                                });

                                $('#voteDetails_${r.recoId}').mouseout(function(){
                                        $('#voteDetails_${r.recoId}').hide();
                                });
                        });
                        
   </g:javascript></li>
	</g:each>
</g:if>
<g:else>
	<g:message code="recommendations.zero.message" />
</g:else>



