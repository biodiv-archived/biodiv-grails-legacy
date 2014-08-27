<div>
	<fieldset>
		<legend>
			<a href="${uGroup.createLink(controller:'login', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }"><g:message code="button.login" /></a> | 
			<g:link controller='register'><g:message code="button.register" /></g:link>
			 
		</legend>
		<div class="control-group"
			style="clear: both; float: left; line-height: 40px;"><g:message code="loginformtemplate.using" />:</div>
		<div class="control_group">
			<auth:externalAuthProviders
				model='["openidIdentifier":openidIdentifier, "openIdPostUrl":openIdPostUrl, "ajax":ajax]' />
		</div>
		<form action='${daoPostUrl}' method='POST' class="form-horizontal"
			name='loginForm'>
			<div class="control-group"
				style="clear: both; border-top: 1px solid #Eee; padding-top: 5px;"><g:message code="loginformtemplate.or" />,
				<g:message code="loginformtemplate.login.user.account" />:</div>
			<div class="control-group">
				<label class="control-label" for="username"><g:message
						code='spring.security.ui.login.username' /> </label>
				<div class="controls">
					<input class="input-xlarge focused" type="text" name="j_username"
						placeholder="${g.message(code:'placeholder.enter.emailid')}">
				</div>
			</div>
			<div class="control-group" style="clear: both;">
				<label class="control-label" for="password"><g:message
						code='spring.security.ui.login.password' /> </label>
				<div class="controls">
					<input class="input-xlarge" type="password" name="j_password"
						placeholder="${g.message(code:'placeholder.enter.password')}">
				</div>
			</div>
			
			<g:if test="${targetUrl}">
				<input type="hidden" name="spring-security-redirect"
					value="${targetUrl}" />
			</g:if>
			
			<div class="control-group">
				<label class="control-label" for="optionsCheckbox"></label>
				<div class="controls">
					<label class="checkbox"> <input type="checkbox"
						name="${rememberMeParameter}" for="remember_me"/> <g:message
							code='spring.security.ui.login.rememberme' /> | 
							<a href="${uGroup.createLink(controller:'register', action:'forgotPassword','userGroupWebaddress':params.webaddress)}">
							<g:message code='spring.security.ui.login.forgotPassword' />
						</a> </label> <input class="btn btn-primary" type="submit" value="Login"
						style="float: right;margin: 3px;">
				</div>
			</div>
		</form>
	</fieldset>
</div>
