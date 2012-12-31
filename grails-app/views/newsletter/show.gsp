<%@ page import="utils.Newsletter"%>
<%@page import="species.utils.Utils"%>

<html>
<head>
<link rel="canonical"
	href="${Utils.getIBPServerDomain() + createLink(controller:'newsletter', action:'show', id:newsletterInstance.id)}" />
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
	<div id="pageContent" class="observation  span9"  style="margin-left:0px;">
		<div class="page-header clearfix">
			<h1>
				${fieldValue(bean: newsletterInstance, field: "title")}
			</h1>
		</div>
		
		<div class="description bodymarker" >
			<table>
				<tbody>

					<tr class="prop">
						<td valign="top" class="value date"><g:formatDate
								date="${newsletterInstance?.date}" /></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="value">
							${newsletterInstance?.newsitem}
						</td>
					</tr>


				</tbody>
			</table>
			<sec:ifLoggedIn>
			<div class="buttons" style="clear:both;">
				<form
					action="${uGroup.createLink(controller:'newsletter', action:'edit', userGroupWebaddress:params.webaddress)}"
					method="GET">
					<g:hiddenField name="id" value="${newsletterInstance?.id}" />
					<g:hiddenField name="userGroup"
						value="${newsletterInstance?.userGroup?.webaddress}" />
					<span class="button">
						<input class="btn btn-primary" style="float: right; margin-right: 5px;" type="submit" value="Edit"/>
					</span> <span class="button"> <a class="btn btn-danger"
						style="float: right; margin-right: 5px;"
						href="${uGroup.createLink(controller:'newsletter', action:'delete', id:newsletterInstance.id)}"
						onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">Delete
					</a> </span>
				</form>
			</div>
		</sec:ifLoggedIn>
		</div>
		
	</div>
</body>
</html>
