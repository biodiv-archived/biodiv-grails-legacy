<head>
<meta name="layout" content="main">
<title>Login</title>
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
					<div class="alert alert-error">
						${flash.message}
					</div>
				</g:if>

				<div id='formLogin' style='clear: both'>
					<fieldset>
						<legend>
							<g:message code="springSecurity.login.title" default="Log in" />
						</legend>
						<div class="control-group"style="clear: both;">Using:</div>
						<div class="control_group">
							<sUser:externalAuthProviders
								model='["openidIdentifier":openidIdentifier]' />
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
									<input class="input-xlarge focused" id="username" type="text"
										name="j_username" placeholder="Enter your email id...">
								</div>
							</div>
							<div class="control-group" style="clear: both;">
								<label class="control-label" for="password"><g:message
										code='spring.security.ui.login.password' /> </label>
								<div class="controls">
									<input class="input-xlarge" id="password" type="password"
										name="j_password" placeholder="Enter your password...">
								</div>
							</div>


							<div class="control-group">
								<label class="control-label" for="optionsCheckbox"></label>
								<div class="controls">
									<label class="checkbox"> <input type="checkbox"
										name="${rememberMeParameter}" id="remember_me"
										for='remember_me'> <g:message
											code='spring.security.ui.login.rememberme' /> | <g:link
											controller='register' action='forgotPassword'>
											<g:message code='spring.security.ui.login.forgotPassword' />
										</g:link> </label>
									<s2ui:submitButton elementId='loginButton' form='loginForm'
										class="btn btn-primary"
										messageCode='spring.security.ui.login.login' />
								</div>
							</div>
						</form>
					</fieldset>

				</div>
			</div>
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
