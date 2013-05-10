<div id="dataLink${i}" class="dataLink-div block"
	<g:if test="${hidden}">style="display:none;"</g:if>>
	
	<g:hiddenField name='dataLinksList[${i}].id' value='${dataLink?.id}' />
	<g:hiddenField name='dataLinksList[${i}].deleted' value='false' />
	<g:hiddenField name='dataLinksList[${i}].new'
		value="${dataLink?.id == null?'true':'false'}" />

	<div class="control-group span5">
		<label class="control-label" for="description"><g:message
				code="project.dataLink.description.label" default="Description" /> </label>

		<div class="controls">
			<textArea rows="4" name='dataLinksList[${i}].description' class="description input-xlarge"
				 >${dataLink?.description}</textArea>
		</div>
	</div>
	<div class="control-group span5">
		<label class="control-label" for="url"><g:message
				code="project.dataLink.url.label" default="Url" /> </label>

		<div class="controls">
			<input type="url" name='dataLinksList[${i}].url' id='dataLinksList[${i}].url' class="input-xlarge"
				value='${dataLink?.url}' />
		</div>
	</div>

        <span class="del-dataLink close_button"></span>
</div>
