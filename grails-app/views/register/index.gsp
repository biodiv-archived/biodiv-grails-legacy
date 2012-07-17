<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.register.title' /></title>

<r:require modules="auth"/>
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

	<div class="container outer-wrapper">
		<div class="row">

			<div class="openid-loginbox super-section">
				<g:if test="${flash.error}">
					<div class="alert alert-error">
						${flash.error}
					</div>
				</g:if>

				<g:if test="${flash.message}">
					<div class="alert alert-success">
						${flash.message}
					</div>
				</g:if>
				
				<div id='formLogin' style='clear: both'>
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

							<div class="control-group" style="clear: both;float: left;line-height: 40px;">Using:</div>
							<div class="control_group">
								<auth:externalAuthProviders
									model='["openidIdentifier":openidIdentifier]' />
							</div>
							<form action="${createLink(controller:'register', action:'register')}" name='registerForm' method="POST"
								class="form-horizontal">
								<div class="control-group"
								style="clear: both; border-top: 1px solid #Eee; padding-top: 5px;">Or,
								register here:</div>
								<div
									class="control-group ${hasErrors(bean: command, field: 'email', 'error')}">
									<label class="control-label" for="email"><g:message
											code='user.email.label' default='E-mail *' /> </label>
									<div class="controls">
										<input
											class="input-large focused ${(command.openId)?'readonly':''} "
											id="email" type="text" value="${command.email}" name="email"
											placeholder="Enter your email id..."
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
												code='user.password.label' default='Password *' /> </label>
										<div class="controls">
											<input class="input-large" id="password" type="password"
												value="${command.password}" name="password"
												placeholder="Enter your password...">

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
												code='user.password2.label' default='Password (again)*' />
										</label>
										<div class="controls">
											<input class="input-large" id="password2" type="password"
												value="${command.password2}" name="password2"
												placeholder="Enter your password again...">

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
											code='user.name.label' default='Name' /> </label>
									<div class="controls">
										<input class="input-large" id="name" type="text"
											value="${command.name}" name="name"
											placeholder="Enter your name...">

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
											code='user.location.label' default='Location' /> </label>
									<div class="controls">
										<input class="input-large" id="location" type="text"
											value="${command.location}" name="location"
											placeholder="Enter your location...">

										<g:hasErrors bean="${command}" field="location">
											<div class="help-inline">
												<g:renderErrors bean="${command}" field="location" />
											</div>
										</g:hasErrors>

									</div>
								</div>
								
								<div
									class="control-group ${hasErrors(bean: command, field: 'captcha_response', 'error')}">
									<label class="control-label" for="captcha_response"><jcaptcha:jpeg name="imageCaptcha" height="100px" width="100px" /></label>
									
									<div class="controls">
										
										<input class="input-large" id="captcha_response" type="text"
											value="" name="captcha_response"
											placeholder="Enter words as shown in box...">

										<g:hasErrors bean="${command}" field="captcha_response">
											<div class="help-inline">
												<g:renderErrors bean="${command}" field="captcha_response" />
											</div>
										</g:hasErrors>

									</div>
								</div>
									    								
								<div class="control-group">
									<span class="policy-text"> By registering you agree to
										our <a href="/terms">Terms and Conditions</a> on the use of
										our site </span>
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
		</div>
	</div>
</body>
</html>
