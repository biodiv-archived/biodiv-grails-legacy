<%@page import="content.Project"%>
<%@page import="content.eml.Document"%>

<table class="table table-hover tablesorter">
	<thead>
		<tr>
			<th title="${message(code: 'Dcoument.title.label', default: 'Title')}">${message(code: 'Dcoument.title.label', default: 'Title')}</th>
			<th title="${message(code: 'Dcoument.type.label', default: 'Document Type')}">${message(code: 'Dcoument.type.label', default: 'Document Type')}</th>
			<th title="${message(code: 'Dcoument.source.label', default: 'Source')}">${message(code: 'Dcoument.source.label', default: 'Source')}</th>
		</tr>
	</thead>

	<tbody class="mainContentList" name="p${params?.offset}">

		<g:each in="${documentInstanceList}" status="i" var="documentInstance">
			<tr class="mainContent ${(i % 2) == 0 ? 'odd' : 'even'}">

				<td><a
					href='${uGroup.createLink(controller: "document", action:"show", id:documentInstance.id, 'userGroupWebaddress':params?.webaddress)}'>
						${documentInstance.title}
				</a></td>

				<td>
					${documentInstance?.type?.value }
				</td>
				<%

                                def className = documentInstance.sourceHolderType

                                def id = documentInstance.sourceHolderId
                                def controller = 'project'
                                String title = ""

                                switch(className) {
                                    case Project.class.getCanonicalName():
                                        controller = 'project'
                                        title="Project : "
                                        break

                                    default:
                                        className = 'document'
                                        id = documentInstance.id
                                        controller = 'document'
                                        title = "Document : "
                                        break
                                }

                                def sourceObj = grailsApplication.getArtefact("Domain",className)?.getClazz()?.read(id)
                                //XXX Needs to be made generic.

                                def parentLink = uGroup.createLink(controller: controller, action:"show", id:id, 'userGroupWebaddress':params?.webaddress)
 %>

				<td><g:if test="${sourceObj}">

                                    <a href="${parentLink}"> <span class="name" style="color: #b1b1b1;">${title}</span>${sourceObj}</a>

					</g:if> <g:else>

</g:else></td>




			</tr>
		</g:each>
	</tbody>
</table>
