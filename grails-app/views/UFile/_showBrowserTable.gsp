<%@page import="content.Project"%>
<%@page import="content.eml.Document"%>

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

							<td>
									${fieldValue(bean: UFileInstance, field: "name")}
								</td>
							<%

	def className = UFileInstance.sourceHolderType
	def id = UFileInstance.sourceHolderId
	def sourceObj = grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
	//XXX Needs to be made generic.
	def controller = 'project'
	
	switch(className) {
		case Project.class.getCanonicalName():
			controller = 'project'
			break
		case Document.class.getCanonicalName():
			controller = 'document'
			break
		default:
			controller = 'project'
			break
	}
	
	def parentLink = uGroup.createLink(controller: controller, action:"show", id:id, 'userGroupWebaddress':params?.webaddress)
 %>

							<td><a href="${parentLink}"> ${sourceObj}</a></td>



							<td><fileManager:displayIcon id="${UFileInstance.id}" /></td>


						</tr>
					</g:each>
				</tbody>
			</table>
