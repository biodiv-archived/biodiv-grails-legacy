<%@page import="content.Project"%>
<%@page import="content.eml.Document"%>

<table class="table table-hover tablesorter">
	<thead>
		<tr>
			<th title="${message(code: 'Dcoument.title.label', default: 'Title')}">${message(code: 'Dcoument.title.label', default: 'Title')}</th>
			<th title="${message(code: 'Dcoument.type.label', default: 'Document Type')}">${message(code: 'Dcoument.type.label', default: 'Document Type')}</th>
			<th title="${message(code: 'Dcoument.description.label', default: 'Description')}">${message(code: 'Dcoument.description.label', default: 'Description')}</th>
			<g:if test="${canPullResource}">		
				<th title="${message(code: 'Dcoument.pullToGroup.label', default: 'Post on Group')}">${message(code: 'Dcoument.pullToGroup.label', default: 'Post')}</th>
			</g:if>
		</tr>
	</thead>

	<tbody class="mainContentList" name="p${params?.offset}">

		<g:each in="${documentInstanceList}" status="i" var="documentInstance">
			<tr class="mainContent ${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>
                                <g:if test="${documentInstance}">
                                <g:set var="featureCount" value="${documentInstance.featureCount}"/>
                                </g:if>


                                <span class="badge ${documentInstance.fetchSpeciesGroup().iconClass()} ${(featureCount>0) ? 'featured':''}" style="position:relative;">
                                </span>


                                <a style="vertical-align:middle;"
						href='${uGroup.createLink(controller: "document", action:"show", id:documentInstance.id, userGroup:userGroupInstance)}'>
						${documentInstance.title}
					</a>
				</td>
				<td>
					${documentInstance?.type?.value }
				</td>
				<td class="ellipsis multiline" style="max-width:220px;">
					${documentInstance.notes}
				</td>
				<g:if test="${canPullResource}">
					<td>
						<uGroup:objectPost model="['objectInstance':documentInstance, canPullResource:canPullResource]" />
					</td>
				</g:if>
			</tr>
		</g:each>
	</tbody>
</table>
