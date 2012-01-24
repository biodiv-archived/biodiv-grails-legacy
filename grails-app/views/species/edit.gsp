<%@ page import="species.Species"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html" />
<meta name="layout" content="main" />
<title>Species Edit</title>
<g:javascript>

$(document).ready(function(){
 
	
});
</g:javascript>
</head>
<body>
	<div class="container_16">
		<div class="grid_16" align="center">
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
								title="${message(code: 'species.id.label', default: 'Id')}" />

							<g:sortableColumn property="title"
								title="${message(code: 'species.title.label', default: 'Title')}" />

							<g:sortableColumn property="percentOfInfo"
								title="${message(code: 'species.percentOfInfo.label', default: 'Percent of Info')}" />

							<g:sortableColumn property="reprImage"
								title="${message(code: 'species.reprImage.label', default: 'Representative Image')}" />

						</tr>
					</thead>
					<tbody>
						<g:each in="${speciesInstanceList}" status="i"
							var="speciesInstance">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

								<td><g:link action="show" id="${speciesInstance.id}">
										${fieldValue(bean: speciesInstance, field: "id")}
									</g:link>
								</td>

								<td>
									<g:link controller="species" action="show" id="${speciesInstance.id}">
												${speciesInstance.title}
											</g:link>
								</td>

								<td>
									${fieldValue(bean: speciesInstance, field: "percentOfInfo")}
								</td>

								<td>
									${fieldValue(bean: speciesInstance, field: "reprImage")}
								</td>

							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
			<div class="paginateButtons">
				<g:paginate total="${speciesInstanceTotal}" />
			</div>
		</div>
	</div>

</body>
</html>
