<%@ page import="content.fileManager.UFile"%>
<%@ page import="org.grails.taggable.Tag"%>
<%@ page import="species.participation.ActivityFeedService"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'UFile.label', default: 'UFile')}" />
<title>File Manager</title>
<r:require modules="add_file" />
<uploader:head />
</head>
<body>
	<div class="body">
		<h1>Browse Files</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>



		<div class="project-list tab-content span8">

			<table class="table table-hover">
				<thead>
					<tr>

						<g:sortableColumn property="name"
							title="${message(code: 'UFile.name.label', default: 'Title')}" />

						<g:sortableColumn property="description"
							title="${message(code: 'UFile.source.label', default: 'Source')}" />


						<g:sortableColumn property="size"
							title="${message(code: 'UFile.file.label', default: 'File')}" />

					</tr>
				</thead>
				<tbody>
					<g:each in="${UFileInstanceList}" status="i" var="UFileInstance">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

							<td><g:link controller="UFile" action="show"
									id="${UFileInstance.id}">
									${fieldValue(bean: UFileInstance, field: "name")}
								</g:link></td>
							<%

	def className = UFileInstance.sourceHolderType
	def id = UFileInstance.sourceHolderId
	def sourceObj = grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
	//XXX Needs to be made generic.
	def parentLink = uGroup.createLink(controller:"project", action:"show", id:id, 'userGroupWebaddress':params?.webaddress)
 %>

							<td><a href="${parentLink}"> ${sourceObj}</a></td>



							<td><fileManager:displayIcon id="${UFileInstance.id}" /></td>


						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
		<div class="paginateButtons">
			<g:paginate total="${UFileInstanceTotal}" />
		</div>
	</div>


	<g:render template="/project/projectSidebar" />


</body>
</html>
