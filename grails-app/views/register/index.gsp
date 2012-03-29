<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.register.title' /></title>

<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'css',file:'auth.css', absolute:true)}" />

<!-- 
<script src="http://maps.google.com/maps/api/js?sensor=true"></script>
<g:javascript src="jquery/jquery.watermark.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
<g:javascript src="location/location-picker.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>
 -->
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
					<td class="openid-loginbox-title" align="left">Create Account
					</td>
				</tr>
				<tr>
					<td><g:if test='${emailSent}'>
							<br />
							<g:message code='spring.security.ui.register.sent' />
						</g:if> <g:else>
							<sUser:externalAuthProviders
								model='["openidIdentifier":openidIdentifier, "title":"Register"]' />


							<div id='formLogin' style="clear: both">

								<g:form action='register' name='registerForm'>

									<table class="openid-loginbox-userpass">
										<tbody>
											<tr>
												<td colspan=2><g:if test="${command.openId }">

													</g:if> <g:else>
														<g:message
															code="spring.security.ui.login.register.noexternalproviders"
															default="I dont have account in the above"/>:
														</g:else></td>
											</tr>

											<s2ui:textFieldRow name='email' bean="${command}"
												value="${command.email}" size='25'
												labelCode='user.email.label' labelCodeDefault='E-mail*'
												readonly="${(command.openId)?'readonly':''}" />

											<g:if test="${!command.openId}">
												<s2ui:passwordFieldRow name='password'
													labelCode='user.password.label' bean="${command}" size='25'
													labelCodeDefault='Password*' value="${command.password}" />

												<s2ui:passwordFieldRow name='password2'
													labelCode='user.password2.label' bean="${command}"
													size='25' labelCodeDefault='Password (again)*'
													value="${command.password2}" />
											</g:if>
											
											<s2ui:textFieldRow name='name' labelCode='user.name.label'
												bean="${command}" size='25' labelCodeDefault='Full Name'
												value="${command.name}" />

											<s2ui:textFieldRow id="address" name='location' bean="${command}"
												value="${command.location}" size='25'
												labelCode='user.location.label' labelCodeDefault='Location' />

											<tr>
												<td colspan=2><span class="policy-text"> 
												By registering you agree to our <a href="/terms">Terms and Conditions</a> on the use of our site
												</span> <s2ui:submitButton
														elementId='createButton' form='registerForm'
														messageCode='spring.security.ui.login.register' /></td>

											</tr>
										</tbody>
									</table>



									<table style="display: none">
										<g:if test="${command.openId}">
												<s2ui:passwordFieldRow name='password'
													labelCode='user.password.label' bean="${command}" size='25'
													labelCodeDefault='Password*' value="dummyPassword" />

												<s2ui:passwordFieldRow name='password2'
													labelCode='user.password2.label' bean="${command}"
													size='25' labelCodeDefault='Password (again)*'
													value="dummyPassword" />
											</g:if>
										<s2ui:textFieldRow name='username'
											labelCode='user.username.label' bean="${command}" size='25'
											labelCodeDefault='Username' value="${command.username}" />

										<s2ui:textFieldRow name='website' bean="${command}"
											value="${command.website}" size='25'
											labelCode='user.website.label' labelCodeDefault='Website' />

										<s2ui:textFieldRow name='timezone' bean="${command}"
											value="${command.timezone}" size='25'
											labelCode='user.timezone.label'
											labelCodeDefault='Timezone Offset' />

										<s2ui:textFieldRow name='aboutMe' bean="${command}"
											value="${command.aboutMe}" size='25'
											labelCode='user.aboutMe.label' labelCodeDefault='About Me' />

										<s2ui:textFieldRow name='profilePic' bean="${command}"
											value="${command.profilePic}" size='25'
											labelCode='user.profilePic.label'
											labelCodeDefault='Profile Picture Link' />


										<s2ui:textFieldRow name='openId' bean="${command}"
											value="${command.openId}" size='25'
											labelCode='user.openId.label' labelCodeDefault='OpenId' />
									</table>





								</g:form>


							</div>
						</g:else></td>
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
