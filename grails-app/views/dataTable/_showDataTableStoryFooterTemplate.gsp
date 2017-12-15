<%@page import="species.utils.Utils"%>
<%@page import="species.License"%>
    
<div class="story-footer">

    <g:if test="${showLike && !showDetails}">
    <div class="footer-item pull-left">
        <obv:like model="['resource':instance]"/>
    </div>	
    </g:if>

    <g:if test="${showDetails}">
    <div class="footer-item">
        <i class="icon-comment" title="${g.message(code:'showobservationstoryfooter.title.comments')}"></i>
        <span class="">${instance.fetchCommentCount()}</span>
    </div>
    </g:if>

    <%License l =instance?.access?.licenseId ? License.read(instance?.access?.licenseId):null;%>
    <g:if test="${l}">
    <div class="footer-item"> 
    <asset:image src="/all/license/${l?.name?.getIconFilename().toLowerCase()+'.png'}" absolute="true" title="${l?.name}" />
    </div>
    </g:if>

  
   
    <g:if test="${!hidePost}">
    	<uGroup:objectPost model="['objectInstance':instance, 'userGroup':userGroup, canPullResource:canPullResource]" />
    </g:if>	
	
</div>
