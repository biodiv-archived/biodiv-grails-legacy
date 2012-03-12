<!--link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'pollBars.css', absolute:true)}" /-->

<style>
.reco_block {
background-color:#ffffff;
padding:5px;
margin-bottom:3px;
border-radius: 5px;
}
.reco {
background-color: #ffffff;        
padding: 3px;
width: 80%;         
float:left;         
}
.iAgree {
float:right;
background-color: #F8F8F8;
background-image: -moz-linear-gradient(center top , #F8F8F8, #ECECEC);
border: 1px solid #C6C6C6;
border-radius: 2px 2px 2px 2px;
display: block;
padding:3px;         
}
.users{
clear:both;
}
.name {
font-weight: bold;
}
</style>

<g:if test="${result.size() > 0 }">
	<g:each in="${result}" var="r">
		<li class="reco_block">
                        <div>
                            <span class="voteCount">${r.noOfVotes}<g:if test="${r.noOfVotes <= 1}"> user</g:if>
                            <g:else> users</g:else> think it is:</span> 
			    <div style="width:${(r.noOfVotes/totalVotes)*100}%" class="pollbar"></div>
                         </div>
			<div class="reco">
				<span class="name"> <g:if test="${r.canonicalForm}">
						<g:link controller="species" action="show" id="${r.speciesId}">
							${r.canonicalForm}
						</g:link>
					</g:if> <g:else>
						${r.name}
					</g:else> </span> 
			</div>
	                <div class="iAgree"> <g:remoteLink
					action="addAgreeRecommendationVote" controller="observation"
					params="['obvId':observationInstance.id, 'recoId':r.recoId, 'currentVotes':r.noOfVotes]"
					onSuccess="jQuery('#votes_${recoId}').html(data.votes);return false;"
					onFailure="if(XMLHttpRequest.status == 401 || XMLHttpRequest.status == 200) {
				    		show_login_dialog();
				    	} else {	    
				    		alert(errorThrown);
				    	}return false;">Agree</g:remoteLink>
		        </div> 


			<div class="users thumbwrap">
				<g:link controller="sUser" action="show"
					id="${r.authors.getAt(0)?.id}">
					<img class="icon"
						src="${createLinkTo(file: r.authors.getAt(0)?.icon()?.fileName?.trim(), absolute:true)}"
						title="${r.authors.getAt(0)?.username}"/>${r.authors.getAt(0)?.username}
				</g:link>
			</div>

                            

			
		                        <g:javascript>
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



