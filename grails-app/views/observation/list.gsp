
<%@ page import="species.participation.Observation"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observation')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
</head>
<body>
	<div class="container_12">

		<div class="grid_12">
			<h1>
				<g:message code="default.list.label" args="[entityName]" />
			</h1>
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>
			<div class="list">
				<table>
					<thead>
						<tr>

							<g:sortableColumn property="id"
								title="${message(code: 'observation.id.label', default: 'Id')}" />

							<th><g:message code="observation.author.label"
									default="Author" />
							</th>

							<g:sortableColumn property="createdOn"
								title="${message(code: 'observation.createdOn.label', default: 'Created On')}" />

							<g:sortableColumn property="notes"
								title="${message(code: 'observation.notes.label', default: 'Notes')}" />

							<g:sortableColumn property="observedOn"
								title="${message(code: 'observation.observedOn.label', default: 'Observed On')}" />

							<g:sortableColumn property="title"
								title="${message(code: 'observation.title.label', default: 'Title')}" />

						</tr>
					</thead>
					<tbody>
						<g:each in="${observationInstanceList}" status="i"
							var="observationInstance">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

								<td><g:link action="show" id="${observationInstance.id}">
										${fieldValue(bean: observationInstance, field: "id")}
									</g:link>
								</td>

								<td>
									${fieldValue(bean: observationInstance, field: "author")}
								</td>

								<td><g:formatDate date="${observationInstance.createdOn}" />
								</td>

								<td>
									${fieldValue(bean: observationInstance, field: "notes")}
								</td>

								<td><g:formatDate date="${observationInstance.observedOn}" />
								</td>

								<td>
									${fieldValue(bean: observationInstance, field: "title")}
								</td>

							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
			<div class="paginateButtons">
				<g:paginate total="${observationInstanceTotal}" />
			</div>
		</div>
	</div>
</body>
</html>
