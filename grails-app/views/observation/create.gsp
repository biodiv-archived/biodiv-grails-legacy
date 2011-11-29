<%@ page import="species.participation.Observation"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'observation.label', default: 'Add Observation')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>

<g:javascript>
	$(document).ready(function(){
		$( "#addAnother" ).click(function() { 
				return false; 
			}
		);
	});
</g:javascript>
</head>
<body>
	<div class="container_16">
		<div class="grid_12">
			<h1>
				<g:message code="default.create.label" args="[entityName]" />
			</h1>

			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>

			<g:hasErrors bean="${observationInstance}">
				<div class="errors">
					<g:renderErrors bean="${observationInstance}" as="list" />
				</div>
			</g:hasErrors>

			<div class="dialog">
				<g:each in="${observationInstance.resource}" var="r">
				</g:each>

				<g:form action="upload" method="post" enctype="multipart/form-data">
					<div
						class="value ${hasErrors(bean: observationInstance, field: 'resource', 'errors')}">
						<input type="file" name="imageFile" /> <span class="button"><g:submitButton
								name="Attach another file" class="save"
								value="${message(code: 'default.button.create.label', default: 'Attach another file')}" />
						</span>
					</div>
				</g:form>
			</div>

			<br />

			<g:form action="save">
				<div class="dialog">
					<table>
						<tbody>

							
							<tr class="prop">
								<td valign="top" class="name"><label for="observedOn"><g:message
											code="observation.observedOn.label" default="Observed On" />
								</label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: observationInstance, field: 'observedOn', 'errors')}">
									<g:datePicker name="observedOn" precision="day"
										value="${observationInstance?.observedOn}"
										class="ui-widget-content ui-corner-all" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name"><label for="url"><g:message
											code="observation.url.label" default="Url" /> </label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: observationInstance, field: 'url', 'errors')}">
									<g:textField name="url" value="${observationInstance?.url}"
										class="text ui-widget-content ui-corner-all" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name"><label for="notes"><g:message
											code="observation.notes.label" default="Notes" /> </label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: observationInstance, field: 'notes', 'errors')}">
									<g:textArea name="notes" value="${observationInstance?.notes}"
										class="text ui-widget-content ui-corner-all" />
								</td>
							</tr>

						</tbody>
					</table>
				</div>

				<div class="buttons">
					<span class="button"><g:submitButton name="create"
							class="save"
							value="${message(code: 'default.button.create.label', default: 'Save')}" />
					</span>
				</div>
			</g:form>
		</div>
	</div>
</body>
</html>

