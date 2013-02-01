

<%@ page import="utils.Newsletter"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
<title><g:message code="default.edit.label" args="[entityName]" />
</title>
<r:require modules="core" />
</head>
<body>
	<div class="span9">
		<div class="page-header">
			<h1>Edit</h1>
		</div>
		
		<div class="tabbable">

			<g:hasErrors bean="${newsletterInstance}">
				<div class="errors">
					<g:renderErrors bean="${newsletterInstance}" as="list" />
				</div>
			</g:hasErrors>

			<form
				action="${uGroup.createLink(controller:'newsletter', action:'update', userGroupWebaddress:params.webaddress)}"
				method="POST">
				<g:hiddenField name="id" value="${newsletterInstance?.id}" />
				<g:hiddenField name="version" value="${newsletterInstance?.version}" />

				<table>
					<tbody>
						<tr class="prop">
							<td valign="top"
								class="value ${hasErrors(bean: newsletterInstance, field: 'title', 'errors')}">
								<g:textField name="title" value="${newsletterInstance?.title}" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top"
								class="value ${hasErrors(bean: newsletterInstance, field: 'date', 'errors')}">
								<g:datePicker name="date" precision="day"
									value="${newsletterInstance?.date}" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top"
								class="value ${hasErrors(bean: newsletterInstance, field: 'newsitem', 'errors')}">
								<ckeditor:editor name="newsitem" height="400px" userSpace="${params.webaddress }">
									${newsletterInstance?.newsitem}
								</ckeditor:editor>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top"
								class="value ${hasErrors(bean: newsletterInstance, field: 'sticky', 'errors')}">

								<g:checkBox style="margin-left:0px;" name="sticky"
									checked="${newsletterInstance.sticky}" /> <g:message
									code="newsletter.sticky"
									default="Check this option to make this page available in sidebar?" />
							</td>
						</tr>
						<g:if test="${newsletterInstance.userGroup}">
							<input type="hidden" name="userGroup"
								value="${newsletterInstance.userGroup.webaddress}" />
						</g:if>
					</tbody>
				</table>

				<div class="buttons">
					<span class="button"> <input class="btn btn-primary"
						style="float: right; margin-right: 5px;" type="submit"
						value="Update" /> </span> <span class="button"> <a
						class="btn btn-danger" style="float: right; margin-right: 5px;"
						href="${uGroup.createLink(controller:'newsletter', action:'delete', id:newsletterInstance.id)}"
						onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">Delete
					</a> </span>
				</div>
			</form>
		</div>
	</div>
</body>
</html>
