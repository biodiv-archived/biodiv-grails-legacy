<%@page import="species.utils.Utils"%>
<%@page import="species.participation.Checklists"%>

    
<div class="story-footer">

    <g:if test="${showLike && !showDetails}">
    <div class="footer-item pull-left">
        <obv:like model="['resource':observationInstance]"/>
    </div>	
    </g:if>

    <g:if test="${showDetails}">
    <div class="footer-item">
        <i class="icon-comment" title="${g.message(code:'showobservationstoryfooter.title.comments')}"></i>
        <span class="">${observationInstance.fetchCommentCount()}</span>
    </div>
    </g:if>
    <div class="footer-item">
        <g:if test="${!observationInstance.isChecklist}">
        <i class="icon-check" title="${g.message(code:'showobservationstoryfooter.title.species.calls')}"></i>
        <span class="">${observationInstance.fetchRecoVoteOwnerCount()}</span>
        </g:if>
    </div>

    <g:if test="${showDetails}">
        <div class="footer-item">
            <i class="icon-eye-open" title="${g.message(code:'showobservationstoryfooter.title.page.views')}"></i>
            <span class="">${observationInstance.getPageVisitCount()}</span>
        </div>
        <g:if test="${observationInstance.isChecklist}">
            <div class="footer-item"> 
                <i class="icon-screenshot" title="${g.message(code:'showobservationstoryfooter.title.species')}"></i>
                <span class="">${observationInstance.speciesCount}</span>
            </div>

            <div class="footer-item"> 
                <img src="${resource(dir:'images/license',file:observationInstance?.license?.name?.getIconFilename()+'.png', absolute:true)}"
                title="${observationInstance.license.name}"/>
            </div>
        </g:if>
        
    </g:if>
    <g:if test="${!hidePost}">
    	<uGroup:objectPost model="['objectInstance':observationInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
    </g:if>	
	
    <g:if test="${!showDetails}">
        <g:render template="/common/observation/noOfResources" model="['instance':observationInstance]"/>
    </g:if>
</div>
