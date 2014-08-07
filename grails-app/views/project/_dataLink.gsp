<div id="dataLink${i}" class="dataLink-div block"
	<g:if test="${hidden}">style="display:none;"</g:if>>
	
	<g:hiddenField name='dataLinks[${i}].id' value='${dataLink?.id}' />
	<g:hiddenField name='dataLinks[${i}].deleted' value='false' />
	<g:hiddenField name='dataLinks[${i}].new'
		value="${dataLink?.id == null?'true':'false'}" />

	<div class="control-group span5">
		<label class="control-label" for="description"><g:message
				code="project.dataLink.description.label" default="Description" /> </label>

		<div class="controls">
                    <textArea rows="4" name='dataLinks[${i}].description' class="description input-xlarge" 
                        placeholder="Enter a brief description about datalink" >${dataLink?.description}</textArea>
		</div>
	</div>
	<div class="control-group span5">
		<label class="control-label" for="url"><g:message
				code="project.dataLink.url.label" default="Url" /> </label>

		<div class="controls">
			<input type="url" name='dataLinks[${i}].url' id='dataLinks[${i}].url' class="input-xlarge"
				placeholder="Enter url for the datalink" value='${dataLink?.url}' />
		</div>
	</div>

        <span class="del-dataLink close_button"></span>
</div>
