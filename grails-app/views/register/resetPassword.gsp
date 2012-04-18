<html>

<head>
<title><g:message code='spring.security.ui.resetPassword.title' />
</title>
<meta name='layout' content='main' />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'auth.css', absolute:true)}" />
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
				<div style='clear: both'>
					<fieldset>
						<legend>
							<g:message code="spring.security.ui.resetPassword.header"
								default="Reset password" />
						</legend>
						<form action='${createLink(controller:'register', action:'resetPassword')}' name='resetPasswordForm'
							autocomplete='off' method="POST" class="form-horizontal">
							<g:hiddenField name='t' value='${token}' />
							<div class="control-group"
								style="clear: both; border-top: 1px solid #Eee; padding-top: 5px;">
								<g:message code='spring.security.ui.resetPassword.description' />
							</div>
							<div
								class="control-group ${hasErrors(bean: command, field: password, 'error')}">
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
								class="control-group ${hasErrors(bean: command, field: password2, 'error')}">
								<label class="control-label" for="password2"><g:message
										code='user.password2.label' default='Password (again)*' /> </label>
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
							<div class="control-group">

								<s2ui:submitButton elementId='reset' form='resetPasswordForm'
									messageCode='spring.security.ui.resetPassword.submit'
									class="btn btn-primary" style="float:right;" />
							</div>
						</form>
					</fieldset>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
