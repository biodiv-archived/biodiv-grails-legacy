<%@page import="species.participation.Observation"%>
<%@ page import="species.auth.SUser"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'SUser.label', default: 'SUser')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>
<link rel="stylesheet"
	href="${resource(dir:'css',file:'tagit/tagit-custom.css', absolute:true)}"
	type="text/css" media="all" />
<g:javascript src="tagit.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<style>
.userCreatedObservations .jcarousel-skin-ie7 .jcarousel-clip-horizontal
	{
	width: 100%;
}

.userCreatedObservations .jcarousel-skin-ie7 .jcarousel-container-horizontal
	{
	width: 100%;
}
</style>
</head>
<body>
	<div class="container_16 big_wrapper">
		<div class=" grid_16">
			<div class="body">

				<h1>
					${fieldValue(bean: SUserInstance, field: "name")}
					<sUser:ifOwns model="['user':SUserInstance]">
						<span style="font-size: 60%; float: right;"> <g:link
								controller="SUser" action="edit" id="${SUserInstance.id }">Edit<span
									class="ui-icon-edit" />
							</g:link> </span>
					</sUser:ifOwns>

				</h1>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<div class="span12 super-section" style="clear: both;">
					<div class="grid_4" style="width: 200px; padding: 0;">
						<div class="figure"
							style="float: left; max-height: 220px; max-width: 200px">
							<g:link controller="SUser" action="show"
								id="${SUserInstance.id }">
								<img class="normal_profile_pic" src="${SUserInstance.icon()}" />
							</g:link>

							<%--						<div class="prop">--%>
							<%--							<span class="name">Member since </span> <span class="value">--%>
							<%--							<sUser:showDate --%>
							<%--								model="['SUserInstance':SUserInstance, 'propertyName':'dateCreated']" />--%>
							<%--							</span>--%>
							<%--						</div>--%>
							<%----%>
							<%----%>
							<%--						<div class="prop">--%>
							<%--							<span class="name">Last visited </span> <span class="value">--%>
							<%--							<sUser:showDate --%>
							<%--								model="['SUserInstance':SUserInstance, 'propertyName':'lastLoginDate']" />--%>
							<%--							</span>--%>
							<%--						</div>--%>
							<%--					--%>

							<div class="prop">
								<span class="name">Member since </span> <span class="value">
									<g:formatDate format="yyyy-MM-dd"
										date="${SUserInstance.dateCreated}" type="datetime"
										style="MEDIUM" /> </span>
							</div>
							<div class="prop">
								<span class="name">Last visited </span> <span class="value">
									<g:formatDate format="yyyy-MM-dd"
										date="${SUserInstance.lastLoginDate}" type="datetime"
										style="MEDIUM" /> </span>
							</div>
						</div>

					</div>

					<div class="user_basic_info grid_11">

						<div class="prop">
							<span class="name"><g:message code="suser.username.label"
									default="Username" /> </span> <span class="value"> ${fieldValue(bean: SUserInstance, field: "username")}
							</span>
						</div>

						<sUser:ifOwnsOrIsPublic model="['user':SUserInstance, 'isPublic':!SUserInstance.hideEmailId]">
							<div class="prop">
								<span class="name"><g:message code="suser.email.label"
										default="Email" /> </span> <span class="value"> <a
									href="mailto:${fieldValue(bean: SUserInstance, field: 'email')}">
										${fieldValue(bean: SUserInstance, field: "email")} </a> </span>

							</div>
						</sUser:ifOwnsOrIsPublic>

						<div class="prop">
							<span class="name"><g:message code="suser.website.label"
									default="Website" /> </span> <span class="value"> <a
								target="blank"
								href="${fieldValue(bean: SUserInstance, field: 'website')}">
									${fieldValue(bean: SUserInstance, field: 'website')} </a> </span>
						</div>
						<div class="prop">
							<span class="name"><g:message code="suser.location.label"
									default="Location" /> </span> <span class="value"> ${fieldValue(bean: SUserInstance, field: "location")}
							</span>
						</div>

					</div>
				</div>

				<br />

				<g:if test="${SUserInstance.aboutMe }">
					<div class="span12 super-section" style="clear: both;">
						<h5>About Me</h5>
						${SUserInstance.aboutMe.encodeAsHTML().replace('\n', '<br/>\n')}
					</div>
					<br />
				</g:if>

				<g:if test="${SUserInstance.openIds}">
					<div class="span12 super-section" style="clear: both;">
						<h5>
							${SUserInstance.openIds?.size()}
							External Provider Identification
							<g:if test="${SUserInstance.openIds?.size()>1}">s</g:if>
						</h5>
						<g:each in="${SUserInstance.openIds}" var="openId">
							<g:if test="${openId.url.indexOf('facebook') != -1}">
								<a href="${openId.url}" target="blank">Facebook</a>
							</g:if>
							<g:elseif test="${openId.url.indexOf('google') != -1 }">
								<a href="${openId.url}" target="blank">Google</a>
							</g:elseif>
							<g:elseif test="${openId.url.indexOf('yahoo')  != -1}">
								<a href="${openId.url}" target="blank">Yahoo</a>
							</g:elseif>
							<g:else>
								<a href="${openId.url}" target="blank">openId.url</a>
							</g:else>
						</g:each>
					</div>

					<br />
				</g:if>

				<div class="span12 super-section" style="clear: both;">
					<h5>
						${Observation.countByAuthor(SUserInstance)}
						Observations
					</h5>
					<obv:showRelatedStory
						model="['controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'user', 'filterPropertyValue':SUserInstance.id, 'id':'a']" />
				</div>

				<br />

				<div class="span12 super-section" style="clear: both;">
					<obv:showAllTags
						model="['tagFilterByProperty':'User', 'tagFilterByPropertyValue':SUserInstance.id]" />
				</div>

			</div>
		</div>


	</div>
</body>

</html>
