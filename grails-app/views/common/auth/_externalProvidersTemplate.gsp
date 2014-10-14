<div class="external_providers">

	<div class="sign_in_external_bttn external_bttn facebookButton">
		<div class="fbJustConnect ${ajax?'ajaxForm':'' }"><g:message code="common.auth.login.facebook" /></div>
	</div>
	<g:if test="${ajax}">
		<div class="sign_in_external_bttn external_bttn googleButton">
			<div class="googleConnect"><g:message code="common.auth.login.google" /></div>
		</div>
		<div class="sign_in_external_bttn external_bttn yahooButton">
			<div class="yahooConnect"><g:message code="common.auth.login.yahoo" /></div>
		</div>
	</g:if>
	<g:else>
		<div class="sign_in_external_bttn">
			<form action='${openIdPostUrl}' method='POST' autocomplete='off'
				name='google_openIdLoginForm'>
				<input type="hidden" name="${openidIdentifier}"
					class="openid-identifier"
					value="https://www.google.com/accounts/o8/id" />
				<g:if test="${targetUrl}">
					<input type="hidden" name="spring-security-redirect"
						value="${targetUrl}" />
				</g:if>
				<input type="submit" value="" class="external_bttn googleButton" />
			</form>
		</div>
		
		<div class="sign_in_external_bttn">
			<form action='${openIdPostUrl}' method='POST' autocomplete='off'
				name='yahoo_openIdLoginForm'>
				<input type="hidden" name="${openidIdentifier}"
					class="yahoo openid-identifier" value="http://me.yahoo.com/" />
				<g:if test="${targetUrl}">
					<input type="hidden" name="spring-security-redirect"
						value="${targetUrl}" />
				</g:if>
				<input type="submit" value="" class="external_bttn yahooButton" />
			</form>
		</div>
	</g:else>
</div>
