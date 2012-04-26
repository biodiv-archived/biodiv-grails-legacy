
<div class="external_providers">
	
	<div class="sign_in_external_bttn external_bttn facebookButton">
		<div class="fbJustConnect">Login with Facebook</div>
	</div>

	<!-- div class="sign_in_external_bttn external_bttn googleButton">
		<div class="googleConnect">Login with Google</div>
	</div-->
	
	<div  class="sign_in_external_bttn">
		<form action='${openIdPostUrl}' method='POST' autocomplete='off'
			name='google_openIdLoginForm'>
			<input type="hidden" name="${openidIdentifier}"
				class="openid-identifier"
				value="https://www.google.com/accounts/o8/id" /> <input
				type="submit" value="" class="external_bttn googleButton"/>
		</form>
	</div>

	<div class="sign_in_external_bttn">
		<form action='${openIdPostUrl}' method='POST' autocomplete='off'
			name='yahoo_openIdLoginForm'>
			<input type="hidden" name="${openidIdentifier}"
				class="yahoo openid-identifier" value="http://me.yahoo.com/" /> <input
				type="submit" value="" class="external_bttn yahooButton" />
		</form>
	</div>

</div>
