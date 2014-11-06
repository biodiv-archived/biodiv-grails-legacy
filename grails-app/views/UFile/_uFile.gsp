<div class="super-section" style="clear: both;">

	<div class="section" style="clear: both;">
	


		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'path', 'error')}">
			<label class="control-label" for="file"> <g:message code="default.file.label" /> <span
							class="req">*</span></label>
			<div class="controls">

				<g:render template='/UFile/docUpload'
					model="['name': 'ufilepath', 'path': uFileInstance?.path, 'size':uFileInstance?.size]" />
			</div>
		</div>




	</div>
</div>
