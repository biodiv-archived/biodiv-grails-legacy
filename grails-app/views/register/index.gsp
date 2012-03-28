<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.register.title' /></title>
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
	width: 250px;
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
								<td align="left">Create Account</td>

							</tr>
						</table></td>
				</tr>
				<tr>
					<td><g:if test='${emailSent}'>
							<br />
							<g:message code='spring.security.ui.register.sent' />
						</g:if> <g:else>
							<div>
							<!-- 
								Register using external providers : <br />
								<div class="sign_in_external_bttn"
									style="background-image: url('../images/external_providers.png'); background-position: 0 0; width: 100px; height: 33px; cursor: pointer; margin-left: 6px;">
									<div id="fb-root"></div>
									<div class="fb-login-button"
										data-scope="email,user_about_me,user_location,user_activities,user_hometown,manage_notifications,user_website,publish_stream"
										data-show-faces="false">Register with Facebook</div>
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
							</div-->
<br/>
								<div style="clear: both">

									<g:form action='register' name='registerForm'>

										<table>
											<tbody>

												<s2ui:textFieldRow name='email' bean="${command}"
													value="${command.email}" size='25'
													labelCode='user.email.label' labelCodeDefault='E-mail*' />

												<s2ui:passwordFieldRow name='password'
													labelCode='user.password.label' bean="${command}" size='25'
													labelCodeDefault='Password*' value="${command.password}" />

												<s2ui:passwordFieldRow name='password2'
													labelCode='user.password2.label' bean="${command}"
													size='25' labelCodeDefault='Password (again)*'
													value="${command.password2}" />
												 
												<s2ui:textFieldRow name='name' labelCode='user.name.label'
													bean="${command}" size='25' labelCodeDefault='Full Name'
													value="${command.name}" />
						
												<s2ui:textFieldRow name='username' labelCode='user.username.label'
													bean="${command}" size='25' labelCodeDefault='Username'
													value="${command.username}" />
						
												<s2ui:textFieldRow name='website' bean="${command}"
													value="${command.website}" size='25'
													labelCode='user.website.label' labelCodeDefault='Website' />
						
												<s2ui:textFieldRow name='location' bean="${command}"
													value="${command.location}" size='25'
													labelCode='user.location.label' labelCodeDefault='Location' />
						
												<s2ui:textFieldRow name='timezone' bean="${command}"
													value="${command.timezone}" size='25'
													labelCode='user.timezone.label'
													labelCodeDefault='Timezone Offset' />
						 
												<s2ui:textFieldRow name='aboutMe' bean="${command}"
													value="${command.aboutMe}" size='25'
													labelCode='user.aboutMe.label'
													labelCodeDefault='About Me' />
						 
						 						<s2ui:textFieldRow name='profilePic' bean="${command}"
													value="${command.profilePic}" size='25'
													labelCode='user.profilePic.label'
													labelCodeDefault='Profile Picture Link' />

												<s2ui:textFieldRow name='openId' bean="${command}"
													value="${command.openId}" size='25'
													labelCode='user.openId.label' labelCodeDefault='OpenId' />

											</tbody>
										</table>

										<s2ui:submitButton elementId='create' form='registerForm'
											messageCode='spring.security.ui.register.submit' />



									</g:form>


								</div>
						</g:else>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<script>
$(document).ready(function() {
	$('#email').focus();
});
</script>

</body>
</html>
