<html>

<head>
<g:set var="title" value="${g.message(code:'title.value.register')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
</title>

<r:require modules="auth" />
<style>
.openid-loginbox .form-horizontal .control-label {
	width: 120px;
}

.openid-loginbox .form-horizontal .controls {
	margin-left: 140px;
}

.openid-loginbox .form-horizontal .controls input {
	width: 290px;
}
</style>
</head>

<body>

	<div class="openid-loginbox super-section">
		<g:if test="${flash.error}">
			<div class="alert alert-error">
				${flash.error}
			</div>
		</g:if>

		<div id='formLogin'>
			<fieldset>
				<legend>
					<g:message code="spring.security.ui.register.description"
						default="Create Account" />
				</legend>

				<g:if test='${emailSent}'>
					<div class="alert alert-success">
						<g:message code='spring.security.ui.register.sent' />
					</div>
				</g:if>
				<g:else>

					<div class="control-group"
						style="clear: both; float: left; line-height: 40px;"><g:message code="loginformtemplate.using" />:</div>
					<div class="control_group">
						<auth:externalAuthProviders
							model='["openidIdentifier":openidIdentifier]' />
					</div>
					<form
						action="${uGroup.createLink(controller:'register', action:'register', userGroupWebaddress:params.webaddress)}"
						name='registerForm' method="POST" class="form-horizontal">
						<div class="control-group"
							style="clear: both; border-top: 1px solid #Eee; padding-top: 5px;"><g:message code="loginformtemplate.or" />,
							<g:message code="default.register.here.label" /> </div>
						<div
							class="control-group ${hasErrors(bean: command, field: 'email', 'error')}">
							<label class="control-label" for="email"><g:message
									code='user.email.label' default='E-mail *' /> </label>
							<div class="controls">
								<input
									class="input-large focused ${(command.openId)?'readonly':''} "
									id="email" type="text" value="${command.email}" name="email"
									placeholder="${g.message(code:'placeholder.enter.emailid')}"
									${(command.openId)?'readonly':''}>

								<g:hasErrors bean="${command}" field="email">
									<div class="help-inline">
										<g:renderErrors bean="${command}" field="email" />
									</div>
								</g:hasErrors>
							</div>
						</div>


						<g:if test="${!command.openId}">
							<div
								class="control-group ${hasErrors(bean: command, field: 'password', 'error')}">
								<label class="control-label" for="password"><g:message
										code='user.password.label' default='${g.message(code:"register.password.label")}' /> </label>
								<div class="controls">
									<input class="input-large" id="password" type="password"
										value="${command.password}" name="password"
										placeholder="${g.message(code:'placeholder.enter.password')}">

									<g:hasErrors bean="${command}" field="password">
										<div class="help-inline">
											<g:renderErrors bean="${command}" field="password" />
										</div>
									</g:hasErrors>
								</div>
							</div>
							<div
								class="control-group ${hasErrors(bean: command, field: 'password2', 'error')}">
								<label class="control-label" for="password2"><g:message
										code='user.password2.label' default='${g.message(code:"register.password.again.label")}' /> </label>
								<div class="controls">
									<input class="input-large" id="password2" type="password"
										value="${command.password2}" name="password2"
										placeholder="${g.message(code:'register.placeholder.enter.pwdagain')}">

									<g:hasErrors bean="${command}" field="password2">
										<div class="help-inline">
											<g:renderErrors bean="${command}" field="password2" />
										</div>
									</g:hasErrors>

								</div>
							</div>

						</g:if>

						<div
							class="control-group ${hasErrors(bean: command, field: 'name', 'error')}">
							<label class="control-label" for="name"><g:message
									code='user.name.label' default='${g.message(code:"default.name.label")}' /> </label>
							<div class="controls">
								<input class="input-large" id="name" type="text"
									value="${command.name}" name="name"
									placeholder="${g.message(code:'register.placeholder.enter.name')}">

								<g:hasErrors bean="${command}" field="name">
									<div class="help-inline">
										<g:renderErrors bean="${command}" field="name" />
									</div>
								</g:hasErrors>

							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: command, field: 'location', 'error')}">
							<label class="control-label" for="location"><g:message
									code='user.location.label' default='${g.message(code:"default.location.label")}' /> </label>
							<div class="controls">
								<input class="input-large" id="location" type="text"
									value="${command.location}" name="location"
									placeholder="${g.message(code:'register.placeholder.enter.location')}">

								<g:hasErrors bean="${command}" field="location">
									<div class="help-inline">
										<g:renderErrors bean="${command}" field="location" />
									</div>
								</g:hasErrors>

							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: command, field: 'captcha_response', 'error')}">
							<label class="control-label" for="captcha_response"><jcaptcha:jpeg
									name="imageCaptcha" height="100px" width="100px" />
							</label>

							<div class="controls">

								<input class="input-large" id="captcha_response" type="text"
									value="" name="captcha_response"
									placeholder="${g.message(code:'register.placeholder.enter.wordshown')}">

								<g:hasErrors bean="${command}" field="captcha_response">
									<div class="help-inline">
										<g:renderErrors bean="${command}" field="captcha_response" />
									</div>
								</g:hasErrors>

							</div>
						</div>

						<div class="control-group">
							<span class="policy-text"> <g:message code="register.index.policy" />
								<a href="/terms"><g:message code="link.terms.conditions" /></a><g:message code="register.index.use.of.site" /> 
							</span>
							<s2ui:submitButton elementId='createButton' form='registerForm'
								messageCode='spring.security.ui.login.register'
								class="btn btn-primary" />
						</div>




						<table style="display: none">
							<g:if test="${command.openId}">
								<s2ui:passwordFieldRow name='password'
									labelCode='user.password.label' bean="${command}" size='25'
									labelCodeDefault='Password*' value="dummyPassword" />

								<s2ui:passwordFieldRow name='password2'
									labelCode='user.password2.label' bean="${command}" size='25'
									labelCodeDefault='Password (again)*' value="dummyPassword" />

								<s2ui:textFieldRow name='facebookUser'
									labelCode='user.facebookUser.label' bean="${facebookUser}"
									size='25' labelCodeDefault='facebookUser'
									value="${command.facebookUser}" />
							</g:if>
							<s2ui:textFieldRow name='username'
								labelCode='user.username.label' bean="${command}" size='25'
								labelCodeDefault='Username' value="${command.username}" />

							<s2ui:textFieldRow name='website' bean="${command}"
								value="${command.website}" size='25'
								labelCode='user.website.label' labelCodeDefault='Website' />

							<s2ui:textFieldRow name='timezone' bean="${command}"
								value="${command.timezone}" size='25'
								labelCode='user.timezone.label'
								labelCodeDefault='Timezone Offset' />

							<s2ui:textFieldRow name='aboutMe' bean="${command}"
								value="${command.aboutMe}" size='25'
								labelCode='user.aboutMe.label' labelCodeDefault='About Me' />

							<s2ui:textFieldRow name='profilePic' bean="${command}"
								value="${command.profilePic}" size='25'
								labelCode='user.profilePic.label'
								labelCodeDefault='Profile Picture Link' />


							<s2ui:textFieldRow name='openId' bean="${command}"
								value="${command.openId}" size='25'
								labelCode='user.openId.label' labelCodeDefault='OpenId' />
						</table>
					</form>
				</g:else>
			</fieldset>
		</div>
	</div>

</body>
</html>
