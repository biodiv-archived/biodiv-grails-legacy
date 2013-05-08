<%@page import="species.utils.Utils"%>
<div class="story-footer" style="width: 100%">
        <div class="footer-item">
            <obv:like model="['resource':observationInstance]"/>
        </div>


   	<g:if test="${showDetails}">
		<div class="footer-item">
			<i class="icon-eye-open" title="No of page views"></i>
                        <span class="badge">${observationInstance.getPageVisitCount()}</span>
		</div>
		<div class="footer-item">
			<i class="icon-check" title="No of name suggestions"></i>
                        <span class="badge">${observationInstance.getRecommendationCount()}</span>
	
		</div>
		<div class="footer-item">
			<i class="icon-comment" title="No of comments"></i>
                        <span class="badge">${observationInstance.fetchCommentCount()}</span>
		</div>
		<div class="footer-item"">
				<obv:addFlag model="['observationInstance':observationInstance]" />
		</div>
		<div class="footer-item"">
			<obv:identificationByEmail
				model="['source':'observationShow', 'requestObject':request, 'cssClass':'btn btn-mini']" />
		</div>
		<!--div class="footer-item">
			<fb:like layout="button_count"
				href="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}"
				width="450" show_faces="true"></fb:like>
		</div-->
	</g:if>
</div>
