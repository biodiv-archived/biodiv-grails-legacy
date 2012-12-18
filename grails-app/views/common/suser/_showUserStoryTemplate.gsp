<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
<div class="observation_story" style="display: inline-block">
	<h5>
		<a
			href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
			${userInstance.name} </a>
	</h5>

	<dl class="dl-horizontal" style="padding-bottom:10px;">
		<g:if test="${userInstance.location}">
			<dt>
				<i class="icon-map-marker"></i>
				${userInstance.location}
			</dt>
			<dd></dd>
		</g:if>

		<g:if test="${userInstance.website}">
			<dt>
				<i class="icon-road"></i>
				<g:if test="${Utils.isURL(userInstance.website) }">
					<a target="_blank" href="${userInstance.getWebsiteLink()}"> ${fieldValue(bean: userInstance, field: 'website')}
					</a>
				</g:if>
				<g:else>
					${fieldValue(bean: userInstance, field: 'website')}
				</g:else>
			</dt>
			<dd></dd>
		</g:if>

		<dt>
			<i class="icon-time"></i>Member since
		</dt>
		<dd>
			<time class="timeago"
					datetime="${userInstance.dateCreated.getTime()}"></time>
			
		</dd>

		<dt>
			<i class="icon-time"></i>Last visited
		</dt>
		<dd>
			<time class="timeago"
					datetime="${userInstance.lastLoginDate.getTime()}"></time>

		</dd>
	</dl>
	<div class="story-footer">
		<span class="footer-item" title="No of Observations"> <i
			class="icon-screenshot"></i> <obv:showNoOfObservationsOfUser
				model="['user':userInstance]" /> </span> <span class="footer-item"
			title="No of Tags"> <i class="icon-tags"></i> <obv:showNoOfTagsOfUser
				model="['userId':userInstance.id]" /> </span> <span class="footer-item"
			title="No of Identifications"> <i class="icon-check"></i> <obv:showNoOfRecommendationsOfUser
				model="['user':userInstance]" /> </span>
	</div>

</div>


