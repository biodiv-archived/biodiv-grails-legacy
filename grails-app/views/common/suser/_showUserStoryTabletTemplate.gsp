<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
<div>
	<h5 style="margin:0">
		<a class="ellipsis" href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}"> ${userInstance.name}
		</a>
	</h5>
<%--	<div class="icons-bar">--%>
<%--		<div class="observation-icons"></div>--%>
<%--	</div>--%>
<%----%>
<%--	<div class="story">--%>
<%--		<g:if test="${userInstance.location}">--%>
<%--			<div>--%>
<%--				<i class="icon-map-marker"></i>--%>
<%--				${userInstance.location}--%>
<%--			</div>--%>
<%--		</g:if>--%>
<%----%>
<%--		<g:if test="${userInstance.website}">--%>
<%--			<div>--%>
<%--				<i class="icon-road"></i>--%>
<%--				<g:if test="${Utils.isURL(userInstance.website) }">--%>
<%--					<a target="_blank"--%>
<%--						href="${userInstance.getWebsiteLink()}"> ${fieldValue(bean: userInstance, field: 'website')}--%>
<%--					</a>--%>
<%--				</g:if><g:else>--%>
<%--					${fieldValue(bean: userInstance, field: 'website')}--%>
<%--				</g:else>--%>
<%--			</div>--%>
<%--		</g:if>--%>
<%----%>
<%--	</div>--%>

	<div class="story-footer">
		<span class="footer-item" title="No of Observations">
			<i class="icon-screenshot"></i>
			<obv:showNoOfObservationsOfUser model="['user':userInstance]"/>
		</span>

		<span class="footer-item" title="No of Tags">
			<i class="icon-tags"></i>
			<obv:showNoOfTagsOfUser model="['userId':userInstance.id]" />
		</span>
		
		<span class="footer-item" title="No of Identifications">
					<i class="icon-check"></i>
					<obv:showNoOfRecommendationsOfUser model="['user':userInstance]" />
		</span>
	</div>
</div>


