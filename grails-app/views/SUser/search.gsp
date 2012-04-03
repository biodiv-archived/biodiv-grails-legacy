<html>

<head>
<meta name='layout' content='main' />
<title><g:message code='spring.security.ui.user.search' /></title>
<g:javascript src="jquery.autopager-1.0.0.js"
	base="${grailsApplication.config.grails.serverURL+'/js/jquery/'}"></g:javascript>
<g:set var="entityName"
	value="${message(code: 'sUser.label', default: 'Users')}" />
</head>

<body>
	<div class="container outer-wrapper">
		<div class="row">
			<div class="span12">
				<div class="page-header">
					<h1>
						<g:message code="default.list.label" args="[entityName]" />
					</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>


				<div class="filters">
					<g:form action='userSearch' name='userSearchForm' class="well form-horizontal ">
						<label class="control-label" for="username"><g:message code='user.username.label' default='Username' />:</label>
						<div class="controls">
							<div class="input-append">
							<g:textField class="span3" name='username' size='50' maxlength='255'
							value='${username}' class="input-medium search-query"/><button id="search" class="btn btn-primary" type="button"><g:message code='spring.security.ui.search' default='Search'/></button>
							</div>
							<div class="btn-group" data-toggle="buttons-radio"
								style="float: right;">
								<button class="list_view_bttn btn list_style_button active">
									<i class="icon-align-justify"></i>
								</button>
								<button class="grid_view_bttn btn grid_style_button">
									<i class="icon-th-large"></i>
								</button>
							</div>
						</div>
						
					</g:form>
					
				</div>

				<div style="clear: both"></div>

				<g:if test='${searched}'>
					<div class="row">
						<!-- main_content -->
						<div class="list span12">
                        	<div class="observations thumbwrap">
								<%
def queryParams = [username: username, enabled: enabled, accountExpired: accountExpired, accountLocked: accountLocked, passwordExpired: passwordExpired]
%>
								<div class="observation">
									<sUser:showUserList
										model="['userInstanceList':results, 'userInstanceTotal':totalCount, 'queryParams':queryParams]" />
								</div>
							</div>
						</div>
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
});

<s2ui:initCheckboxes/>

</script>

</body>
</html>
