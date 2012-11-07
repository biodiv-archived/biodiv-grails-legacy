<%@page import="species.utils.Utils"%>
<div class="story-footer" style="width: 100%">
	<g:if test="${showDetails}">
		<div class="footer-item">
			<i class="icon-eye-open"></i>
			${observationInstance.getPageVisitCount()}
		</div>
		<div class="footer-item">
			<i class="icon-check"></i>
			${observationInstance.getRecommendationCount()}
	
		</div>
		<div class="footer-item">
			<i class="icon-comment"></i>
			${observationInstance.fetchCommentCount()}
		</div>
		<div class="footer-item"">
				<obv:addFlag model="['observationInstance':observationInstance]" />
		</div>
		<div class="footer-item"">
			<obv:identificationByEmail
				model="['source':'observationShow', 'requestObject':request, 'cssClass':'btn btn-mini']" />
		</div>
		<div class="footer-item" style="width: 100px">
			<fb:like layout="button_count"
				href="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}"
				width="450" show_faces="true"></fb:like>
		</div>
	</g:if>
</div>
