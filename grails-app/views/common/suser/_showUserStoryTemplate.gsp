<%@page import="species.participation.Observation"%>
<%@page import="species.utils.Utils"%>
<div class="observation_story" style="overflow: auto;">
	<g:if test="${!showDetails }">
		<h5 class="ellipsis">
			<a
				href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
				${userInstance.name} </a>
		</h5>
	</g:if>

	<div class="pull-left" style="padding-bottom: 10px;width:100%;">
		<g:if test="${showDetails}">
			<div class="prop">
				<span class="name"><i class="icon-user"></i> <g:message
						code="suser.username.label" default="Username" /> </span>
				<div class="value">
					${fieldValue(bean: userInstance, field: "username")}
				</div>
			</div>

			<div class="prop">
				<span class="name"><i class="icon-user"></i> <g:message
						code="suser.name.label" default="Full Name" /> </span>
				<div class="value">
					${fieldValue(bean: userInstance, field: "name")}
				</div>
			</div>



			<sUser:ifOwnsOrIsPublic
				model="['user':userInstance, 'isPublic':!userInstance.hideEmailId]">
				<div class="prop">
					<span class="name"> <i class="icon-envelope"></i> <g:message
							code="suser.email.label" default="Email" /> </span>
					<div class="value">

						<a href="mailto:${fieldValue(bean: userInstance, field: 'email')}">
							${fieldValue(bean: userInstance, field: "email")} </a>
					</div>
				</div>
			</sUser:ifOwnsOrIsPublic>

		</g:if>
		<g:if test="${userInstance.location}">
			<div class="prop">
				<span class="name"><i class="icon-map-marker"></i>Location</span>
				<div class="value">
					${userInstance.location}
				</div>
			</div>
		</g:if>

		<g:if test="${userInstance.website}">
			<div class="prop">
				<span class="name"><i class="icon-road"></i>Website</span>
				<div class="value">
					<div class="linktext pull-left">
						${fieldValue(bean: userInstance, field: 'website')}
					</div>
					<% def openId = userInstance.openIds.find { it.url.indexOf('facebook') != -1 }
									def facebookUrl = openId?.url %>
					<g:if test="${facebookUrl}">
						<div class="facebookButton pull-left"
							style="background-repeat: no-repeat; height: 33px;">
							<a class="fbJustConnect" target="_blank" href="${facebookUrl}">Facebook
								Profile</a>
						</div>
					</g:if>
				</div>
			</div>
		</g:if>

		<g:if test="${!showDetails }">
			<div class="prop">
				<span class="name"><i class="icon-time"></i>Member since </span>
				<div class="value">
					<time class="timeago"
						datetime="${userInstance.dateCreated.getTime()}"></time>
				</div>
			</div>
			<g:if test="${userInstance.lastLoginDate}">
				<div class="prop">
					<span class="name"><i class="icon-time"></i>Last visited </span>
					<div class="value">
						<time class="timeago"
							datetime="${userInstance.lastLoginDate.getTime()}"></time>
					</div>
				</div>
			</g:if>
		</g:if>
	</div>
        <g:if test="${!showDetails }">
            <obv:getStats model="['user':userInstance, 'userGroup':userGroupInstance]"/>
        </g:if>
</div>





