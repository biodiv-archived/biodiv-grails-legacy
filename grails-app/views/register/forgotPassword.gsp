<html>

<head>
<title><g:message code='spring.security.ui.forgotPassword.title' />
</title>
<meta name='layout' content='main' />
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'auth.css', absolute:true)}" />
</head>
<style>
form {
	padding-top: 10px;
}
</style>
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
							<g:message code="springSecurity.forgotPassword.title"
								default="Forgot password ???" />
						</legend>

						<g:if test='${emailSent}'>
							<div class="alert alert-success">
								<g:message code='spring.security.ui.forgotPassword.sent' />
							</div>
						</g:if>
						<g:else>

							<form action='${createLink(controller:'register', action:'forgotPassword')}' name="forgotPasswordForm" method="POST" 
								class="form-horizontal">

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
		</div>
	</div>

</body>
</html>
