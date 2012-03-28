<head>
<meta name="layout" content="main">
<title>Login</title>
<style type='text/css' media='screen'>
div.openid-loginbox {
	width: 400px;
	margin-left: auto;
	margin-right: auto;
	background: #F5F5F5;
	border: 1px solid #E5E5E5;
	padding: 15px;
}

.openid-loginbox-inner {
	width: 100%;
}

td.openid-loginbox-title {
	border-bottom: 1px #c0c0ff solid;
	padding: 10px;
}

td.openid-loginbox-title table {
	width: 100%;
	font-size: 18px;
}

.openid-loginbox-useopenid {
	font-weight: normal;
	font-size: 14px;
}

td.openid-loginbox-title img {
	border: 0;
	vertical-align: middle;
	padding-right: 3px;
}

table.openid-loginbox-userpass {
	margin: 3px 3px 3px 8px;
	margin-left: auto;
	margin-right: auto;
}

table.openid-loginbox-userpass td {
	height: 25px;
	padding: 3px;
}

input.openid-identifier {
	background: url(http://stat.livejournal.com/img/openid-inputicon.gif)
		no-repeat;
	background-color: #fff;
	background-position: 0 50%;
	padding-left: 18px;
}

input[type='text'],input[type='password'] {
	font-size: 16px;
	width: 310px;
}

input[type='submit'] {
	font-size: 14px;
}

td.openid-submit {
	padding: 3px;
}

.sign_in_external_bttn {
	float: left;
	margin: 3px;
}

#openidLogin {
	border-top: 1px #c0c0ff solid;
	margin-top: 50px;
}

#formLogin {
	border-top: 1px #c0c0ff solid;
	margin-top: 50px;
}

.fb-login-button {
	opacity: 0;
}
</style>
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
					<td class="openid-loginbox-title">
						<table>
							<tr>
								<td align="left">Sign in</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<div>
							Sign in with:<br />
							<div class="sign_in_external_bttn"
								style="background-image: url('../images/external_providers.png'); background-position: 0 0; width: 100px; height: 33px; cursor: pointer; margin-left: 6px;">
								<div class="fb-login-button"
									data-scope="email,user_about_me,user_location,user_activities,user_hometown,manage_notifications,user_website,publish_stream"
									data-show-faces="false">Login with Facebook</div>
							</div>

							<div class="sign_in_external_bttn">
								<form action='${openIdPostUrl}' method='POST' autocomplete='off'
									name='openIdLoginForm'>
									<input type="hidden" name="${openidIdentifier}"
										class="openid-identifier"
										value="https://www.google.com/accounts/o8/id" /> <input
										type="submit" value=""
										style="background-image: url('../images/external_providers.png'); background-position: 0 -33px; width: 100px; height: 33px; cursor: pointer; background-color: #ffffff; border: 0;" />
								</form>
							</div>
							
							<div class="sign_in_external_bttn">
								<form action='${openIdPostUrl}' method='POST' autocomplete='off'
									name='openIdLoginForm'>
									<input type="hidden" name="${openidIdentifier}"
										class="yahoo openid-identifier"
										value="http://me.yahoo.com/" /> <input
										type="submit" value=""
										style="background-image: url('../images/external_providers.png'); background-position: 0 -99px; width: 100px; height: 33px; cursor: pointer; background-color: #ffffff; border: 0;" />
								</form>
							</div>
							
							<!-- div class="sign_in_external_bttn"
								style="background-image: url('../images/external_providers.png'); background-position: 0 -133px; width: 100px; height: 33px; cursor: pointer;"
								onclick="showOpenIdForm();"></div-->
						</div>

						<!-- div id='openidLogin' style="display: none; clear: both">
							<form action='${openIdPostUrl}' method='POST' autocomplete='off'
								name='openIdLoginForm'>
								<table class="openid-loginbox-userpass">
									<tr>
										<td>manually enter your OpenID</td>
									</tr>
									<tr>
										<td style="padding: 3px;"><input type="text"
											name="${openidIdentifier}" class="openid-identifier" />
										<td colspan='2' class="openid-submit" align="center"
											style="padding: 3px;"><input type="submit"
											value="Log in" /></td>
										</td>
									</tr>
									
								</table>
							</form>
						</div-->

						<div id='formLogin' style='clear: both'>

							<form action='${daoPostUrl}' method='POST'
								name='loginForm'>
								<table class="openid-loginbox-userpass">
									<tr>
										<td colspan=2>Or, sign in with your user account</td>
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
										<td colspan='2' class="openid-submit" align="center"><input
											type="checkbox" class="checkbox"
											name="${rememberMeParameter}" id="remember_me"
											checked="checked" /> <label for='remember_me'><g:message
													code='spring.security.ui.login.rememberme' />
										</label> | <span class="forgot-link"> <g:link
													controller='register' action='forgotPassword'>
													<g:message code='spring.security.ui.login.forgotPassword' />
												</g:link> </span></td>
									</tr>
									<tr>
										<td colspan='2' class="openid-submit" align="center"><s2ui:linkButton
												elementId='register' controller='register'
												messageCode='spring.security.ui.login.register' /> <s2ui:submitButton
												elementId='loginButton' form='loginForm'
												messageCode='spring.security.ui.login.login' /></td>
									</tr>

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
