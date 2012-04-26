<div style='clear: both'>
	<fieldset>
		<legend>
			<g:message code="springSecurity.login.title" default="Log in" />
		</legend>
		<div class="control-group"
			style="clear: both; float: left; line-height: 40px;">Using:</div>
		<div class="control_group">
			<auth:externalAuthProviders
				model='["openidIdentifier":openidIdentifier, "openIdPostUrl":openIdPostUrl]' />
		</div>
		<form action='${daoPostUrl}' method='POST' class="form-horizontal"
			name='loginForm'>
			<div class="control-group"
				style="clear: both; border-top: 1px solid #Eee; padding-top: 5px;">Or,
				log in with your user account:</div>
			<div class="control-group">
				<label class="control-label" for="username"><g:message
						code='spring.security.ui.login.username' /> </label>
				<div class="controls">
					<input class="input-xlarge focused" type="text" name="j_username"
						placeholder="Enter your email id...">
				</div>
			</div>
			<div class="control-group" style="clear: both;">
				<label class="control-label" for="password"><g:message
						code='spring.security.ui.login.password' /> </label>
				<div class="controls">
					<input class="input-xlarge" type="password" name="j_password"
						placeholder="Enter your password...">
				</div>
			</div>

			<div class="control-group">
				<label class="control-label" for="optionsCheckbox"></label>
				<div class="controls">
					<label class="checkbox"> <input type="checkbox"
						name="${rememberMeParameter}" for='remember_me'> <g:message
							code='spring.security.ui.login.rememberme' /> | <g:link
							controller='register' action='forgotPassword'>
							<g:message code='spring.security.ui.login.forgotPassword' />
						</g:link> </label> <input class="btn btn-primary" type="submit" value="Login"
						class="s2ui_hidden_button" style="float: right;margin: 3px;">
				</div>
			</div>
		</form>
	</fieldset>
</div>