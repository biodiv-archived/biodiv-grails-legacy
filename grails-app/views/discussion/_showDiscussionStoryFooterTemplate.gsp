<%@page import="species.utils.Utils"%>
<%@page import="species.participation.Checklists"%>


<style>
	.discussion .story-footer{
   		bottom:0px;
	}
    
    .discussion .selectable {
		margin-left:20px;
    }

</style>

<div class="story-footer">

    <g:if test="${showLike && !showDetails}">
    <div class="footer-item pull-left">
        <obv:like model="['resource':discussionInstance]"/>
    </div>	
    </g:if>

    <g:if test="${showDetails}">
    <div class="footer-item">
        <i class="icon-comment" title="${g.message(code:'showobservationstoryfooter.title.comments')}"></i>
        <span class="">${discussionInstance.fetchCommentCount()}</span>
    </div>
    </g:if>
 
    <g:if test="${showDetails}">
        <div class="footer-item">
            <i class="icon-eye-open" title="${g.message(code:'showobservationstoryfooter.title.page.views')}"></i>
            <span class="">${discussionInstance.getPageVisitCount()}</span>
        </div>
    </g:if>
    
     <g:if test="${showDetails}">
      <div class="footer-item"> 
     	<i class="icon-user" title="${g.message(code:'showobservationstoryfooter.title.discussions.participants')}"></i>
        <span class="">${discussionInstance.activeUserCount()}</span>
      </div>
    </g:if>
    
    <g:if test="${!hidePost}">
    	<uGroup:objectPost model="['objectInstance':discussionInstance, 'userGroup':userGroup, canPullResource:canPullResource]" />
    </g:if>	
	
</div>
