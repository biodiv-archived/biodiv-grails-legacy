<div class="span12 super-section" style="clear: both;">

	<div class="section" style="clear: both;">


		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'path', 'error')}">
			<label class="control-label" for="file"> File </label>
			<div class="controls">

				<g:render template='/UFile/docUpload'
					model="['name': 'ufilepath', 'path': uFileInstance?.path, 'size':uFileInstance?.size]" />
			</div>
		</div>

		<!-- 
		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'name', 'error')}">
			<label class="control-label" for="name"> Title </label>
			<div class="controls">
				<input class="input-xlarge" type='text' name='uFile.name'
					value='${uFileInstance?.name}' placeholder="File Name" />
			</div>
		</div>
		 -->
		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'description', 'error')}">
			<label class="control-label" for="description"> Description </label>
			<div class="controls">
				<textarea rows='5' columns='10' name='uFile.description'
					> ${uFileInstance?.description}</textarea>
			</div>

		</div>

		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'tags', 'error')}">
			<label class="control-label" for='tags'> <i class="icon-tags"></i>Tags
			</label>
			<div class="controls">
				<ul class='file-tags' id="${fileId}-tags" name="uFile.tags">
					<g:if test='${uFileInstance}'>
						<g:each in="${uFileInstance?.tags}" var="tag">
							<li>
								${tag}
							</li>
						</g:each>
					</g:if>
				</ul>
			</div>
		</div>


		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'contributors', 'error')}">
			<label class="control-label" for="contributors">Contributors</label>
			<div class="controls">
				<g:textField name="uFile.contributors"
					value="${uFileInstance?.contributors }" />
			</div>
		</div>



		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'attribution', 'error')}">
			<label class="control-label" for="attribution">Attribution</label>
			<div class="controls">
				<g:textField name="uFile.attribution"
					value="${uFileInstance?.attribution}" />
			</div>
		</div>


		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'license', 'error')}">
			<label class="control-label" for="License"> License </label>

			<div id="uFile.license" class="licence_div dropdown">

				<a id="selected_license_${i}" class="btn dropdown-toggle btn-mini"
					data-toggle="dropdown"> <img
					src="${uFileInstance?.license?uFileInstance.license.name.getIconFilename()+'.png':resource(dir:'images/license',file:'cc_by.png', absolute:true)}"
					title="Set a license for this file" /> <b class="caret"></b>
				</a>

				<ul id="license_options_${i}" class="dropdown-menu license_options">
					<span>Choose a license</span>
					<g:each in="${species.License.list()}" var="l">
						<li class="license_option"
							onclick="$('#license_${i}').val($.trim($(this).text()));$('#selected_license_${i}').find('img:first').replaceWith($(this).html());">
							<img
							src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span
							style="display: none;"> ${l?.name?.value}
						</span>
						</li>
					</g:each>
				</ul>
			</div>
		</div>
	</div>
</div>