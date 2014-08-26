<div>
	<fieldset>
		<legend>
			<a href="${uGroup.createLink(controller:'login', 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }"><g:message code="msg.Login" /></a> | 
			<g:link controller='register'><g:message code="msg.Register" /></g:link>
			 
		</legend>
		<div class="control-group"
			style="clear: both; float: left; line-height: 40px;"><g:message code="msg.Using" />:</div>
		<div class="control_group">
			<auth:externalAuthProviders
				model='["openidIdentifier":openidIdentifier, "openIdPostUrl":openIdPostUrl, "ajax":ajax]' />
		</div>
		<form action='${daoPostUrl}' method='POST' class="form-horizontal"
			name='loginForm'>
			<div class="control-group"
				style="clear: both; border-top: 1px solid #Eee; padding-top: 5px;"><g:message code="msg.Or" />,
				<g:message code="msg.Login.user account" />:</div>
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
