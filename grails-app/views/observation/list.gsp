
<%@ page import="species.participation.Observation"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Observations')}" />
<title><g:message code="default.list.label" args="[entityName]" />
</title>
</head>
<body>
	<div class="container_16">
		<div class="grid_16">
			<h1>
				<g:message code="default.list.label" args="[entityName]" />
			</h1>
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>
			<div class="list">
					<div class="observations thumbwrap grid_16">
						<div class="observation">
							<g:each in="${observationInstanceList}" status="i"
								var="observationInstance">
								<obv:showSnippet model="['observationInstance':observationInstance]"></obv:showSnippet>
							</g:each>
						</div>
					</div>
				</div>
			<div class="paginateButtons">
				<g:paginate total="${observationInstanceTotal}" max="2"/>
			</div>
		</div>
	</div>
	<g:javascript>	
	$(document).ready(function(){
		
	});
	</g:javascript>
</body>
</html>
