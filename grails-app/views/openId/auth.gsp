<head>
<meta name="layout" content="main">
<title>Login</title>
<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'auth.css', absolute:true)}" />
</head>

<body>
	<div class="container_16 big_wrapper">
		<div class="openid-loginbox">

			<g:if test='${flash.message}'>
				<div class='login_message'>
					${flash.message}
				</div>
			</g:if>

			<table class='openid-loginbox-inner' cellpadding="0" cellspacing="0">
				<tr>
					<td class="openid-loginbox-title" align="left">
						Sign in
					</td>
				</tr>
				<tr>
					<td>
						<sUser:externalAuthProviders model='["openidIdentifier":openidIdentifier, "title":"Log in"]'/>

						<div id='formLogin' style='clear: both'>

							<form action='${daoPostUrl}' method='POST'
								name='loginForm'>
								<table class="openid-loginbox-userpass">
									<tr>
										<td colspan=2>Or, log in with your user account</td>
									</tr>
									<tr>
										<td><label for="username" style="font-weight: bold;"><g:message code='spring.security.ui.login.username'/></label></td>
										<td><input type="text" name='j_username' id='username' />
										</td>
									</tr>
									<tr>
										<td><label for="password" style="font-weight: bold;"><g:message code='spring.security.ui.login.password'/></label></td>
										<td><input type="password" name='j_password'
											id='password' /></td>
									</tr>

									<tr>
										<td colspan='2' class="openid-submit" align="center" valign="middle"><input
											type="checkbox" class="checkbox"
											name="${rememberMeParameter}" id="remember_me"
											checked="checked" /> <label for='remember_me'><g:message
													code='spring.security.ui.login.rememberme' />
										</label> | <span class="forgot-link"> <g:link
													controller='register' action='forgotPassword'>
													<g:message code='spring.security.ui.login.forgotPassword' />
												</g:link> </span>
												
												<s2ui:submitButton
												elementId='loginButton' form='loginForm'
												messageCode='spring.security.ui.login.login' /></td>
									</tr>
									<!-- tr>
										<td colspan='2' class="openid-submit" align="center" valign="middle"><s2ui:linkButton
												elementId='register' controller='register'
												messageCode='spring.security.ui.login.register' /> </td>
									</tr-->

								</table>
							</form>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<g:javascript>
	(function() { document.forms['loginForm'].elements['username'].focus(); })();
		
		function showOpenIdForm() {
		        document.getElementById('openidLogin').style.display = '';
		}
		
		var openid = true;
		
		function toggleForms() {
			if (openid) {
				document.getElementById('openidLogin').style.display = 'none';
				document.getElementById('formLogin').style.display = '';
			}
			else {
				document.getElementById('openidLogin').style.display = '';
				document.getElementById('formLogin').style.display = 'none';
			}
			openid = !openid;
		}
	</g:javascript>
</body>
