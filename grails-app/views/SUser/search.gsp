<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' />
</title>
<g:javascript src="jquery.autopager-1.0.0.js"
	base="${grailsApplication.config.grails.serverURL+'/js/jquery/'}"></g:javascript>

</head>

<body>
	<div class="container_16 big_wrapper">
		<div class=" grid_16">
			<div class="body">
				<g:form action='userSearch' name='userSearchForm'>
					<g:message code='user.username.label' default='Username' />:</td>
					<g:textField name='username' size='50' maxlength='255'
						autocomplete='off' value='${username}' />
					<s2ui:submitButton elementId='search' form='userSearchForm'
						messageCode='spring.security.ui.search' />
				</g:form>

				<br />
				<g:if test='${searched}'>

					<%
def queryParams = [username: username, enabled: enabled, accountExpired: accountExpired, accountLocked: accountLocked, passwordExpired: passwordExpired]
%>

					<div class="list grid_16">
						<sUser:showUserList model="['userInstanceList':results, 'userInstanceTotal':totalCount, 'queryParams':queryParams]" />
					</div>

					<br />

					<div class="paginateButtons" style="visibility:hidden; clear: both">
						<g:paginate total="${totalCount}" params="${queryParams}" />
					</div>

				</g:if>

			</div>
		</div>
	</div>

	<script>
$(document).ready(function() {
	$("#username").focus().autocomplete({
		minLength: 3,
		cache: false,
		source: "${createLink(action: 'ajaxUserSearch')}"
	});
	
	<%if(!params.max) {%>
		document.forms.userSearchForm.submit();
	<%}%>
});

<s2ui:initCheckboxes/>

</script>

</body>
</html>
	