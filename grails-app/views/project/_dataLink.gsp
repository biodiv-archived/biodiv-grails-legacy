<div id="dataLink${i}" class="dataLink-div"
	<g:if test="${hidden}">style="display:none;"</g:if>>
	
	<table>
	<thead></thead>
	<tbody>
	<tr>
	<td>
	<g:hiddenField name='dataLinksList[${i}].id' value='${dataLink?.id}' />
	<g:hiddenField name='dataLinksList[${i}].deleted' value='false' />
	<g:hiddenField name='dataLinksList[${i}].new'
		value="${dataLink?.id == null?'true':'false'}" />

	<div class="control-group">
		<label class="control-label" for="description"><g:message
				code="project.dataLink.description.label" default="Description" /> </label>

		<div class="controls">
			<textArea name='dataLinksList[${i}].description' class="site-name"
				value='${dataLink?.description}' ></textArea>
		</div>
	</div>

	<div class="control-group">
		<label class="control-label" for="url"><g:message
				code="project.dataLink.url.label" default="url" /> </label>

		<div class="controls">
			<g:textField name='dataLinksList[${i}].url' class="url"
				value='${dataLink?.url}' />
		</div>
	</div>

</td>
<td>
	<span class="del-dataLink"> <img
		src="${resource(dir:'images/skin', file:'close.png')}"
		style="vertical-align: middle;" />
	</span>
	</td>
	</tr>
	</tbody>
	</table>
</div>
