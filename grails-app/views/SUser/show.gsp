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
					<span style="font-size: 60%;float:right;">
						<g:link controller="SUser" action="edit" id="${SUserInstance.id }">Edit<span class="ui-icon-edit" /></g:link>
					</span>
				</sUser:ifOwns>
					
				</h1>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>
			</div>

			<div class="snippet grid_4 tablet" style="width: 200px; padding: 0;">
				<div class="figure"
					style="float: left; max-height: 220px; max-width: 200px">
					<g:link controller="SUser" action="show" id="${SUserInstance.id }">
						<img class="normal_profile_pic" src="${SUserInstance.icon()}" />
					</g:link>
					<div class="prop">
						<span class="name">Member since </span> <span class="value">
							<g:formatDate format="yyyy-MM-dd" date="${SUserInstance.dateCreated}" type="datetime" style="MEDIUM"/>
						</span>
					</div>
					<div class="prop">
						<span class="name">Last visited </span> <span class="value">
							<g:formatDate format="yyyy-MM-dd" date="${SUserInstance.lastLoginDate}" type="datetime" style="MEDIUM"/>
						</span>
					</div>
				</div>

			</div>

			<div class="user_basic_info grid_11">

				<div class="prop">
					<span class="name"><g:message code="resource.username.label"
							default="Username" /> </span> <span class="value"> ${fieldValue(bean: SUserInstance, field: "username")}
					</span>
				</div>
				<div class="prop">
					<span class="name"><g:message code="resource.email.label"
							default="Email" /> </span> <span class="value"> <a href="mailto:${fieldValue(bean: SUserInstance, field: 'email')}">${fieldValue(bean: SUserInstance, field: "email")}</a>
					</span>
				</div>

				<div class="prop">
					<span class="name"><g:message code="resource.website.label"
							default="Website" /> </span> <span class="value"> <a href="${fieldValue(bean: SUserInstance, field: 'website')}">${fieldValue(bean: SUserInstance, field: 'website')}</a>
					</span>
				</div>
				<div class="prop">
					<span class="name"><g:message code="resource.location.label"
							default="Location" /> </span> <span class="value"> ${fieldValue(bean: SUserInstance, field: "location")}
					</span>
				</div>
				<div class="prop">
					<span class="name"><g:message code="resource.timezone.label"
							default="Timezone Offset" /> </span> <span class="value"> ${fieldValue(bean: SUserInstance, field: "timezone")}
					</span>
				</div>
				<div class="prop">
					<span class="name"><g:message code="resource.aboutMe.label"
							default="About Me" /> </span> <span class="value"> ${fieldValue(bean: SUserInstance, field: "aboutMe")}
					</span>
				</div>


			</div>
			
			<br/><br/>
			
			<div class="grid_15 userCreatedObservations" style="clear: both">
				<h5>${Observation.countByAuthor(SUserInstance)} Observations</h5>
				<obv:showRelatedStory
					model="['controller':'observation', 'action':'getRelatedObservation', 'filterProperty': 'user', 'filterPropertyValue':SUserInstance.id, 'id':'a']" />
			</div>

			<div class="tags_section grid_15">
				<obv:showAllTags  model="['tagFilterByProperty':'User', 'tagFilterByPropertyValue':SUserInstance.id]" />
			</div>
		</div>


	</div>
</body>

</html>
