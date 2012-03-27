<html>
<%@ page import="org.codehaus.groovy.grails.plugins.PluginManagerHolder"%>

<sec:ifNotSwitched>
	<sec:ifAllGranted roles='ROLE_SWITCH_USER'>
		<g:if test='${user.username}'>
			<g:set var='canRunAs' value='${true}' />
		</g:if>
	</sec:ifAllGranted>
</sec:ifNotSwitched>

<head>
<meta name='layout' content='main' />
<g:set var="entityName"
	value="${message(code: 'user.label', default: 'User')}" />
<title><g:message code="default.edit.label" args="[entityName]" />
</title>
</head>

<body>

	<div class="container_16 big_wrapper">
		<div class=" grid_16">
			<div class="body">
				<h1>
					${fieldValue(bean: user, field: "name")}
					
					<span style="font-size: 60%;float:right;">
						<g:link controller="SUser" action="show" id="${user.id }">View my profile</g:link>
					</span>
			
				</h1>
				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>
					
			<g:form action="update" name='userEditForm' class="button-style">
				<g:hiddenField name="id" value="${user?.id}" />
				<g:hiddenField name="version" value="${user?.version}" />

				<%
def tabData = []
tabData << [name: 'userinfo', icon: 'icon_user', messageCode: 'spring.security.ui.user.info']
//tabData << [name: 'roles',    icon: 'icon_role', messageCode: 'spring.security.ui.user.roles']
boolean isOpenId = PluginManagerHolder.pluginManager.hasGrailsPlugin('springSecurityOpenid')
if (isOpenId) {
	tabData << [name: 'openIds', icon: 'icon_role', messageCode: 'spring.security.ui.user.openIds']
}
%>

				<s2ui:tabs elementId='tabs' height='375' data="${tabData}">

					<s2ui:tab name='userinfo' height='275'>
						<div class="snippet grid_4 tablet"
							style="width: 200px; padding: 0;">
							<div class="figure"
								style="float: left; max-height: 220px; max-width: 200px">
								<g:link controller="SUser" action="show" id="${user.id }">
									<img class="normal_profile_pic" src="${user.icon()}" />
								</g:link>
								<div class="prop">
									<span class="name">Member since </span> <span class="value">
										${fieldValue(bean: user, field: "dateCreated")} </span>
								</div>
								<div class="prop">
									<span class="name">Last visited </span> <span class="value">
										${fieldValue(bean: user, field: "lastLoginDate")} </span>
								</div>
							</div>

						</div>
						<div class="user_basic_info grid_10">
							<table>
								<tbody>
									<s2ui:textFieldRow name='email' bean="${user}"
										value="${user.email}" size='40' labelCode='user.email.label'
										labelCodeDefault='E-mail*' />

									<s2ui:textFieldRow name='name' labelCode='user.name.label'
										bean="${user}" size='40' labelCodeDefault='Full Name'
										value="${user.name}" />

									<s2ui:textFieldRow name='username'
										labelCode='user.username.label' bean="${user}" size='40'
										labelCodeDefault='Username' value="${user.username}" />

									<s2ui:textFieldRow name='website' bean="${user}"
										value="${user.website}" size='40'
										labelCode='user.website.label' labelCodeDefault='Website' />

									<s2ui:textFieldRow name='location' bean="${user}"
										value="${user.location}" size='40'
										labelCode='user.location.label' labelCodeDefault='Location' />

									<s2ui:textFieldRow name='timezone' bean="${user}"
										value="${user.timezone}" size='40'
										labelCode='user.timezone.label'
										labelCodeDefault='Timezone Offset' />
								</tbody>
							</table>
						</div>
					</s2ui:tab>

					<g:if test='${isOpenId}'>
						<s2ui:tab name='openIds' height='275'>
							<g:if test='${user?.openIds}'>
								<ul>
									<g:each var="openId" in="${user.openIds}">
										<li>
											${openId.url}
										</li>
									</g:each>
								</ul>
							</g:if>
							<g:else>
	No OpenIDs registered
	</g:else>
						</s2ui:tab>
					</g:if>

				</s2ui:tabs>

				<div style='float: left; margin-top: 10px;'>
					<s2ui:submitButton elementId='update' form='userEditForm'
						messageCode='default.button.update.label' />

					<g:if test='${user}'>
						<!--s2ui:deleteButton /-->
					</g:if>

					<g:if test='${canRunAs}'>
						<a id="runAsButton"> ${message(code:'spring.security.ui.runas.submit')}
						</a>
					</g:if>

				</div>

			</g:form>

			<g:if test='${user}'>
				<!-- s2ui:deleteButtonForm instanceId='${user.id}'/-->
			</g:if>

			<g:if test='${canRunAs}'>
				<form name='runAsForm'
					action='${request.contextPath}/j_spring_security_switch_user'
					method='POST'>
					<g:hiddenField name='j_username' value="${user.username}" />
					<input type='submit' class='s2ui_hidden_button' />
				</form>
			</g:if>
			</div>
		</div>
	</div>

	<script>
$(document).ready(function() {
	$('#username').focus();

	<s2ui:initCheckboxes/>

	$("#runAsButton").button();
	$('#runAsButton').bind('click', function() {
	   document.forms.runAsForm.submit();
	});
});
</script>

</body>
</html>
