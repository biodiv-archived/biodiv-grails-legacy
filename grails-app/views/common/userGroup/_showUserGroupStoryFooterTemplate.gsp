<%@page import="species.utils.Utils"%>
<div class="story-footer" style="width: 100%">
	<div class="footer-item">
		<i class="icon-eye-open"></i>
		${userGroupInstance.getPageVisitCount()}

	</div>

	<g:if test="${showDetails}">
		<div class="footer-item">
			<i class="icon-comment"></i>
			<fb:comments-count
				href="${createLink(controller:'userGroup', action:'show', id:userGroupInstance.id, base:Utils.getDomainServerUrl(request))}"></fb:comments-count>
	
		</div>
	</g:if>
	<g:else>
	</g:else>

	<g:if test="${showDetails}">
		<div class="footer-item"">
			<obv:identificationByEmail
				model="['source':'userGroupShow', 'requestObject':request]" />
		</div>
	</g:if>
	<g:if test="${showDetails}">
		<div class="footer-item" style="width: 100px">
			<fb:like layout="button_count"
				href="${createLink(controller:'userGroup', action:'show', id:userGroupInstance.id, base:Utils.getDomainServerUrl(request))}"
				width="450" show_faces="true"></fb:like>
		</div>
	</g:if>

</div>