<%@page import="species.utils.Utils"%>
<div class="story-footer" style="width: 100%">
	<g:if test="${showLike && !showDetails}">
	        <div class="footer-item pull-left">
	            <obv:like model="['resource':observationInstance]"/>
	        </div>	
	</g:if>
		<div class="footer-item">
			<i class="icon-comment" title="No of comments"></i>
                        <span class="">${observationInstance.fetchCommentCount()}</span>
		</div>
   	<g:if test="${showDetails}">
		<div class="footer-item">
			<i class="icon-eye-open" title="No of page views"></i>
                        <span class="">${observationInstance.getPageVisitCount()}</span>
		</div>
		<div class="footer-item">
			<i class="icon-check" title="No of name suggestions"></i>
                        <span class="">${observationInstance.getRecommendationCount()}</span>
	
		</div>
	
	</g:if>


</div>
