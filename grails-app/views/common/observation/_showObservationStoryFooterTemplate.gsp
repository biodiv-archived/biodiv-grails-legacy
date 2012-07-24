<%@page import="species.utils.Utils"%>
<div class="story-footer" style="width: 100%">
	<div class="footer-item">
		<i class="icon-eye-open"></i>
		${observationInstance.getPageVisitCount()}

	</div>

	<div class="footer-item">
		<i class="icon-check"></i>
		${observationInstance.getRecommendationCount()}

	</div>

	<g:if test="${showDetails}">
	<div class="footer-item">
		<i class="icon-comment"></i>
		<fb:comments-count
			href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}"></fb:comments-count>

	</div>

	
		<div class="footer-item"">
			<obv:addFlag model="['observationInstance':observationInstance]" />
		</div>
	</g:if>
	<g:else>
		<g:if test="${observationInstance.flagCount>0}">
			<div id="show-flag-count" class="footer-item">
				<i class="icon-flag"></i>
				${observationInstance.flagCount}
			</div>
		</g:if>
	</g:else>

	<g:if test="${showDetails}">
		<div class="footer-item"">
			<obv:identificationByEmail
				model="['source':'observationShow', 'requestObject':request, 'cssClass':'btn btn-mini']" />
		</div>
	</g:if>
	<g:if test="${showDetails}">
		<div class="footer-item" style="width: 100px">
			<fb:like layout="button_count"
				href="${createLink(controller:'observation', action:'show', id:observationInstance.id, base:Utils.getDomainServerUrl(request))}"
				width="450" show_faces="true"></fb:like>
		</div>
	</g:if>

</div>