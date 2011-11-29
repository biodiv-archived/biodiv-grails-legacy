<div class="body">
	
	<g:set var="entityName"
		value="${message(code: 'speciesField.label', default: 'SpeciesField')}" />
	<h4>
		<g:message code="default.edit.label" args="[entityName]" />
	</h4>
	<g:if test="${flash.message}">
		<div class="message">
			${flash.message}
		</div>
	</g:if>
	<g:hasErrors bean="${speciesFieldInstance}">
		<div class="errors">
			<g:renderErrors bean="${speciesFieldInstance}" as="list" />
		</div>
	</g:hasErrors>
	<g:form method="post" controller="speciesField" action="index">
		<g:hiddenField name="id" value="${speciesFieldInstance?.id}" />
		<g:hiddenField name="version" value="${speciesFieldInstance?.version}" />
		<div class="dialog">
			<table>
				<tbody>

					<tr class="prop">
						<td valign="top" class="name"><label for="audienceType"><g:message
									code="speciesField.audienceType.label" default="Audience Type" />
						</label>
						</td>
						<td valign="top"
							class="value ${hasErrors(bean: speciesFieldInstance, field: 'audienceType', 'errors')}">
							<g:select name="audienceType"
								from="${species.SpeciesField$AudienceType?.values()}"
								optionKey="name" optionValue="value"
								keys="${species.SpeciesField$AudienceType?.values()*.name()}"
								multiple="true"/>
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="license"><g:message
									code="speciesField.license.label" default="License" /> </label>
						</td>
						<td valign="top"
							class="value ${hasErrors(bean: speciesFieldInstance, field: 'license', 'errors')}">
							<g:select name="license.id" from="${species.License.list()}"
								optionKey="id" optionValue="name"
								keys="${species.License.list()?.id}"
								value="${speciesFieldInstance?.licenses?.id}"  multiple="true"/></td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="description"><g:message
									code="speciesField.description" default="Description" /> </label>
						</td>
						<td valign="top"
							class="value ${hasErrors(bean: speciesFieldInstance, field: 'description', 'errors')}">
							
							<g:textArea name="description" id="description${speciesFieldInstance?.id}" class="fieldEditor">${speciesFieldInstance?.description }</g:textArea>
								
							</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="references"><g:message
									code="speciesField.references.label" default="References" /> </label>
						</td>
						<td valign="top"
							class="value ${hasErrors(bean: speciesFieldInstance, field: 'references', 'errors')}">
							<g:render template="/common/editReferencesTemplate" model="['speciesFieldInstance':speciesFieldInstance]"/>
						</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name"><label for="resources"><g:message
									code="speciesField.resources.label" default="Resources" /> </label>
						</td>
						<td valign="top"
							class="value ${hasErrors(bean: speciesFieldInstance, field: 'resources', 'errors')}">

							<ul>
								<g:each in="${speciesFieldInstance?.resources?}" var="r">
									<li><a><img class="icon"
						src="${createLinkTo(dir: 'images/resources/'+speciesFieldInstance.species.id, file:r.fileName.trim(), absolute:true)}"
						title="${r?.description}" style="float:left;"/></a><p class='caption clearfix'>
							${r?.description==null?r.type.value():r.description}
						<g:link controller="resource" action="edit"
											id="${r.id}" style="float:right;">
											[edit]
										</g:link></p></li>
								</g:each>
							</ul> <g:link controller="resource" action="create"
								params="['speciesField.id': speciesFieldInstance?.id]">
								${message(code: 'default.add.label', args: [message(code: 'resource.label', default: 'Resource')])}
							</g:link>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		<div class="buttons">
			<span class="button"><g:actionSubmit class="save"
					action="update"
					value="${message(code: 'default.button.update.label', default: 'Update')}" 
					/>
			</span> <span class="button"><g:actionSubmit class="delete"
					action="delete"
					value="${message(code: 'default.button.delete.label', default: 'Delete')}"
					onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
			</span>
		</div>
	</g:form>
</div>