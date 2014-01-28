<html>

<head>
<g:set var="title" value="Forgot Password"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="auth" />
</head>
<style>
form {
	padding-top: 10px;
}
</style>
<body>

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
		<div>
			<fieldset>
				<legend>
					<g:message code="springSecurity.forgotPassword.title"
						default="Forgot password ???" />
				</legend>

				<g:if test='${emailSent}'>
					<div class="alert alert-success">
						<g:message code='spring.security.ui.forgotPassword.sent' />
					</div>
				</g:if>
				<g:else>

					<form
						action='${uGroup.createLink(controller:'register', action:'forgotPassword', userGroupWebaddress:params.webaddress)}'
						name="forgotPasswordForm" method="POST" class="form-horizontal">

						<div class="control-group"
							style="clear: both; border-top: 1px solid #Eee; padding-top: 5px;">
							<g:message code='spring.security.ui.forgotPassword.description' />
						</div>

						<div class="control-group">
							<label class="control-label" for="email"><g:message
									code='spring.security.ui.forgotPassword.username'
									default='E-mail *' /> </label>
							<div class="controls">
								<input class="input-large focused" id="username" type="text"
									name="username" placeholder="Enter your email id...">

							</div>
						</div>

						<div class="control-group">

							<s2ui:submitButton elementId='reset' form='forgotPasswordForm'
								messageCode='spring.security.ui.forgotPassword.submit'
								class="btn btn-primary" style="float:right;" />
						</div>

					</form>
				</g:else>
			</fieldset>
		</div>
	</div>

</body>
</html>
