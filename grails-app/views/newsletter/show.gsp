
<%@ page import="utils.Newsletter"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'newsletter.label', default: 'Newsletter')}" />
<title><g:message code="default.show.label" args="[entityName]" />
</title>
<style>
.newsletter_wrapper {
	padding: 30px;
	margin-left: auto;
	margin-right: auto;
	background-color: #e8f6f0;
	font-family: 'Helvetica Neue', Arial, 'Liberation Sans', FreeSans,
		sans-serif;
}

.newsletter_wrapper .body {
	background-color: #ffffff;
	padding: 10px;
}

.newsletter_wrapper .body h1 {
	padding: 10px;
	border-bottom: 2px solid #60c59e;
}

.newsletter_wrapper .body .date {
	font-size: 10px;
	font-style: italic;
}
</style>
<r:require modules="core" />
</head>
<body>
	<div>
		<div class="page-header">
			
					<h1>
						${fieldValue(bean: newsletterInstance, field: "title")}
					</h1>
		</div>
		<div class="description notes_view bodymarker">
			<table>
				<tbody>

					<tr class="prop">
						<td valign="top" class="value date"><g:formatDate
								date="${newsletterInstance?.date}" />
						</td>
					</tr>
					
					<tr class="prop">
						<td valign="top" class="value">
							${newsletterInstance?.newsitem}
						</td>
					</tr>


				</tbody>
			</table>
		</div>
		<sec:ifLoggedIn>
			<div class="buttons">
				<g:form>
					<g:hiddenField name="id" value="${newsletterInstance?.id}" />
					<g:hiddenField name="userGroup"
						value="${newsletterInstance?.userGroup?.webaddress}" />
					<span class="button"><g:actionSubmit class="edit"
							action="edit"
							value="${message(code: 'default.button.edit.label', default: 'Edit')}" />
					</span>
					<span class="button"><g:actionSubmit class="delete"
							action="delete"
							value="${message(code: 'default.button.delete.label', default: 'Delete')}"
							onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					</span>
				</g:form>
			</div>
		</sec:ifLoggedIn>
	</div>
</body>
</html>
