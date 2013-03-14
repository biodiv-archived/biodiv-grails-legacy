<div class="ufile-block">

	<input name="${fileId}.path" type="hidden" value='${filePath}' />
	<input name="${fileId}.id" type="hidden" value='${fileId}' /> <input
		name="files" type="hidden" value='${fileId}' /> <input
		name="${name}" type="hidden" value='${fileId}' />



	<g:hiddenField name='${fileId}.deleted' value='false' />
	<g:hiddenField name='${fileId}.new' value="${uFileInstance?'false':'true'}" />

	<span class="qq-upload-file">${fileName}</span> <span
		class="qq-upload-size">${fileSize}</span> <label for="name">
		Name </label> <input type='text' name='${fileId}.name' value='${fileName}' />

	<label for="description"> Description </label>
	<textarea rows='5' columns='10' name='${fileId}.description' value='${uFileInstance?.description}'> </textarea>

	<label for='tags'> Tags </label>
	<ul class='file-tags' id="${fileId}-tags" name="${fileId}.tags">
	<g:if test='${uFileInstance}'>
		<g:each in="${uFileInstance.tags}" var="tag">
			<li>
				${tag}
			</li>
		</g:each>
		</g:if>
	</ul>


	<span class="del-ufile"> <img
		src="${resource(dir:'images/skin', file:'close.png')}"
		style="vertical-align: middle;" />
	</span>
</div>