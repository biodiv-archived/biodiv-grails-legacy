<html>

<head>
<g:set var="title" value="${g.message(code:'register.value.reset')}"/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
<r:require modules="auth" />
<style type="text/css">
.openid-loginbox .form-horizontal .control-label {
	width: 150px;
	font-weight: bold;
	margin-right:10px;
}
.openid-loginbox .form-horizontal .controls {
	margin-left: 170px;
}
</style>
</head>

<body>

	<div class="openid-loginbox super-section">
		
		<div>
			<fieldset>
				<legend style="margin-bottom:0px;border-bottom:0px;">
					<g:message code="spring.security.ui.resetPassword.header" />
				</legend>
				<form
					action="${uGroup.createLink(controller:'user', action:'resetPassword', id:params.id, userGroupWebaddress:params.webaddress)}"
					name='resetPasswordForm' autocomplete='off' method="POST"
					class="form-horizontal">
					
					<div class="control-group"
						style="clear: both; border-top: 1px solid #Eee; padding-top: 5px;">
						
					</div>
						<div
						class="control-group ${hasErrors(bean: command, field: 'currentPassword', 'error')}">
						<label class="control-label" for="currentPassword"><g:message
								code='user.currentPassword.label' /> </label>
						<div class="controls">
							<input class="input-xlarge" id="currentPassword" type="password"
								value="${command.currentPassword}" name="currentPassword"
								placeholder="${g.message(code:'placeholder.enter.current.password')}">

							<g:hasErrors bean="${command}" field="currentPassword">
								<div class="help-inline">
									<g:renderErrors bean="${command}" field="currentPassword" />
								</div>
							</g:hasErrors>
						</div>
					</div>
					<div
						class="control-group ${hasErrors(bean: command, field: 'password', 'error')}">
						<label class="control-label" for="password"><g:message
								code='register.password.label' /> </label>
						<div class="controls">
							<input class="input-xlarge" id="password" type="password"
								value="${command.password}" name="password"
								placeholder="${g.message(code:'placeholder.enter.new.password')}">

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
								code='register.password.again.label'  /> </label>
						<div class="controls">
							<input class="input-xlarge" id="password2" type="password"
								value="${command.password2}" name="password2"
								placeholder="${g.message(code:'placeholder.enter.new.again')}">

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

</body>
</html>
