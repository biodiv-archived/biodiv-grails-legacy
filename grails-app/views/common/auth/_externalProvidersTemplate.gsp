
<div class="external_providers">
	
	<div class="sign_in_external_bttn"
		style="background-image: url('../images/external_providers.png'); background-position: 0 0; width: 100px; height: 33px; cursor: pointer; margin-left: 6px;">
		<!-- div class="fb-login-button"
			data-scope="email,user_about_me,user_location,user_activities,user_hometown,manage_notifications,user_website,publish_stream"
			data-show-faces="true">Login with Facebook</div-->
		<div class="fbJustConnect">Login with Facebook</div>
	</div>

	<div class="sign_in_external_bttn">
		<form action='${openIdPostUrl}' method='POST' autocomplete='off'
			class="form-inline" name='google_openIdLoginForm'>
			<input type="hidden" name="${openidIdentifier}"
				class="openid-identifier"
				value="https://www.google.com/accounts/o8/id" /> <input
				type="submit" value=""
				style="background-image: url('../images/external_providers.png'); background-position: 0 -33px; width: 100px; height: 33px; cursor: pointer; background-color: #ffffff; border: 0;" />
		</form>
	</div>

	<div class="sign_in_external_bttn">
		<form action='${openIdPostUrl}' method='POST' autocomplete='off'
			class="form-inline" name='yahoo_openIdLoginForm'>
			<input type="hidden" name="${openidIdentifier}"
				class="yahoo openid-identifier" value="http://me.yahoo.com/" /> <input
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
					style="padding: 3px;"><input type="submit" value="Log in" />
				</td>
				</td>
			</tr>

		</table>
	</form>
</div-->