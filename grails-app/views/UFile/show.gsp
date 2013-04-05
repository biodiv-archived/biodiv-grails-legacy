
<%@ page import="content.fileManager.UFile"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'UFile.label', default: 'UFile')}" />
<title><g:message code="default.show.label" args="[entityName]" /></title>
<r:require modules="core" />
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a class="home"
			href="${createLink(uri: '/')}"><g:message
					code="default.home.label" /></a></span> <span class="menuButton"><g:link
				class="list" action="list">
				<g:message code="default.list.label" args="[entityName]" />
			</g:link></span> <span class="menuButton"><g:link class="create"
				action="create">
				<g:message code="default.new.label" args="[entityName]" />
			</g:link></span>
	</div>
	<div class="body">
		<h1>
			<g:message code="default.show.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<div class="dialog">
			<table>
				<tbody>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.id.label" default="Id" /></td>

						<td valign="top" class="value">
							${fieldValue(bean: UFileInstance, field: "id")}
						</td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.size.label" default="Size" /></td>

						<td valign="top" class="value">
							${fieldValue(bean: UFileInstance, field: "size")}
						</td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.path.label" default="Path" /></td>

						<td valign="top" class="value">
							${fieldValue(bean: UFileInstance, field: "path")}
						</td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.name.label" default="Name" /></td>

						<td valign="top" class="value">
							${fieldValue(bean: UFileInstance, field: "name")}
						</td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.description.label" default="Description" /></td>

						<td valign="top" class="value">
							${fieldValue(bean: UFileInstance, field: "description")}
						</td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.mimetype.label" default="Mimetype" /></td>

						<td valign="top" class="value">
							${fieldValue(bean: UFileInstance, field: "mimetype")}
						</td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.dateCreated.label" default="Date Created" /></td>

						<td valign="top" class="value"><g:formatDate
								date="${UFileInstance?.dateCreated}" /></td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.downloads.label" default="Downloads" /></td>

						<td valign="top" class="value">
							${fieldValue(bean: UFileInstance, field: "downloads")}
						</td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.doi.label" default="Doi" /></td>

						<td valign="top" class="value">
							${fieldValue(bean: UFileInstance, field: "doi")}
						</td>

					</tr>

					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="UFile.doi.label" default="Tags" /></td>

						<td valign="top" class="value">
							${UFileInstance.tags}
						</td>

					</tr>

				</tbody>
			</table>
		</div>
		<div class="buttons">
			<g:form>
				<g:hiddenField name="id" value="${UFileInstance?.id}" />
				<span class="button"><g:actionSubmit class="edit"
						action="edit"
						value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
				<span class="button"><g:actionSubmit class="delete"
						action="delete"
						value="${message(code: 'default.button.delete.label', default: 'Delete')}"
						onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
			</g:form>
		</div>
	</div>
</body>
</html>
