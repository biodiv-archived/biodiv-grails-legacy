<%@page import="species.participation.Observation"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.Recommendation"%>

<g:if test="${result.size() > 0 }">
<g:each in="${result}" var="r">
<li class="reco_block ${r.maxVotedSpeciesName?'max_voted_species_name':''}">
<div class="">
    <div class="users">
        <g:if test="${!hideAgree}">
        <div class="iAgree ">
            <g:if test="${!r.disAgree}">
            <button class="btn btn-primary btn-small nameAgree ${r.isLocked?' disabled ': ''}" style="margin-left: 1px;" onclick="addAgreeRecoVote(${r.obvId}, ${r.recoId}, ${r.noOfVotes}, $(this).closest('li'), '${uGroup.createLink(controller:'observation', action:'addAgreeRecommendationVote')}', this); return true;"><g:message code="button.agree" /></button>
            </g:if>
            <g:else>
            <button class="btn btn-primary btn-small nameRemove ${r.isLocked?' disabled ': ''}" style="margin-left: 1px;" onclick="removeRecoVote(${r.obvId}, ${r.recoId}, '${uGroup.createLink(controller:'observation', action:'removeRecommendationVote')}', this); return true;"><g:message code="button.remove" /></button>
            </g:else>
        </div>


        </g:if>
        <g:each in="${r.authors}" var="author">
        <a href="${uGroup.createLink(controller:'user', action:'show', id:author?.id)}" title="${author?.name }">
            <img class="small_profile_pic"
            src="${author?.profilePicture(ImageType.SMALL)}"
            title="${author.name}" />
        </a>
        </g:each>

        <sUser:hasObvLockPerm model="['obvId': r.obvId]">
        <%
            def lockButton
            if(r.showLock){
                lockButton = g.message(code:"button.lock")
            }
            else{
                lockButton = g.message(code:"button.unlock")
            }
        %>
        <a class="lockObvId pull-right btn btn-primary btn-small ${(lockButton == 'Lock' && r.isLocked)?' disabled ': ''}" style="margin-left: 1px; background: orangered;"
        onclick="lockObv('${uGroup.createLink(controller:'observation', action:'lock', id:observationInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}', '${lockButton}', ${r.recoId}, ${r.obvId}, this )">
        <i class="icon-lock"></i>${lockButton}</a>
    </sUser:hasObvLockPerm>


    </div>    

    <g:if test="${r.observationImage}">
    <a href="${uGroup.createLink([action:"show", controller:"observation", id:r.obvId, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">

        <img style="width: 75px; height: 75px;"
        src="${r.observationImage}">
    </a>
    </g:if>

<!--    <span class="voteCount">
        <span id="votes_${r.recoId}">
            ${r.noOfVotes} 
        </span> 
    <g:if test="${r.noOfVotes <= 1}"><g:message code="text.user.thinks" /> </g:if>
    <g:else> <g:message code="text.user.thinks" /></g:else> 
    <g:message code="text.it.is" />:</span>
-->
    <span class="highlight">
        <g:if test="${r.speciesId}">
        <a href="${uGroup.createLink(action:'show', controller:'species', id:r.speciesId, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
            <i> ${r.name} </i>
        </a>
        </g:if>
        <g:elseif test="${r.isScientificName}">
        <i>${r.name}</i>
        </g:elseif>
        <g:else>
        ${r.name}
        </g:else>${r.commonNames}</span>
        <comment:showCommentPopup model="['commentHolder':Recommendation.read(r.recoId), 'rootHolder':r.observationInstance?:observationInstance]" />
        <%--				<obv:showRecoComment--%>
        <%--					model="['recoComments':r.recoComments, 'recoId': r.recoId]" />--%>

    </div> 
    
    <script type="text/javascript">
    $(document).ready(function(){
    $('#voteCountLink_${r.recoId}').click(function() {
    $('#voteDetails_${r.recoId}').show();
    });

    $('#voteDetails_${r.recoId}').mouseout(function(){
    $('#voteDetails_${r.recoId}').hide();
    });
    });

    </script></li>
    </g:each>
    </g:if>
    <%--<g:else>--%>
    <%--	<g:message code="recommendations.zero.message" />--%>
    <%--</g:else>--%>



