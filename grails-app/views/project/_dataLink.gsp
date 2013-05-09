<div id="dataLink${i}" class="dataLink-div"
	<g:if test="${hidden}">style="display:none;"</g:if>>
	
	<table  style="background-color:whitesmoke;">
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
			<textArea rows="4" name='dataLinksList[${i}].description' class="description"
				 >${dataLink?.description}</textArea>
		</div>
	</div>
    </td>
    <td>
	<div class="control-group">
		<label class="control-label" for="url"><g:message
				code="project.dataLink.url.label" default="Url" /> </label>

		<div class="controls">
			<input type="url" name='dataLinksList[${i}].url' class="input-block-level"
				value='${dataLink?.url}' />
		</div>
	</div>

</td>
<td>

	<span class="del-dataLink" style="margin-left:20px; padding:5px;"> <img
		src="${resource(dir:'images/skin', file:'remove.jpg')}"
		style="vertical-align: middle;" />
	</span>
	</td>
	</tr>
	</tbody>
	</table>
</div>
