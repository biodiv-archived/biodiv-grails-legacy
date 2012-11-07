<%@page import="species.participation.Observation"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.Recommendation"%>

<g:if test="${result.size() > 0 }">
	<g:each in="${result}" var="r">
		<li class="reco_block">
			<div>
				<div class="users">
					<div class="iAgree ">
						<g:if test="${customsecurity.hasPermissionToMakeSpeciesCall([id:r.obvId, className:species.participation.Observation.class.getCanonicalName(),
										permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()}">
							<a href="#" class="btn btn-primary btn-small" onclick="addAgreeRecoVote(${r.obvId}, ${r.recoId}, ${r.noOfVotes}, $(this).closest('li'), '${uGroup.createLink(controller:'observation', action:'addAgreeRecommendationVote')}'); return false;">Agree</a>
						</g:if><g:else>
								<a href="#" onclick="$('#selectedGroupList').modal('show'); return false;"
								title="Protected to group members/experts. Need to join any of the user groups this observation belongs to inorder to add a species call" class="btn btn-primary btn-small">Join Groups / Be an expert</a>
						</g:else>
					</div>
					<g:each in="${r.authors}" var="author">
						<a href="${uGroup.createLink(controller:"SUser", action:"show", id:author?.id)}" title="${author?.name }">
							<img class="small_profile_pic"
								src="${author?.icon(ImageType.VERY_SMALL)}"
								title="${author.name}" />
						</a>
					</g:each>
				</div>

				<g:if test="${r.observationImage}">
					<a href="${uGroup.createLink([action:"show", controller:"observation", id:r.obvId, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
					
						<img style="width: 75px; height: 75px;"
							src="${r.observationImage}">
					</a>
				</g:if>

				<span class="voteCount"><span id="votes_${r.recoId}">
						${r.noOfVotes} </span> <g:if test="${r.noOfVotes <= 1}"> user thinks</g:if>
					<g:else> users think</g:else> it is:</span><span class="highlight ellipsis multiline">
					<g:if test="${r.canonicalForm}">
						<a href="${uGroup.createLink(action:'show', controller:'species', id:r.speciesId, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
							<i> ${r.canonicalForm} </i>
						</a>
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



