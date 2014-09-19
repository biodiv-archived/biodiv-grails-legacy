<%@page import="content.Project"%>
<%@page import="content.eml.Document"%>

<table class="table table-hover tablesorter">
	<thead>
		<tr>
			<th title="${message(code:'Dcoument.title.label')}">${message(code: 'Dcoument.title.label')}</th>
			<th title="${message(code: 'Dcoument.type.label')}">${message(code: 'Dcoument.type.label')}</th>
			<th title="${message(code: 'Dcoument.description.label')}">${message(code: 'Dcoument.description.label')}</th>
			<g:if test="${canPullResource}">		
				<th title="${message(code: 'Dcoument.pullToGroup.label')}">${message(code: 'Dcoument.pullToGroup.label')}</th>
			</g:if>
		</tr>
	</thead>

	<tbody class="mainContentList" name="p${params?.offset}">

		<g:each in="${documentInstanceList}" status="i" var="documentInstance">
			<tr class="mainContent ${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td class="table-column title">
                                <g:if test="${documentInstance}">
                                <g:set var="featureCount" value="${documentInstance.featureCount}"/>
                                </g:if>


                                <span class="badge ${(featureCount>0) ? 'featured':''}" style="position:relative;" title="${(featureCount>0) ? 'Featured':''}" >
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
					${raw(documentInstance.notes)}
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
