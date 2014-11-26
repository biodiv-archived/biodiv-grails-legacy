<%@page import="species.utils.Utils"%>
<%@page import="species.groups.UserGroup"%>
<%@ page import="utils.Newsletter"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'default.pages.label')}"/>
<title>${g.message(code:'link.create.newsletter')}</title>
<style>
.body {
	padding: 10px;
}
</style>
<r:require modules="core" />
</head>
<body>
	<div class="span11">
		<div class="page-header">
		<h1>
			<g:message code="default.create.label" args="[title]" />
		</h1>
		</div>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${newsletterInstance}">
			<div class="errors">
				<g:message code="newsletter.create.fix.error" />
			</div>
		</g:hasErrors>
		<form action="${uGroup.createLink(controller:'newsletter', action:'save', userGroupWebaddress:params.webaddress)}" method="POST">
			<div class="dialog">
				<div
					class="control-group ${hasErrors(bean: newsletterInstance, field: 'title', 'errors')}">
					<label for="title"><g:message code="default.title.label" /> </label>
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
					<label for="date"><g:message code="default.date.label" /> </label>
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
						
						
						<ckeditor:editor name="newsitem" height="400px" userSpace="${params.webaddress }">
							${newsletterInstance?.newsitem}
						</ckeditor:editor>
						
						<g:hasErrors bean="${newsletterInstance}" field="newsitem">
							<div class="help-inline">
								<g:renderErrors bean="${newsletterInstance}" field="newsitem" />
							</div>
						</g:hasErrors>
					</div>
				</div>

				<div class="row control-group left-indent">

					<label class="checkbox" style="text-align: left;"> <g:checkBox
							style="margin-left:0px;" name="sticky"
							checked="${newsletterInstance.sticky}" /> <g:message
							code="newsletter.sticky" />
					</label>
				</div>

				<g:if test="${params.userGroup}">
					<input type="hidden" name="userGroup"
						value="${(!(params.userGroup instanceof UserGroup))?params.userGroup:params.userGroup.webaddress}" />
				</g:if>

			</div>
			<div class="buttons">
				<span class="button">
				<input class="btn btn-primary"
					style="float: right; margin-right: 5px;" type="submit" value="${g.message(code:'default.button.create.label')}" />
				
				</span>
			</div>
		</form>
	</div>
	<script>
		$(document).ready(function() {
		});
	</script>
</body>
</html>
