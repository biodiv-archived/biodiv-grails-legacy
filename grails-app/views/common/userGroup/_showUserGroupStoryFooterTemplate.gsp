<%@page import="species.utils.Utils"%>
<div class="story-footer">
	<div class="footer-item">
		<i class="icon-eye-open"></i>
		${userGroupInstance.getPageVisitCount()}

	</div>

	<g:if test="${showDetails}">
		<div class="footer-item">
			<i class="icon-comment"></i>
			<fb:comments-count
				href="${createLink(mapping:'userGroup', action:'show', params:"['webaddress':userGroupInstance.webaddress]", base:Utils.getDomainServerUrl(request))}"></fb:comments-count>
	
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
				href="${createLink(mapping:'userGroup', action:'show', params:"['webaddress':userGroupInstance.webaddress]", base:Utils.getDomainServerUrl(request))}"
				width="450" show_faces="true"></fb:like>
		</div>
	</g:if>

</div>
