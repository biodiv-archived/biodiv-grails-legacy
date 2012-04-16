<%@page import="species.utils.ImageType"%>
<style>
.reco_block {
	background-color: #ffffff;
	padding: 5px;
	margin-bottom: 3px;
	border-radius: 5px;
	min-height:30px;
}

.reco {
	background-color: #ffffff;
	padding: 3px;
	width: 75%;
	float: left;
}

.iAgree {
	float: right;
}

.users {
	clear: both;
	float:right;
}

.name {
	font-weight: bold;
}
</style>

<g:if test="${result.size() > 0 }">
	<g:each in="${result}" var="r">
		<li class="reco_block">
			<div>
			<div class="users">
					<div class="iAgree btn btn-primary btn-small">
						<g:remoteLink action="addAgreeRecommendationVote" method="GET"
							controller="observation"
							params="['obvId':observationInstance.id, 'recoId':r.recoId, 'currentVotes':r.noOfVotes]"
							onSuccess="showRecos(data, textStatus);return false"
							onFailure="handleError(XMLHttpRequest,textStatus,errorThrown);return false;">Agree</g:remoteLink>
					</div>
					<g:each in="${r.authors}" var="author">
						<g:link controller="SUser" action="show" id="${author?.id}">
							<img class="very_small_profile_pic"
								src="${author?.icon(ImageType.VERY_SMALL)}"
								title="${author.username}" />
						</g:link>
					</g:each>
				</div>
				<span class="voteCount"><span id="votes_${r.recoId}">
						${r.noOfVotes} </span> <g:if test="${r.noOfVotes <= 1}"> user thinks</g:if>
					<g:else> users think</g:else> it is:</span><span class="name"> <g:if
						test="${r.canonicalForm}">
						<g:link controller="species" action="show" id="${r.speciesId}">
							<i>
								${r.canonicalForm}
							</i>
						</g:link>
					</g:if> <g:else>
						<i>
							${r.name}
						</i>
					</g:else> </span>
				<!-- div style="width:${(r.noOfVotes/totalVotes)*100}%" class="pollbar"></div-->
				
			</div> <g:javascript>
                        $(document).ready(function(){
                                $('#voteCountLink_${r.recoId}').click(function() {
                                        $('#voteDetails_${r.recoId}').show();
                                });

                                $('#voteDetails_${r.recoId}').mouseout(function(){
                                        $('#voteDetails_${r.recoId}').hide();
                                });
                        });
                       
                        </g:javascript>
		</li>
	</g:each>
	
<g:javascript>

	function showRecos(data, textStatus) {
		jQuery('#recoSummary').html(jQuery(data).find('recoHtml').text());
		$("#seeMoreMessage").hide();
	}
	
	function handleError(XMLHttpRequest,textStatus,errorThrown) {
		if(XMLHttpRequest.status == 401 || XMLHttpRequest.status == 200) {
			show_login_dialog();
		} else {	    
			alert(errorThrown);
		}
	}
                        
</g:javascript>	
</g:if>
<g:else>
	<g:message code="recommendations.zero.message" />
</g:else>



