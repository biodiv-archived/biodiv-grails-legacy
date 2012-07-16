<div id="reference${i}" class="reference-div">
	<g:hiddenField name="referencesList[${i}].id"
		value="${referenceInstance?.id}" />
	<g:hiddenField name="referencesList[${i}].version"
		value="${referenceInstance?.version}" />
	<g:hiddenField name='referencesList[${i}].deleted' value='false' />
	<g:hiddenField name='referencesList[${i}].new' value='false' />
	<g:hiddenField name="referencesList[${i}].speciesField.id"
		value="${referenceInstance?.speciesField?.id}" />
	<div class="dialog">
		<table>
			<tbody>
				<tr class="prop">
					<td valign="middle" class="name"><label for="url"><g:message
								code="reference.url.label" default="Url" /> </label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: referenceInstance, field: 'url', 'errors')}">
						<g:textField name="referencesList[${i}].url"
							value="${referenceInstance?.url}" />
					</td>
				</tr>

				<tr class="prop">
					<td valign="middle" class="name"><label for="title"><g:message
								code="reference.title.label" default="Title" /> </label>
					</td>
					<td valign="top"
						class="value ${hasErrors(bean: referenceInstance, field: 'title', 'errors')}">
						<g:textField name="referencesList[${i}].title"
							value="${referenceInstance?.title}" />
						<span class="del-reference">
        					<img src="${resource(dir:'images/skin', file:'icon-delete.png')}"
            					style="vertical-align:middle;"/>
    					</span>
					</td>
				</tr>

			</tbody>
		</table>

	</div>
</div>