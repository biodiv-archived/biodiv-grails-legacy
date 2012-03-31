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

					<span style="font-size: 60%; float: right;"> <g:link
							controller="SUser" action="show" id="${user.id }">View my profile</g:link>
					</span>

				</h1>
				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>

				<g:form action="update" name='userEditForm' class="button-style updateForm">
					<g:hiddenField name="id" value="${user?.id}" />
					<g:hiddenField name="version" value="${user?.version}" />


					<div class="grid_4"
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
								<s2ui:textFieldRow name='username'
									labelCode='user.username.label' bean="${user}" size='40'
									labelCodeDefault='Username' value="${user.username}" />

								<s2ui:textFieldRow name='name' labelCode='user.name.label'
									bean="${user}" size='40' labelCodeDefault='Full Name'
									value="${user.name}" />

								<s2ui:textFieldRow name='email' bean="${user}"
									value="${user.email}" size='40' labelCode='user.email.label'
									labelCodeDefault='E-mail*' readonly />
											
								<s2ui:textFieldRow name='website' bean="${user}"
									value="${user.website}" size='40'
									labelCode='user.website.label' labelCodeDefault='Website' />

								<s2ui:textFieldRow name='location' bean="${user}"
									value="${user.location}" size='40'
									labelCode='user.location.label' labelCodeDefault='Location' />


							</tbody>
						</table>
					</div>
					
					<div class="span12 super-section" style="clear:both;">
						<h5>About Me</h5>
						<textarea cols='100' rows='3' style="width:100%" name="aboutMe" id="aboutMe">${user.aboutMe }</textarea>
					</div>

					<div class="span12 super-section" style="clear:both;">
						<h5>Settings</h5>
						<div>
							<div>
								<g:checkBox name="sendNotification" value="${user.sendNotification}" />
								<label for="sendNotification"><g:message code='user.sendNotification.label' default='Send me email notifications'/></label>
							</div>
							<div>
								<g:checkBox name="hideEmailId" value="${user.hideEmailId}" />
								<label for="hideEmailId"><g:message code='user.hideEmailId.label' default='Hide my email if from others'/></label>
							</div>
							
						</div>						
					</div>

					
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
