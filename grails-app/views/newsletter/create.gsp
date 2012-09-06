

<%@ page import="utils.Newsletter"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
<title><g:message code="default.create.label"
		args="[entityName]" />
</title>
<style>
.body {
	padding: 10px;
}
</style>
</head>
<body>
	<div class="body">
		<h1>
			<g:message code="default.create.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${newsletterInstance}">
			<div class="errors">
				<g:renderErrors bean="${newsletterInstance}" as="list" />
			</div>
		</g:hasErrors>
		<g:form action="save">
			<div class="dialog">
				<div
					class="control-group ${hasErrors(bean: newsletterInstance, field: 'title', 'errors')}">
					<label for="title"><g:message code="newsletter.title.label"
							default="Title" />
					</label>
					<div class="controls">
						<g:textField name="title" value="${newsletterInstance?.title}" />

						<g:hasErrors bean="${newsletterInstance}" field="title">
							<div class="help-inline">
								<g:renderErrors bean="${newsletterInstance}" field="title" />
							</div>
						</g:hasErrors>
					</div>
				</div>
				<div
					class="control-group ${hasErrors(bean: newsletterInstance, field: 'date', 'errors')}">
					<label for="date"><g:message code="newsletter.date.label"
							default="Date" />
					</label>
					<div class="controls">
						<g:datePicker name="date" precision="day"
							value="${newsletterInstance?.date}" />

						<g:hasErrors bean="${newsletterInstance}" field="date">
							<div class="help-inline">
								<g:renderErrors bean="${newsletterInstance}" field="date" />
							</div>
						</g:hasErrors>
					</div>
				</div>

				<div id="main_editor"
					class="control-group ${hasErrors(bean: newsletterInstance, field: 'newsitem', 'errors')}">

					<div class="controls">
						<ckeditor:editor name="newsitem" height="300px">
							${newsletterInstance?.newsitem}
						</ckeditor:editor>
						<g:hasErrors bean="${newsletterInstance}" field="newsitem">
							<div class="help-inline">
								<g:renderErrors bean="${newsletterInstance}" field="newsitem" />
							</div>
						</g:hasErrors>
					</div>
				</div>




				<g:if test="${params.userGroupId}">
					<input type="hidden" name="userGroupId"
						value="${params.userGroupId}" />
				</g:if>

			</div>
			<div class="buttons">
				<span class="button"><g:submitButton name="create"
						class="save"
						value="${message(code: 'default.button.create.label', default: 'Create')}" />
				</span>
			</div>
		</g:form>
	</div>
	<script>
		$(document).ready(function() {
		});
	</script>
</body>
</html>
