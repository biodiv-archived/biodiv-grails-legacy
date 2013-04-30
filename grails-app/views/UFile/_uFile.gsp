<div class="super-section" style="clear: both;">

	<div class="section" style="clear: both;">
	


		<div
			class="control-group ${hasErrors(bean: uFileInstance, field: 'path', 'error')}">
			<label class="control-label" for="file"> File <span
							class="req">*</span></label>
			<div class="controls">

				<g:render template='/UFile/docUpload'
					model="['name': 'ufilepath', 'path': uFileInstance?.path, 'size':uFileInstance?.size]" />
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