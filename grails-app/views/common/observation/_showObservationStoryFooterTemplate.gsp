<%@page import="species.utils.Utils"%>
<div class="story-footer">
	<g:if test="${showLike && !showDetails}">
	        <div class="footer-item pull-left">
	            <obv:like model="['resource':observationInstance]"/>
	        </div>	
	</g:if>
		<div class="footer-item">
			<i class="icon-comment" title="Comments"></i>
                        <span class="">${observationInstance.fetchCommentCount()}</span>
		</div>
		<div class="footer-item">
		<g:if test="${!observationInstance.isChecklist}">
			<i class="icon-check" title="Species calls"></i>
            <span class="">${observationInstance.fetchRecoVoteOwnerCount()}</span>
        </g:if>
		</div>

   	<g:if test="${showDetails}">
		<div class="footer-item">
			<i class="icon-eye-open" title="Page views"></i>
                        <span class="">${observationInstance.getPageVisitCount()}</span>
		</div>
	
	</g:if>


</div>
