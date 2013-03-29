
<%@page import="species.License"%>
<table class="ufile-block">
	<thead></thead>
	<tbody>
		<tr>
			<td><input name="${fileId}.path" type="hidden"
				value='${filePath}' /> <input name="${fileId}.id" type="hidden"
				value='${fileId}' /> <input name="files" type="hidden"
				value='${fileId}' /> <input name="${name}" type="hidden"
				value='${fileId}' /> <g:hiddenField name='${fileId}.deleted'
					value='false' /> <g:hiddenField name='${fileId}.new'
					value="${uFileInstance?'false':'true'}" />

				<div>
					<span class="qq-upload-file"><i class="icon-file"></i> ${fileName}
					</span> <span class="qq-upload-size"> ${fileSize}
					</span>
				</div> <label class="control-label" for="name"> Title </label>
				<div class="controls">
					<input class="input-xlarge" type='text' name='${fileId}.name'
						value='${fileName}' placeholder=".input-xlarge" />
				</div> <label class="control-label" for="description"> Description
			</label>
				<div class="controls">
					<textarea rows='5' columns='10' name='${fileId}.description'
						value='${uFileInstance?.description}'> </textarea>
				</div> <label class="control-label" for='tags'> <i
					class="icon-tags"></i>Tags
			</label>
				<div class="controls">
					<ul class='file-tags' id="${fileId}-tags" name="${fileId}.tags">
						<g:if test='${uFileInstance}'>
							<g:each in="${uFileInstance.tags}" var="tag">
								<li>
									${tag}
								</li>
							</g:each>
						</g:if>
					</ul>
				</div> <label class="control-label" for="License"> License </label>

				<div id="${fileId}.license" class="licence_div dropdown">

					<a id="selected_license_${i}" class="btn dropdown-toggle btn-mini"
						data-toggle="dropdown"> <img
						src="${resource(dir:'images/license',file:'', absolute:true)}"
						title="Set a license for this image" /> <b class="caret"></b>
					</a>

					<ul id="license_options_${i}" class="dropdown-menu license_options">
						<span>Choose a license</span>
						<g:each in="${species.License.list()}" var="l">
							<li class="license_option"
								onclick="$('#license_${i}').val($.trim($(this).text()));$('#selected_license_${i}').find('img:first').replaceWith($(this).html());">
								<img
								src="${resource(dir:'images/license',file:l?.name?.getIconFilename()+'.png', absolute:true)}" /><span
								style="display: none;">
									${l?.name?.value}
							</span>
							</li>
						</g:each>
					</ul>

				</div></td>
			<td><span class="del-ufile"> <img
					src="${resource(dir:'images/skin', file:'close.png')}"
					style="vertical-align: middle;" /></td>
		</tr>
	</tbody>
</table>