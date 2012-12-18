<%@page import="species.groups.UserGroup"%>
<%@page import="species.utils.ImageType"%>
<%@page import="species.participation.Observation"%>
<div class="signature">
		<div class="figure user-icon" style="float:left;">
			<a href="${uGroup.createLink( 'controller':'SUser', action:'show', id:userInstance.id, userGroup:userGroup, 'userGroupWebaddress':userGroupWebaddress)}"> <img
				style="float: left;" src="${userInstance.icon(ImageType.SMALL)}"
				class="small_profile_pic" title="${userInstance.name}" /></a>
		</div>
		<div class="story" style="margin-left:35px">
			<a href="${uGroup.createLink('controller':'SUser', action:'show', id:userInstance.id,  userGroup:userGroup, 'userGroupWebaddress':userGroupWebaddress)}"> ${userInstance.name} </a>
			
<%--			<g:if test="${userInstance.location}">--%>
<%--				<div>--%>
<%--					<i class="icon-map-marker"></i>--%>
<%--					${userInstance.location}--%>
<%--				</div>--%>
<%--			</g:if>--%>

			<div class="story-footer" style="position:static;">
				<div class="footer-item" title="No of Observations">
					<i class="icon-screenshot"></i>
					<obv:showNoOfObservationsOfUser model="['user':userInstance]"/>
				</div>

<%--				<div class="footer-item" title="No of Tags">--%>
<%--					<i class="icon-tags"></i>--%>
<%--					<obv:showNoOfTagsOfUser model="['userId':userInstance.id]" />--%>
<%--				</div>--%>
				
				<div class="footer-item" title="No of Identifications">
					<i class="icon-check"></i>
					<obv:showNoOfRecommendationsOfUser model="['user':userInstance]" />
				</div>
<%--				--%>
<%--				<div class="footer-item" title="No of Identifications">--%>
<%--					<i class="icon-comment"></i>--%>
<%--					${userInstance.fetchCommentCount()}--%>
<%--				</div>--%>
				
			</div>

		</div>

	</div>
