<%@page import="content.Project"%>
<%@page import="content.eml.Document"%>

<table class="table table-hover">
				<thead>
					<tr>

						<g:sortableColumn property="title"
							title="${message(code: 'Document.title.label', default: 'Title')}" />

						<g:sortableColumn property="description"
							title="${message(code: 'Dcoument.source.label', default: 'Source')}" />

						<g:sortableColumn property="uFile"
							title="${message(code: 'Document.uFile.label', default: 'File')}" />

					</tr>
				</thead>
				<tbody>
					<g:each in="${documentInstanceList}" status="i" var="documentInstance">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

							<td>
									${fieldValue(bean: documentInstance, field: "title")}
								</td>
							<%

	def className = documentInstance.sourceHolderType
	def id = documentInstance.sourceHolderId
	def sourceObj = grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
	//XXX Needs to be made generic.
	def controller = 'project'
	
	switch(className) {
		case Project.class.getCanonicalName():
			controller = 'project'
			break

		default:
			controller = 'document'
			break
	}
	
	def parentLink = uGroup.createLink(controller: controller, action:"show", id:id, 'userGroupWebaddress':params?.webaddress)
 %>

							<td><a href="${parentLink}"> ${sourceObj}</a></td>



							<td><fileManager:displayIcon id="${documentInstance.uFile.id}" /></td>


						</tr>
					</g:each>
				</tbody>
			</table>
