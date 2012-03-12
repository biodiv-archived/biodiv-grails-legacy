<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'pollBars.css', absolute:true)}" />

<g:if test="${result.size() > 0 }">
	<g:each in="${result}" var="r">
		<li>
			<div style="width:70%; border: solid 2px;">
				<span class="name"> <g:if test="${r.canonicalForm}">
						<g:link controller="species" action="show" id="${r.speciesId}">
							${r.canonicalForm}
						</g:link>
					</g:if> <g:else>
						${r.name}
					</g:else> </span> <span class="voteCount"> ${r.noOfVotes} </span> 
					<div
					style="width:${(r.noOfVotes/totalVotes)*100}%" class="pollbar"></div>
			</div>


			<div class="thumbwrap">
				<g:link controller="sUser" action="show"
					id="${r.authors.getAt(0)?.id}">
					<img class="icon"
						src="${createLinkTo(file: r.authors.getAt(0)?.icon()?.fileName?.trim(), absolute:true)}"
						title="${r.authors.getAt(0)?.username}" />
				</g:link>
			</div>
			
			 <span class="iAgree" style="float: right;"> <g:remoteLink
					action="addAgreeRecommendationVote" controller="observation"
					params="['obvId':observationInstance.id, 'recoId':r.recoId, 'currentVotes':r.noOfVotes]"
					onSuccess="jQuery('#votes_${recoId}').html(data.votes);return false;"
					onFailure="if(XMLHttpRequest.status == 401 || XMLHttpRequest.status == 200) {
				    		show_login_dialog();
				    	} else {	    
				    		alert(errorThrown);
				    	}return false;">I agree</g:remoteLink>
		</span> <g:javascript>
	$(document).ready(function(){
		$('#voteCountLink_${recoId}').click(function() {
			$('#voteDetails_${recoId}').show();
		});

		$('#voteDetails_${recoId}').mouseout(function(){
			$('#voteDetails_${recoId}').hide();
		});
	});
</g:javascript>
		</li>
	</g:each>
</g:if>
<g:else>
	<g:message code="recommendations.zero.message" />
</g:else>



