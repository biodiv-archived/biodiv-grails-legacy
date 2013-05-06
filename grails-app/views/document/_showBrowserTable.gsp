<%@page import="content.Project"%>
<%@page import="content.eml.Document"%>

<table class="table table-hover">
	<thead>
		<tr>

			<g:sortableColumn property="title"
				title="${message(code: 'Document.title.label', default: 'Title')}" />

			<g:sortableColumn property="description"
				title="${message(code: 'Dcoument.description.label', default: 'Description')}" />

			<g:sortableColumn property="type"
				title="${message(code: 'Dcoument.type.label', default: 'Document Type')}" />

			<g:sortableColumn property="description"
				title="${message(code: 'Dcoument.source.label', default: 'Source')}" />



		</tr>
	</thead>

	<tbody class="mainContentList" name="p${params?.offset}">

		<g:each in="${documentInstanceList}" status="i" var="documentInstance">
			<tr class="mainContent ${(i % 2) == 0 ? 'odd' : 'even'}">

				<td><a
					href='${uGroup.createLink(controller: "document", action:"show", id:documentInstance.id, 'userGroupWebaddress':params?.webaddress)}'>
						${documentInstance.title}
				</a></td>
				<td class="ellipsis multiline" style="max-width: 250px;">
					${documentInstance?.description }
				</td>
				<td>
					${fieldValue(bean: documentInstance, field: "type")}
				</td>
				<%

	def className = documentInstance.sourceHolderType

									def id = documentInstance.sourceHolderId
												def controller = 'project'


	switch(className) {
		case Project.class.getCanonicalName():
			controller = 'project'
			break

		default:
			className = 'document'
			id = documentInstance.id
			controller = 'document'
			break
	}
									
								def sourceObj = grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
								//XXX Needs to be made generic.
	
	def parentLink = uGroup.createLink(controller: controller, action:"show", id:id, 'userGroupWebaddress':params?.webaddress)
 %>

				<td><g:if test="${sourceObj}">

						<a href="${parentLink}"> ${sourceObj}</a>

					</g:if> <g:else>
Generic
</g:else></td>




			</tr>
		</g:each>
	</tbody>
</table>
